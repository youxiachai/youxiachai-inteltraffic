package com.dracode.andrdce.ct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheUtil {

	private CacheDBHelper dbadapter;

	private UserAppSession userSession;

	public static final String FIELD_ID = "id";
	public static final String FIELD_DATATYPE = "data_type";
	public static final String FIELD_BLOBVAL = "blob_val";
	public static final String FIELD_EXPIRETM = "expire_time";

	public CacheUtil(UserAppSession userSession) {
		this.userSession = userSession;

		if (this.userSession == null) {
			// UserSession.LogE("CacheUtil create: UserSession为NULL");
			throw new CtRuntimeException("CacheUtil create: UserSession为NULL");
		}

		// 检测数据库
		if (dbadapter == null) {
			dbadapter = new CacheDBHelper(userSession);
		}
	}

	/**
	 * 执行Cache指令
	 * 
	 * @param url
	 * @param fieldMap
	 */
	public Object executeCacheUrl(String url, Map<String, Object> fieldMap) {
		if (url == null || url.length() < 1)
			return null;
		String s = getCommand(url);
		if (s == null)
			return null;
		// 检测数据库
		if (dbadapter == null)
			throw new CtRuntimeException("执行缓存命令出错：dbadapter为空");
		try {
			if ("init".equals(s)) {
				initCache(url, fieldMap);
				return null;
			}
			if ("put".equals(s)) {
				if (fieldMap == null)
					putCache(url);
				else
					putCache(url, fieldMap);
				return null;
			}
			if ("get".equals(s))
				return getCacheList(url, fieldMap);
			if ("getobj".equals(s))
				return getCacheObj(url, fieldMap);
			if ("remove".equals(s)) {
				removeCache(url, fieldMap);
				return null;
			}
			if ("clear".equals(s)) {
				clearCache(url);
				return null;
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw new CtRuntimeException("执行缓存命令出错：" + s + " - "
					+ ex.getMessage());
		}
		return null;
	}

	public Object executeCacheUrl(String url) {
		return executeCacheUrl(url, null);
	}

	List<String> initedTables = new ArrayList<String>();

	public void putToCache(String table, String tp, String key,
			Object value, long expireTm) {
		key = UserAppSession.urlEncode(key);
		String url;
		boolean tbInited = isTableInited(table);
		if (!tbInited) {
			url = "sqldb://" + table + "/init?" + FIELD_ID + "=s";
			url = url + "&" + FIELD_DATATYPE + "=s";
			url = url + "&" + FIELD_BLOBVAL + "=b";
			url += "&" + FIELD_EXPIRETM + "=d";
			initCache(url, null);
		}
		url = "sqldb://" + table + "/put?id=" + key;
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		fieldMap.put(FIELD_ID, key);
		fieldMap.put(FIELD_DATATYPE, tp);
		fieldMap.put(FIELD_EXPIRETM, System.currentTimeMillis() + expireTm);

		if (value == null)
			fieldMap.put(FIELD_BLOBVAL, value);
		else {
			try {
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(byteOut);
				out.writeObject(value);
				fieldMap.put(FIELD_BLOBVAL, byteOut.toByteArray());
				out.close();
				byteOut.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new CtRuntimeException("生成缓存字节码出错：" + e.getMessage());
			}
		}

		putCache(url, fieldMap);
	}

	public void removeFromCache(String table, String tp, String key) {
		key = UserAppSession.urlEncode(key);
		String url = "sqldb://" + table + "/remove";
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		if (key != null && key.length() > 0)
			fieldMap.put(FIELD_ID, key);
		if (tp != null && tp.length() > 0)
			fieldMap.put(FIELD_DATATYPE, tp);
		this.removeCache(url, fieldMap);
	}

	public Object getFromCache(String table, String key) {
		return getFromCacheEx(table, key, false);
	}

	public Object getFromCacheEx(String table, String key, boolean useExpiredObj) {
		Map<String, Object> m = this.getMapFromCacheEx(table, key);
		if (m == null)
			return null;
		else {
			if (!useExpiredObj) {
				long tm = TypeUtil.ObjectToLong(m.get(FIELD_EXPIRETM));
				if (tm > 0 && tm < System.currentTimeMillis())
					return null;
			}
			byte[] value = (byte[]) m.get(FIELD_BLOBVAL);
			if (value == null)
				return null;
			return bytesToObj(value);
		}
	}

	public static Object bytesToObj(byte[] value) {
		try {
			ByteArrayInputStream byteIn = new ByteArrayInputStream(value);
			ObjectInputStream in = new ObjectInputStream(byteIn);
			Object res = in.readObject();
			in.close();
			byteIn.close();
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			throw new CtRuntimeException("读取缓存字节码出错：" + e.getMessage());
		}
	}
	
	public Map<String, Object> getMapFromCacheEx(String table, String key) {
		key = UserAppSession.urlEncode(key);
		String url = "sqldb://" + table + "/getobj";
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		fieldMap.put(FIELD_ID, key);
		Map<String, Object> m = getCacheObj(url, fieldMap);
		return m;
	}

	public boolean isTableInited(String table) {
		boolean tbInited = false;
		for (String t : initedTables)
			if (t.equals(table)) {
				tbInited = true;
				break;
			}

		if (!tbInited)
			if (dbadapter.checkTable(table)) {
				initedTables.add(table);
				tbInited = true;
			}
		return tbInited;
	}

	/**
	 * 初始化Cache，创建表，表存在，不做操作
	 * 
	 * @param url
	 *            ，SQLLITE下格式为：sqldb://表名/init?id=值&字段1=Integer&字段2=String，
	 *            其中id是必须的，字段类型有Integer，String，Double，Float，Long，Short，Blob
	 * @param fieldMap
	 *            可以为NULL，不为NULL时，id是必须的，会忽略url问号后面
	 *            url和fieldMap都不传字段，默认创建id(String型)和val(Blob型)两个字段的表
	 */
	public synchronized void initCache(String url, Map<String, Object> fieldMap) {
		if (url == null || url.length() < 1)
			throw new CtRuntimeException("initCache: url为NULL或空值");
		if (!"init".equals(getCommand(url)))
			throw new CtRuntimeException("initCache:指令不正确");
		if (url.indexOf("id=") < 0
				|| (fieldMap != null && fieldMap.get("id") == null))
			throw new CtRuntimeException("initCache:没有传id字段");
		// 检测表
		String table = getTableName(url);
		if (!dbadapter.checkTable(table)) {
			UserAppSession.LogD("initCache: table " + table
					+ " not exist, recreate!");

			String fieldStr = "";
			if (url.indexOf("?") + 1 < url.length()) {
				fieldStr = url.substring(url.indexOf("?") + 1);
			}

			if (fieldMap != null && fieldMap.entrySet().size() > 0) {
				// 创建表
				dbadapter.createTable(table, fieldMap);
			} else if (fieldStr.length() > 0) {
				Map<String, Object> _fieldMap = getFieldMap(url);
				// 创建表
				dbadapter.createTable(table, _fieldMap);
			} else {
				Map<String, Object> _fieldMap = new HashMap<String, Object>();
				_fieldMap.put(FIELD_ID, "String");
				_fieldMap.put(FIELD_BLOBVAL, "Blob");
				// 创建表
				dbadapter.createTable(table, _fieldMap);
			}
		}
		if (!isTableInited(table))
			initedTables.add(table);
	}

	/**
	 * 新增或更新Cache
	 * 
	 * @param url
	 *            : SQLLITE下格式为：sqldb://表名/put?id=id值&字段1=值1&字段2=值2，id参数是必须的
	 */
	public void putCache(String url) {
		putCache(url, null);
	}

	/**
	 * 新增或更新Cache
	 * 
	 * @param url
	 *            : SQLLITE下格式为：sqldb://表名/put
	 * @param fieldMap
	 *            ：字段名和字段值集合，id字段是必须的，url后面带有字段会忽略
	 */
	public void putCache(String url, Map<String, Object> fieldMap) {

		if (url == null || url.length() < 1)
			throw new CtRuntimeException("putCache: url为NULL或空值");
		String table = getTableName(url);
		String fieldStr = "";
		if (url.indexOf("?") + 1 < url.length()) {
			fieldStr = url.substring(url.indexOf("?") + 1);
		}

		if (!"put".equals(getCommand(url)))
			throw new CtRuntimeException("putCache:指令不正确");
		if (fieldMap != null && fieldMap.get("id") == null)
			throw new CtRuntimeException("putCache:fieldMap中没有传id字段");

		Map<String, Object> idMap = null;
		idMap = new HashMap<String, Object>();
		idMap.put("SelectFields", "id");
		if (fieldMap != null)
			idMap.put("id", fieldMap.get("id"));

		List<Map<String, Object>> cacheList = getCacheList("sqldb://" + table
				+ "/get", idMap);
		idMap.remove("SelectFields");

		if (cacheList.size() != 1) {
			if (cacheList.size() > 1)
				removeCache(url.replaceFirst("put", "remove"), idMap);
			if (fieldMap != null && fieldMap.entrySet().size() > 0) {
				// 新增
				dbadapter.insert(table, fieldMap);
			} else if (fieldStr.length() > 0) {
				// 新增
				dbadapter.insert(table, getFieldMap(url));
			} else {
				UserAppSession.LogD("putCache: 没有可新增的数据");
			}
		} else {
			if (fieldMap != null && fieldMap.entrySet().size() > 0) {
				// 修改
				dbadapter.update(table, fieldMap, idMap);
			} else if (fieldStr.length() > 0) {
				// 修改
				dbadapter.update(table, getFieldMap(url), idMap);
			} else {
				UserAppSession.LogD("putCache:没有可修改的数据");
			}
		}
	}

	/**
	 * 删除Cache
	 * 
	 * @param url
	 *            : SQLLITE下格式为：sqldb://表名/remove?字段1=过滤值1&字段2=过滤值2
	 */
	public void removeCache(String url) {
		removeCache(url, null);
	}

	public void removeCache(String url, Map<String, Object> fieldMap) {
		if (url == null || url.length() < 1)
			throw new CtRuntimeException("removeCache: url为NULL或空值");
		String table = getTableName(url);

		if (fieldMap == null || fieldMap.size() == 0)
			fieldMap = getFieldMap(url);

		if (!"remove".equals(getCommand(url)))
			throw new CtRuntimeException("removeCache:指令不正确");

		dbadapter.delete(table, fieldMap);
	}

	/**
	 * 清空Cache
	 * 
	 * @param url
	 *            : SQLLITE下格式为：sqldb://表名/clear
	 */
	public void clearCache(String url) {
		if (url == null || url.length() < 1)
			throw new CtRuntimeException("clearCache:url为NULL或空值");
		String table = getTableName(url);

		if (!"clear".equals(getCommand(url)))
			throw new CtRuntimeException("clearCache: 指令不正确");

		dbadapter.delete(table, null);
	}

	/**
	 * 获取Cache
	 * 
	 * @param url
	 *            : SQLLITE下格式为：sqldb://表名/get?字段1=值1&字段2=值2&OrderBy=id asc,name
	 *            desc&RowCount=10，
	 *            SelectFields表示要查询的字段，OrderBy表示要排序，RowCount为要获取的记录数
	 *            ，SelectFields、OrderBy和RowCount都区分大小写
	 */
	public List<Map<String, Object>> getCacheList(String url) {
		return getCacheList(url, null);
	}

	public List<Map<String, Object>> getCacheList(String url,
			Map<String, Object> fieldMap) {
		// long tm=System.currentTimeMillis();
		// UserSession.LogD("tm:"+Long.toString(System.currentTimeMillis()-tm)+" aa: 1");
		if (url == null || url.length() < 1)
			throw new CtRuntimeException("getCacheList: url为NULL或空值");
		String table = getTableName(url);
		if (fieldMap == null || fieldMap.size() == 0)
			fieldMap = getFieldMap(url);

		if (!"get".equals(getCommand(url)))
			throw new CtRuntimeException("getCacheList: 指令不正确");

		if (!dbadapter.checkTable(table))
			return new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> list = dbadapter.get(table, fieldMap);
		if (list == null)
			list = new ArrayList<Map<String, Object>>();
		return list;
	}

	/**
	 * 根据ID获取Cache
	 * 
	 * @param url
	 *            : SQLLITE下格式为：sqldb://表名/getobj?id=值
	 */
	public Map<String, Object> getCacheObj(String url) {
		return getCacheObj(url, null);
	}

	public Map<String, Object> getCacheObj(String url,
			Map<String, Object> fieldMap) {
		if (url == null || url.length() < 1)
			throw new CtRuntimeException("getCacheObj: url为NULL或空值");
		String table = getTableName(url);
		if (fieldMap == null || fieldMap.size() == 0)
			fieldMap = getFieldMap(url);

		if (!"getobj".equals(getCommand(url)))
			throw new CtRuntimeException("getCacheObj: 指令不正确");

		if (!dbadapter.checkTable(table))
			return null;
		List<Map<String, Object>> list = dbadapter.get(table, fieldMap);
		if (list.size() > 0)
			return list.get(0);
		else
			return null;
	}

	/**
	 * 获取表名
	 * 
	 * @param url
	 * @return
	 */
	private String getTableName(String url) {
		if (url.indexOf("//") < 0 && url.lastIndexOf("/") < 0)
			throw new CtRuntimeException("getTableName: url格式错误，未找到“//”或“/”");
		int start = url.indexOf("//") + 2;
		int end = url.lastIndexOf("/");
		if (end < start + 2)
			throw new CtRuntimeException("getTableName: url格式错误，未找到结束符“/”");
		String table = url.substring(start, end);
		return table;
	}

	/**
	 * 获取指令
	 * 
	 * @param url
	 * @return
	 */
	private String getCommand(String url) {
		if (url.lastIndexOf("/") < 0 && url.indexOf("?") < 0)
			throw new CtRuntimeException("getCommand: url格式错误，未找到“/”或“?”");
		int start = url.lastIndexOf("/") + 1;
		int end = url.indexOf("?");
		String cmd;
		if (end >= 0)
			cmd = url.substring(start, end);
		else
			cmd = url.substring(start);
		return cmd;
	}

	/**
	 * 获取字段MAP
	 * 
	 * @param url
	 * @return
	 */
	private Map<String, Object> getFieldMap(String url) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (url.indexOf("?") < 0) {
			return result;
			// throw new CtRuntimeException("getFieldMap: url格式错误，未找到“?”");
		}
		String fieldStr = "";
		if (url.indexOf("?") + 1 < url.length()) {
			fieldStr = url.substring(url.indexOf("?") + 1);
		}
		if (fieldStr.length() > 0) {
			if (fieldStr.indexOf("&") > -1) {
				String[] fieldArray = fieldStr.split("&");
				for (int i = 0; i < fieldArray.length; i++) {
					if (fieldArray[i].indexOf("=") > -1) {
						String[] _fieldArray = fieldArray[i].split("=");
						if (_fieldArray.length > 1) {
							result.put(_fieldArray[0],
									UserAppSession.urlDecode(_fieldArray[1]));
						} else {
							result.put(_fieldArray[0], "");
						}
					}
				}
			} else {
				if (fieldStr.indexOf("=") > -1) {
					String[] _fieldArray = fieldStr.split("=");
					if (_fieldArray.length > 1) {
						result.put(_fieldArray[0],
								UserAppSession.urlDecode(_fieldArray[1]));
					} else {
						result.put(_fieldArray[0], "");
					}
				}
			}
		}

		return result;
	}

	public void close() {
		if (dbadapter != null)
			dbadapter.close();
		dbadapter = null;
	}
}
