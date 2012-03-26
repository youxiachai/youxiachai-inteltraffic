package com.dracode.andrdce.ctact;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dracode.andrdce.ct.CtRuntimeException;
import com.dracode.andrdce.ct.NetUtil;
import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;

public class CtImageHelper {

	public static final String TABLE_CT_IMG_CACHE = "ct_img_cache";

	public String cacheTableName = TABLE_CT_IMG_CACHE;

	public static Map<String, Object> loadImgsFromCache(Object dataObj) {
		CtImageHelper cih = new CtImageHelper();
		return cih.doLoadImgsFromCache(dataObj);
	}

	/**
	 * 加载图片缓存
	 */
	public Map<String, Object> doLoadImgsFromCache(Object dataObj) {
		Map<String, Object> resMap = new HashMap<String, Object>();

		if (!UserAppSession.cursession().isCacheTableInited(cacheTableName)) {
			return resMap;
		}

		if (dataObj instanceof List) {
			List<Map<String, Object>> dataList = TypeUtil
					.CastToList_SO(dataObj);
			for (Map<String, Object> si : dataList)
				loadImgsOfMapFromCache(si, resMap);
		}
		if (dataObj instanceof Map) {
			Map<String, Object> dataMap = TypeUtil.CastToMap_SO(dataObj);
			loadImgsOfMapFromCache(dataMap, resMap);
		}

		return resMap;
	}

	private void loadImgsOfMapFromCache(Map<String, Object> dataMap,
			Map<String, Object> res) {
		if (dataMap == null)
			return;
		for (String k : dataMap.keySet()) {
			Object o = dataMap.get(k);
			if (o instanceof String && k.startsWith("IMGURL")) {
				String imgurl = TypeUtil.ObjectToString(o);
				if (imgurl != null && imgurl.length() > 0)
					loadImageOfUrlToCache(imgurl, res);
			}
			if (o instanceof Map) {
				loadImgsOfMapFromCache(TypeUtil.CastToMap_SO(o), res);
			}
			if (o instanceof List) {
				List<Map<String, Object>> dataList = TypeUtil.CastToList_SO(o);
				for (Map<String, Object> si : dataList)
					loadImgsOfMapFromCache(si, res);
			}
		}
	}

	private void loadImageOfUrlToCache(String imgurl, Map<String, Object> res) {
		String url = "sqldb://" + cacheTableName + "/getobj?id="
				+ UserAppSession.urlEncode(imgurl);
		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof Map) {
			Map<String, Object> m = TypeUtil.CastToMap_SO(o);
			res.put(imgurl, m.get("blob_cache"));
		}
	}

	/**
	 * 图片缓存
	 */
	public void downloadImgsToCache(Object dataObj) {
		if (!UserAppSession.cursession().isCacheTableInited(cacheTableName)) {
			String url = "sqldb://" + cacheTableName + "/init?id=s";
			url += "&blob_cache=b";
			UserAppSession.cursession().executeCacheUrl(url);
		}
		if (dataObj instanceof List) {
			List<Map<String, Object>> dataList = TypeUtil
					.CastToList_SO(dataObj);
			for (Map<String, Object> si : dataList)
				saveImgsOfMapToCache(si);
		}
		if (dataObj instanceof Map) {
			Map<String, Object> dataMap = TypeUtil.CastToMap_SO(dataObj);
			saveImgsOfMapToCache(dataMap);
		}
	}

	private void saveImgsOfMapToCache(Map<String, Object> dataMap) {
		if (dataMap == null)
			return;
		for (String k : dataMap.keySet()) {
			Object o = dataMap.get(k);
			if (o instanceof String && k.startsWith("IMGURL")) {
				String imgurl = TypeUtil.ObjectToString(o);
				if (imgurl != null && imgurl.length() > 0)
					saveImageOfUrlToCache(imgurl);
			}
			if (o instanceof Map) {
				saveImgsOfMapToCache(TypeUtil.CastToMap_SO(o));
			}
			if (o instanceof List) {
				List<Map<String, Object>> dataList = TypeUtil.CastToList_SO(o);
				for (Map<String, Object> si : dataList)
					saveImgsOfMapToCache(si);
			}
		}
	}

	public void saveImageOfUrlToCache(String imgurl) {
		Map<String, Object> fieldMap;
		InputStream inStrm = NetUtil.getUrlStreamData(imgurl, UserAppSession.cursession());
		if (inStrm == null)
			throw new CtRuntimeException("下载图像内容失败");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte buf[] = new byte[1024 * 8];
		while (true) {
			int c;
			try {
				c = inStrm.read(buf);
				if (c == -1)
					break;
				out.write(buf, 0, c);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte[] bs = out.toByteArray();
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String url = "sqldb://" + cacheTableName + "/put";
		fieldMap = new HashMap<String, Object>();
		fieldMap.put("id", imgurl);
		fieldMap.put("blob_cache", bs);
		UserAppSession.cursession().executeCacheUrl(url, fieldMap);
	}

}
