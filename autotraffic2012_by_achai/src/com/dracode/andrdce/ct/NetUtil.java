package com.dracode.andrdce.ct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetUtil {
	public static List<String> recentNetLogs = new ArrayList<String>();
	public static String lastNetResult = "";
	public static boolean netReqLogEnabled = true;

	public static class NetReqInfo {
		String url;
		Object netobj;
		Thread thr;

		public void stop() {
			if (netobj instanceof HttpURLConnection) {
				Log.d("dce net", "kill http " + url);
				HttpURLConnection http = (HttpURLConnection) netobj;
				http.disconnect();
				thr.interrupt();
			}
		}
	}

	private static List<NetReqInfo> autoReleaseNetObjs = new ArrayList<NetReqInfo>();

	public static synchronized void addAutoReleaseNetObj(String url,
			Object obj, Thread thr) {
		int idx = -1;
		for (int i = 0; i < autoReleaseNetObjs.size(); i++)
			if (autoReleaseNetObjs.get(i).netobj == obj) {
				idx = i;
				break;
			}
		if (idx == -1) {
			NetReqInfo info = new NetReqInfo();
			info.url = url;
			info.netobj = obj;
			info.thr = thr;
			autoReleaseNetObjs.add(info);
		}
	}

	public static synchronized void removeAutoReleaseNetObj(Object obj) {
		int idx = -1;
		for (int i = 0; i < autoReleaseNetObjs.size(); i++)
			if (autoReleaseNetObjs.get(i).netobj == obj) {
				idx = i;
				break;
			}
		if (idx >= 0)
			autoReleaseNetObjs.remove(idx);
	}

	public static synchronized void releaseNetObjs() {
		try {
			for (NetReqInfo netinfo : autoReleaseNetObjs) {
				try {
					netinfo.stop();
				} catch (Throwable e) {
				}
			}
			autoReleaseNetObjs.clear();
		} catch (Throwable ex) {
		}
	}

	public static synchronized void stopNetUrl(String url) {
		try {
			for (int i = 0; i < autoReleaseNetObjs.size(); i++)
				if (autoReleaseNetObjs.get(i).url.equals(url)) {
					try {
						autoReleaseNetObjs.get(i).stop();
					} catch (Throwable e) {
					}
				}
		} catch (Throwable ex) {
		}
	}

	private static String getExceptionTraceStack(Throwable ex) {
		ByteArrayOutputStream deo = new ByteArrayOutputStream();
		PrintStream de = new PrintStream(deo);
		ex.printStackTrace(de);
		String err = new String(deo.toByteArray());
		return err;
	}

	public static String getEncryptKey(String url) {
		DceJni jni = DceJni.getDceJni();
		int po = url.indexOf('?');
		String s;
		if (po >= 0)
			s = url.substring(po + 1);
		else
			s = url;
		String res = jni.jniCall("1", s);
		return res;
	}

	/**
	 * @Title: checkNetworkType
	 * @Description: 获取接入点
	 * @param ctx
	 *            上下文
	 */
	public static String checkNetworkType(Context ctx) {
		String typeName = "";
		ConnectivityManager connectivity = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null)
			return "";

		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info == null) {
			return typeName;
		}
		if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			typeName = info.getExtraInfo().toLowerCase();
		} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			/* Log.d(TAG, "网络类型为WIFI"); */
			typeName = "wifi";
		} else {
			typeName = "";
		}
		return typeName;
	}

	// 提交URL
	public static String getUrlData(String url) {
		return getUrlData(url, null, null);
	}

	public static InputStream getUrlStreamData(String url) {
		return getUrlStreamData(url, null, null);
	}

	// 提交URL
	public static String getUrlData(String url, UserAppSession us) {
		return getUrlData(url, null, us);
	}

	public static InputStream getUrlStreamData(String url, UserAppSession us) {
		return getUrlStreamData(url, null, us);
	}

	// 提交URL返回字符串的统一接口
	public static String getUrlData(String url, Map<String, Object> params,
			UserAppSession us) {
		return (String) getUrlDataEx(url, params, us, false, true);
	}

	public static InputStream getUrlStreamData(String url,
			Map<String, Object> params, UserAppSession us) {
		return (InputStream) getUrlDataEx(url, params, us, true, true);
	}

	public static Object getUrlDataEx(String url, Map<String, Object> params,
			UserAppSession us, boolean needStream, boolean canRetry) {
		long tm = 0;
		if (canRetry)
			if (isNetLogEnabled()) {
				clearNetLog();
				addNetLog(UserAppSession.getCurTimeStr()
						+ " getting url data: " + url);
				tm = System.currentTimeMillis();
			}

		String postData = "";
		try {
			if (params != null)
				for (String k : params.keySet()) {
					Object v = params.get(k);
					postData += "&" + k + "="
							+ URLEncoder.encode(v.toString(), HTTP.UTF_8);
				}
		} catch (Throwable e) {
			if (isNetLogEnabled()) {
				addNetLog("输入参数出错:");
				addNetLog(getExceptionTraceStack(e));
			}
			throw new CtRuntimeException("输入参数出错:" + e.getMessage());
		}

		HttpURLConnection httpConn = null;
		InputStream inStrm, resStrm = null;
		URL urlo;
		boolean needRetry = false;
		String res = "";
		try {

			try {
				urlo = new URL(url);
				if ("cmwap".equals(UserAppSession.networkType.trim()
						.toLowerCase())) {
					Proxy proxy = new Proxy(Proxy.Type.HTTP,
							new InetSocketAddress("10.0.0.172", 80));
					httpConn = (HttpURLConnection) urlo.openConnection(proxy);
				} else
					httpConn = (HttpURLConnection) urlo.openConnection();
				addAutoReleaseNetObj(url, httpConn, Thread.currentThread());

				if (us != null && us.sessionId != null
						&& us.sessionId.length() > 0) {
					httpConn.addRequestProperty("Cookie", "JSESSIONID="
							+ us.sessionId);
				}

				if (postData.length() > 0) {
					httpConn.setDoOutput(true);
					// 设置以POST方式
					httpConn.setRequestMethod("POST");
					// Post 请求不能使用缓存
					httpConn.setUseCaches(false);
					httpConn.setInstanceFollowRedirects(true);
					// 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
					httpConn.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
				}
				// 设置请求超时
				httpConn.setConnectTimeout(UserAppSession.timeoutConnection);
				httpConn.setReadTimeout(UserAppSession.timeoutSocket);

				if (isNetLogEnabled()) {
					Map<String, List<String>> props = httpConn
							.getRequestProperties();
					addNetLog("Request Headers:");
					for (String k : props.keySet()) {
						List<String> vs = props.get(k);
						String v;
						if (vs == null || vs.size() == 0)
							v = "";
						else if (vs.size() == 1)
							v = vs.get(0);
						else
							v = vs.toString();
						addNetLog(k + "=" + v);
					}
					if (postData.length() > 0) {
						addNetLog("Request Content:");
						addNetLog(postData);
					}
				}

				httpConn.connect();

			} catch (Throwable e) {
				if (isNetLogEnabled()) {
					addNetLog("连接失败:");
					addNetLog(getExceptionTraceStack(e));
				}
				UserAppSession.cursession().checkNetworkType();
				if (e instanceof SocketTimeoutException)
					throw new CtRuntimeException("连接失败: 连接服务器超时");
				else
					throw new CtRuntimeException("连接失败:" + e.getMessage());
			}

			if (postData.length() > 0) {
				try {
					// DataOutputStream流
					DataOutputStream out = new DataOutputStream(
							httpConn.getOutputStream());
					// 要上传的参数
					String content = postData;
					// 将要上传的内容写入流中
					out.writeBytes(content);
					// 刷新、关闭
					out.flush();
					out.close();
				} catch (Throwable e) {
					if (isNetLogEnabled()) {
						addNetLog("提交数据出错:");
						addNetLog(getExceptionTraceStack(e));
					}
					if (e instanceof SocketTimeoutException)
						throw new CtRuntimeException("提交数据出错: 提交超时");
					else
						throw new CtRuntimeException("提交数据出错:" + e.getMessage());
				}
			}

			String charset = "utf-8";
			String contentType = "";
			int statusCode = 0;
			try {
				// 若状态码为200 ok
				statusCode = httpConn.getResponseCode();
				if (statusCode == HttpURLConnection.HTTP_OK || statusCode == -1) {
					// 取得HTTP response
					if (us != null) {
						Map<String, List<String>> headerFields = httpConn
								.getHeaderFields();
						for (String k : headerFields.keySet()) {
							if ("content-type".equalsIgnoreCase(k)) {
								String v = headerFields.get(k).toString()
										.toLowerCase();
								contentType = v;
								if (v.startsWith("[") && v.endsWith("]"))
									v = v.substring(1, v.length() - 1);
								int p1 = v.indexOf("charset=");
								if (p1 >= 0) {
									p1 += "charset=".length();
									int p2 = v.indexOf(";", p1);
									if (p2 > p1)
										charset = v.substring(p1, p2);
									else
										charset = v.substring(p1);
								}
							}
							if ("set-cookie".equalsIgnoreCase(k)) {
								String v = headerFields.get(k).toString();
								if (v.startsWith("[") && v.endsWith("]"))
									v = v.substring(1, v.length() - 1);
								int p1 = v.toUpperCase().indexOf("JSESSIONID=");
								if (p1 >= 0) {
									p1 += "JSESSIONID=".length();
									int p2 = v.indexOf(";", p1);
									if (p2 > p1)
										us.sessionId = v.substring(p1, p2);
									else
										us.sessionId = v.substring(p1);
								}
							}
						}
					}

				} else if (statusCode != 400)
					throw new Exception("响应代码错误" + Integer.toString(statusCode));

				inStrm = httpConn.getInputStream();
				if (inStrm == null) {
					throw new Exception("无法获取结果流");
				}
			} catch (Throwable e) {
				if (isNetLogEnabled()) {
					addNetLog("打开数据出错:");
					addNetLog(getExceptionTraceStack(e));
				}
				if (e instanceof SocketTimeoutException)
					throw new CtRuntimeException("打开数据失败: 读取超时");
				else
					throw new CtRuntimeException("打开数据失败:" + e.getMessage());
			}

			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte buf[] = new byte[1024 * 8];
				while (true) {
					int c = inStrm.read(buf);
					if (c == -1)
						break;
					out.write(buf, 0, c);
				}
				byte[] bs = out.toByteArray();
				if (!needStream || isNetLogEnabled())
					res = res + (new String(bs, charset));
				if (isNetLogEnabled()) {
					tm = System.currentTimeMillis() - tm;
					addNetLog("Response Time: " + Long.toString(tm));
					addNetLog("Response Size: " + bs.length);
					addNetLog("Response Header:");
					Map<String, List<String>> headerFields = httpConn
							.getHeaderFields();
					for (String k : headerFields.keySet()) {
						String v = headerFields.get(k).toString();
						addNetLog(k + "=" + v);
					}
					addNetLog("Response Content:");
					addNetLog(res);
					addNetLog("");
					addNetLog("");
				}
				out.close();
				if (inStrm != null)
					inStrm.close();
				if (statusCode == 400 || statusCode == 500)
					throw new Exception("响应代码错误" + Integer.toString(statusCode));
				if (canRetry)
					if (contentType.toLowerCase().contains("wap.wmlc"))
						needRetry = true;

				if (needStream && !needRetry) {
					resStrm = new ByteArrayInputStream(bs);
				}
			} catch (Throwable e) {
				addNetLog("获取出错:");
				addNetLog(getExceptionTraceStack(e));
				throw new CtRuntimeException("获取数据出错:" + e.getMessage());
			}
		} finally {
			try {
				if (httpConn != null) {
					httpConn.disconnect();
					removeAutoReleaseNetObj(httpConn);
				}
			} catch (Throwable te) {
			}
		}
		if (needRetry)
			return getUrlDataEx(url, params, us, needStream, false);
		else if (needStream)
			return resStrm;
		else
			return res;
	}

	public static boolean isNetLogEnabled() {
		return netReqLogEnabled;
	}

	public static void addNetLog(String msg) {
		synchronized (lastNetResult) {
			lastNetResult = lastNetResult + msg + "\n";
		}
	}

	public static void clearNetLog() {
		synchronized (lastNetResult) {
			while (recentNetLogs.size() > 10)
				recentNetLogs.remove(0);
			if (lastNetResult.length() > 0)
				recentNetLogs.add(lastNetResult);
			lastNetResult = "";
		}
	}

	public static void resetNetLogs() {
		synchronized (lastNetResult) {
			recentNetLogs.clear();
			lastNetResult = "";
		}
	}

	// 提交URL返回JSON对象的统一接口
	public static JSONObject getUrlJsonData(String url,
			Map<String, Object> params, UserAppSession us, long expireTm) {
		return getUrlJsonDataEx("url_json_data", null, url, params, us,
				expireTm, 0);
	}

	public static JSONObject getUrlJsonDataEx(String cacheTable,
			String cacheType, String url, Map<String, Object> params,
			UserAppSession us, long expireTm, int cacheMode) {
		// cacheMode:
		// 0-正常读写缓存；1只读取缓存；2强制读取失效的缓存；3强制重新更新缓存；4使用HASH值比对更新；5使用HASH值正常读写缓存
		boolean bNetReqed = false;
		boolean bCacheExpired = false;
		String jsonStr = null, cacheStr = null;
		String keyUrl = url;
		JSONObject jsonObj, cachedObj = null;
		String vKeyName = "&verifyCode=";
		int p1 = keyUrl.indexOf(vKeyName);
		if (p1 > 0)
			keyUrl = keyUrl.substring(0, p1);

		// 尝试从缓存获取
		if (expireTm > 0 && cacheMode != 3) {
			// 获取缓存记录
			Map<String, Object> m = us.getCacheUtil().getMapFromCacheEx(
					cacheTable, keyUrl);
			if (m == null) {
				jsonStr = null;
				bCacheExpired = true;
			} else {
				// 比对缓存失效时间
				long tm = TypeUtil
						.ObjectToLong(m.get(CacheUtil.FIELD_EXPIRETM));
				if (tm > 0 && tm < System.currentTimeMillis())
					bCacheExpired = true;
				// 如果强制要用缓存，则不管失效时间
				if (cacheMode == 2)
					bCacheExpired = false;
				// 获取缓存内容
				if (bCacheExpired && cacheMode == 0)
					jsonStr = null;
				else {
					byte[] value = (byte[]) m.get(CacheUtil.FIELD_BLOBVAL);
					if (value != null) {
						cacheStr = (String) CacheUtil.bytesToObj(value);
						jsonStr = cacheStr;
					}
				}
			}

			// 写日志
			if (jsonStr != null) {
				addNetLog(UserAppSession.getCurTimeStr()
						+ " load url from cache: sz"
						+ Integer.toString(jsonStr.length()) + "  " + url);
				if (bCacheExpired)
					addNetLog("cache url is expired!");
				addNetLog(jsonStr);
			}
		}

		// 是否从服务器获取
		if (cacheMode == 1) {
			if (jsonStr == null || bCacheExpired)
				return null;
		} else if (jsonStr == null) { // 没有缓存，或缓存失效，必须重新下载
			jsonStr = getUrlData(url, params, us);
			bNetReqed = true;
		} else if (cacheMode == 4) { // HASH值更新，必须重新比对下载
			String hash = "";
			try {
				cachedObj = new JSONObject(jsonStr);
				if (cachedObj.has("hash"))
					hash = cachedObj.getString("hash");
			} catch (JSONException e) {
			}
			url += "&hash=" + hash;
			jsonStr = getUrlData(url, params, us);
			bNetReqed = true;
		} else if (cacheMode == 5) { // 有HASH值
			// 有HASH值但缓存有效，跳过
			if (!bCacheExpired) {
				//
			} else {
				// 有HASH值但缓存失效，尝试重新比对下载
				String hash = "";
				try {
					cachedObj = new JSONObject(jsonStr);
					if (cachedObj.has("hash"))
						hash = cachedObj.getString("hash");
				} catch (JSONException e) {
				}
				url += "&hash=" + hash;
				try {
					jsonStr = getUrlData(url, params, us);
					bNetReqed = true;
				} catch (Throwable ex) {
					// 尝试重新下载失败，还是用缓存吧
					ex.printStackTrace();
					jsonStr = cacheStr;
				}
			}
		}

		// 分析结果返回
		try {
			jsonObj = new JSONObject(jsonStr);
			if (jsonObj.has("errorMsg"))
				throw new CtRuntimeException(jsonObj.getString("errorMsg"));
			if (cacheMode == 4 && cachedObj != null) {
				if (jsonObj.has("resultCode"))
					if ("55555".equals(jsonObj.getString("resultCode")))
						return cachedObj;
			}
			if (expireTm > 0 && bNetReqed) {
				us.putToCache(cacheTable, cacheType, keyUrl, jsonStr, expireTm);// 写入缓存
			}
			return jsonObj;
		} catch (JSONException e) {
			throw new CtRuntimeException("处理数据出错:服务返回数据的格式无法解释");
		}
	}

	public static String getLastNetResult() {
		String res = "";
		for (String log : recentNetLogs)
			res += log + "\n\n";
		res += lastNetResult;
		return res;
	}

}
