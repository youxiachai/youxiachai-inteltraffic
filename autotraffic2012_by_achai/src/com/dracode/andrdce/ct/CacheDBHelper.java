package com.dracode.andrdce.ct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * @ClassName: CacheDBHelper
 * @Description: 数据库操作类
 * @author Figo.Gu
 * @date 2012-2-27 下午09:20
 */
public class CacheDBHelper extends SQLiteOpenHelper {
	/** 调试标签. */
	private static final String TAG = CacheDBHelper.class.getSimpleName();
	/** 数据库名. */
	private static final String DATABASE_NAME = "andrdce";
	/** 数据库版本. */
	private static final int DATABASE_VERSION = 8;
	/**  .*/
	protected SQLiteDatabase sdb;	

	public CacheDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		doOpenDatabase(context, null);
	}

	public CacheDBHelper(Context context, String name, CursorFactory factory,  int version) {
        super(context, name, factory, version);  
		doOpenDatabase(context, factory);
    }  

	protected void doOpenDatabase(Context context, CursorFactory factory) {
		// 开启数据库
		if(sdb!=null)
			return;
		try {
			sdb = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_WORLD_WRITEABLE, factory);
		} catch (Exception e) {
			throw new CtRuntimeException("创建或开启数据库失败！");
		}
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*
		db.execSQL("DROP TABLE IF EXISTS " + NearbySortInfo.TABLE_NEARBY_SORT_IMG_CACHE);
		db.execSQL("DROP TABLE IF EXISTS " + NearbySortInfo.TABLE_NEARBY_SORT_INFO);
		
		if (!UserAppSession.cursession().isCacheTableInited(
				NearbySortInfo.TABLE_NEARBY_SORT_INFO)) {
			String url = "sqldb://" + NearbySortInfo.TABLE_NEARBY_SORT_INFO + "/init?id=s";
			url += "&hash_val=s";
			url += "&json_def=s";
			UserAppSession.cursession().executeCacheUrl(url);
		}
		if (!UserAppSession.cursession().isCacheTableInited(
				NearbySortInfo.TABLE_NEARBY_SORT_IMG_CACHE)) {
			String url = "sqldb://" + NearbySortInfo.TABLE_NEARBY_SORT_IMG_CACHE
					+ "/init?id=s";
			url += "&blob_cache=b";
			UserAppSession.cursession().executeCacheUrl(url);
		}*/
	}

	/**
	 * 检测表
	 * @return
	 */
	public boolean checkTable(String tableName){
		StringBuilder sql = new StringBuilder();
		Cursor cursor = null;
	
		sql.append("select count(*) from ");
		sql.append(tableName);
		try {
			cursor = sdb.rawQuery(sql.toString(), null);
		} catch (Exception e) {
			return false;
		}
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		
		cursor.close();
		
		if (count > 0) return true;
		else return false;
	}
	
	/**
	 * @Title: createTable
	 * @Description: 创建表
	 */
	public void createTable(String table, Map<String,Object> fieldMap) {
		StringBuilder sql = new StringBuilder();
		
		sdb.beginTransaction();// 开始事务
		try {
			sql.append("CREATE TABLE IF NOT EXISTS ").append(table);
			sql.append("(");
			sql.append("AUTO_ID").append(" integer PRIMARY KEY autoincrement,");
			
			if (fieldMap == null) {
				fieldMap = new HashMap<String, Object>();
			}
			for(String k:fieldMap.keySet()){
				Object v = fieldMap.get(k);
				String s=TypeUtil.ObjectToString(v).toLowerCase();
	            if ("integer".equals(s)||"i".equals(s)) {
					sql.append(k).append(" integer,");
				} else if ("string".equals(s)||"s".equals(s)) {
					sql.append(k).append(" text,");
				} else if ("float".equals(s)||"f".equals(s)) {
					sql.append(k).append(" real,");
				} else if ("date".equals(s)||"d".equals(s)) {
					sql.append(k).append(" integer,");
				} else if ("blob".equals(s)||"b".equals(s)) {
					sql.append(k).append(" blob,");
				}
			}
			sql.append(");");
			String sqlStr = sql.toString().replaceFirst(",\\)", "\\)");
			sdb.execSQL(sqlStr);
			sdb.setTransactionSuccessful();// 事务提交成功
		} catch (Exception e) {
			e.printStackTrace();
			throw new CtRuntimeException("创建表出错："+e.getMessage() + "," + sql.toString());
		} finally {
			sdb.endTransaction();// 事务结束
		}
	}
	
	/**
	 * @Title: insertData
	 * @Description: 插入数据
	 */
	public void insert(String table, Map<String,Object> fieldMap) {
		ContentValues cv = new ContentValues();

		if (fieldMap == null) {
			fieldMap = new HashMap<String, Object>();
		}
		for(String k:fieldMap.keySet()){
			Object v = fieldMap.get(k);
			if (v instanceof Integer) {
				int value = ((Integer) v).intValue();
				cv.put(k, value);
			} else if (v instanceof String) {
				String value = (String) v;
				cv.put(k, value);
			} else if (v instanceof Double) {
				double value = ((Double) v).doubleValue();
				cv.put(k, value);
			} else if (v instanceof Float) {
				float value = ((Float) v).floatValue();
				cv.put(k, value);
			} else if (v instanceof Long) {
				long value = ((Long) v).longValue();
				cv.put(k, value);
			} else if (v instanceof Short) {
				Short value = (Short) v;
				cv.put(k, value);
			} else if (v instanceof Byte) {
				Byte value = (Byte) v;
				cv.put(k, value);
			} else if (v instanceof byte[]) {
				byte[] value = (byte[]) v;
				cv.put(k, value);
			}
		}
		sdb.beginTransaction();// 开始事务
		try {
			sdb.insert(table, null, cv);
			sdb.setTransactionSuccessful();// 事务提交成功
		} catch (Exception e) {
			e.printStackTrace();
			throw new CtRuntimeException("插入数据出错：" + e.getMessage() + ",表名：" + table);
		} finally {
			sdb.endTransaction();// 事务结束
		}
	}
	
	/**
	 * @Title: update
	 * @Description: 修改数据
	 * @param table 表名
	 * @param fieldMap 更新的字段
	 * @param whereFieldMap 查询条件
	 */
	public int update(String table, Map<String,Object> fieldMap, Map<String,Object> whereFieldMap) {
		ContentValues cv = new ContentValues();
		String whereClause = null;
		List<String> whereArgs=new ArrayList<String>();
		int resid = 0;
		
		if (fieldMap == null) {
			fieldMap = new HashMap<String, Object>();
		}
		if (whereFieldMap == null) {
			whereFieldMap = new HashMap<String, Object>();
		}

		for(String k : fieldMap.keySet()){
			Object v = fieldMap.get(k);
            
            if (v instanceof Integer) {
				int value = ((Integer) v).intValue();
				cv.put(k, value);
			} else if (v instanceof String) {
				String value = (String) v;
				cv.put(k, value);
			} else if (v instanceof Double) {
				double value = ((Double) v).doubleValue();
				cv.put(k, value);
			} else if (v instanceof Float) {
				float value = ((Float) v).floatValue();
				cv.put(k, value);
			} else if (v instanceof Long) {
				long value = ((Long) v).longValue();
				cv.put(k, value);
			} else if (v instanceof Short) {
				Short value = (Short) v;
				cv.put(k, value);
			} else if (v instanceof Byte) {
				Byte value = (Byte) v;
				cv.put(k, value);
			} else if (v instanceof byte[]) {
				byte[] value = (byte[]) v;
				cv.put(k, value);
			}
		}
		
		for(String k : whereFieldMap.keySet()){
			Object v = whereFieldMap.get(k);
            if (whereClause == null) whereClause = "";
            if (whereClause.indexOf("?") > -1) {
            	whereClause += " and ";
            }
            
            if (v instanceof Integer) {
				int value = ((Integer) v).intValue();
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof String) {
				String value = (String) v;
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Double) {
				double value = ((Double) v).doubleValue();
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Float) {
				float value = ((Float) v).floatValue();
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Long) {
				long value = ((Long) v).longValue();
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Short) {
				Short value = (Short) v;
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Byte) {
				Byte value = (Byte) v;
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			}
		}
		sdb.beginTransaction();// 开始事务
		try {
			resid = sdb.update(table, cv, whereClause, getStringArray(whereArgs));
			sdb.setTransactionSuccessful();// 事务提交成功
		} catch (Exception e) {
			e.printStackTrace();
			throw new CtRuntimeException("修改数据出错：" + e.getMessage() + ",表名：" + table);
		} finally {
			sdb.endTransaction();// 事务结束
		}
		return resid;
	}
	
	/**
	 * @Title: get
	 * @Description: 获取数据
	 */
	public List<Map<String, Object>> get(String table, Map<String, Object> fieldMap) {
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		Map<String, Object> entity = null;
		Cursor c = null;
		List<String> whereArgs=new ArrayList<String>();

		if (fieldMap == null) {
			fieldMap = new HashMap<String, Object>();
		}
		StringBuffer sql = new StringBuffer();
		String selFds=(String) fieldMap.get("SelectFields");
		if(selFds==null)
			selFds="*";
		String where=(String) fieldMap.get("WhereClause");
		if(where==null)
			where="1=1";
		sql.append("select ").append(selFds);
		sql.append(" from " + table);
		sql.append(" where "+where+" ");
		
		for(String k:fieldMap.keySet()){
			Object v = fieldMap.get(k);
            if(k.equals("SelectFields") || k.equals("OrderBy") || k.equals("MaxRowCount")||k.equals("WhereClause"))
            	continue;
            
            if (v instanceof Byte) {
            	Log.e(TAG, "不允许使用流数据作为条件查询！");
            	return null;
			}
            
            if (v instanceof Integer) {
				int value = ((Integer) v).intValue();
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof String) {
				String value = (String) v;
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Double) {
				double value = ((Double) v).doubleValue();
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Float) {
				float value = ((Float) v).floatValue();
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Long) {
				long value = ((Long) v).longValue();
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Short) {
				Short value = (Short) v;
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Byte) {
				Byte value = (Byte) v;
				whereArgs.add(String.valueOf(value));
			}
            
            sql.append(" and " + k + "=? ");
		}
		if (fieldMap.get("OrderBy") != null) {
			sql.append(" order by " + fieldMap.get("OrderBy").toString());
		}
		if (fieldMap.get("MaxRowCount") != null) {
			sql.append(" limit " + fieldMap.get("MaxRowCount").toString());
		}
		c = sdb.rawQuery(sql.toString(), getStringArray(whereArgs));

		c.moveToFirst();
		while (c.getPosition() != c.getCount()) {
			entity = new HashMap<String, Object>();

			for (int i = 0; i < c.getColumnCount(); i++) {
				String n = c.getColumnName(i);
				if(n.startsWith("blob"))
					entity.put(n, c.getBlob(i));
				else
					entity.put(n, c.getString(i));
			}
			dataList.add(entity);
			
			c.moveToNext();
		}
		c.close();

		return dataList;
	}
	
	/**
	 * @Title: delete
	 * @Description: 删除数据
	 */
	public void delete(String table, Map<String,Object> fieldMap) {
		String whereClause = null;
		List<String> whereArgs=new ArrayList<String>();
		
		if (fieldMap == null) {
			fieldMap = new HashMap<String, Object>();
		}
		for(String k:fieldMap.keySet()){
			Object v = fieldMap.get(k);
            
            if (whereClause == null) whereClause = "";
            if (whereClause.indexOf("?") > -1) {
            	whereClause += " and ";
            }
            
            if (v instanceof Integer) {
				int value = ((Integer) v).intValue();
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof String) {
				String value = (String) v;
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Double) {
				double value = ((Double) v).doubleValue();
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Float) {
				float value = ((Float) v).floatValue();
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Long) {
				long value = ((Long) v).longValue();
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			} else if (v instanceof Short) {
				Short value = (Short) v;
				whereClause += k + "=? ";
				whereArgs.add(String.valueOf(value));
			}
		}

		sdb.beginTransaction();// 开始事务
		try {
			sdb.delete(table, whereClause, getStringArray(whereArgs));
			sdb.setTransactionSuccessful();// 事务提交成功
		} catch (Exception e) {
			e.printStackTrace();
			throw new CtRuntimeException("插入数据出错：" + e.getMessage() + ",表名：" + table);
		} finally {
			sdb.endTransaction();// 事务结束
		}
	}

	private String[] getStringArray(List<String> strls) {
		String[] res=new String[strls.size()];
		for(int i=0;i<strls.size();i++)
			res[i]=strls.get(i);
		return res;
	}

	@Override
	public synchronized void close() {
		if(sdb!=null)
			sdb.close();
		sdb=null;
		super.close();
	}
	
}
