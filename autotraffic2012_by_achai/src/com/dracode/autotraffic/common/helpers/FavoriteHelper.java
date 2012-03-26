package com.dracode.autotraffic.common.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.andrdce.ct.UserAppSession;

public class FavoriteHelper {

	public static final String TABLE_USER_FAVORITE_INFO = "user_favorite_info";

	public static interface OnFavoriteChosenEvent {
		public void onFavoriteChosen(Favorite fav);
	}

	// Favorite(收藏)
	// ---------------
	// id String
	// type String
	// city String
	// name String
	// value String
	// memo String
	// user String
	// fav_time String
	// param1 String
	// param2 String
	// param3 String
	// param4 String
	// param5 String
	// param6 String
	// param7 String
	// param8 String
	// param9 String

	/* 收藏 */
	public static class Favorite {
		protected String id;
		protected String type;
		protected String city;
		protected String name;
		protected String value;
		protected String memo;
		protected String user;
		protected String fav_time;
		protected String param1;
		protected String param2;
		protected String param3;
		protected String param4;
		protected String param5;
		protected String param6;
		protected String param7;
		protected String param8;
		protected String param9;

		public Favorite() {
		}

		public Favorite(String type2, String city2, String name2,
				String value2, String user2) {
			id = getFavoriteItemId(type2, city2, name2, user2);
			type = type2;
			city = city2;
			name = name2;
			value = value2;
			user = user2;
			fav_time = UserAppSession.getCurTimeStr();
		}

		public String getCity() {
			return city;
		}

		public void setCity(String value) {
			this.city = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String value) {
			this.name = value;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getMemo() {
			return memo;
		}

		public void setMemo(String value) {
			this.memo = value;
		}

		public static String getLoadCacheUrl(String type, String city,
				String user) {
			return "sqldb://" + TABLE_USER_FAVORITE_INFO + "/get?type="
					+ UserAppSession.urlEncode(type) + "&city="
					+ UserAppSession.urlEncode(city) + "&user="
					+ UserAppSession.urlEncode(user)
					+ "&MaxRowCount=50&OrderBy=fav_time desc";
		}

		@Override
		public String toString() {
			return getName();
		}

		public void saveToCache() {
			if (!UserAppSession.cursession().isCacheTableInited(
					TABLE_USER_FAVORITE_INFO)) {
				String url = "sqldb://" + TABLE_USER_FAVORITE_INFO
						+ "/init?id=s";
				url += "&type=s";
				url += "&city=s";
				url += "&name=s";
				url += "&value=s";
				url += "&memo=s";
				url += "&user=s";
				url += "&fav_time=s";
				url += "&param1=s";
				url += "&param2=s";
				url += "&param3=s";
				url += "&param4=s";
				url += "&param5=s";
				url += "&param6=s";
				url += "&param7=s";
				url += "&param8=s";
				url += "&param9=s";
				UserAppSession.cursession().executeCacheUrl(url);
			}

			String url = "sqldb://" + TABLE_USER_FAVORITE_INFO + "/put";
			Map<String, Object> fieldMap = new HashMap<String, Object>();
			fieldMap.put("id", id);
			fieldMap.put("type", type);
			fieldMap.put("city", city);
			fieldMap.put("name", name);
			fieldMap.put("value", value);
			fieldMap.put("memo", memo);
			fieldMap.put("user", user);
			fieldMap.put("fav_time", fav_time);
			fieldMap.put("param1", param1);
			fieldMap.put("param2", param2);
			fieldMap.put("param3", param3);
			fieldMap.put("param4", param4);
			fieldMap.put("param5", param5);
			fieldMap.put("param6", param6);
			fieldMap.put("param7", param7);
			fieldMap.put("param8", param8);
			fieldMap.put("param9", param9);
			UserAppSession.cursession().executeCacheUrl(url, fieldMap);
		}

		public void loadFromMap(Map<String, Object> m) {
			id = TypeUtil.ObjectToString(m.get("id"));
			type = TypeUtil.ObjectToString(m.get("type"));
			city = TypeUtil.ObjectToString(m.get("city"));
			name = TypeUtil.ObjectToString(m.get("name"));
			value = TypeUtil.ObjectToString(m.get("value"));
			memo = TypeUtil.ObjectToString(m.get("memo"));
			user = TypeUtil.ObjectToString(m.get("user"));
			fav_time = TypeUtil.ObjectToString(m.get("fav_time"));
			param1 = TypeUtil.ObjectToString(m.get("param1"));
			param2 = TypeUtil.ObjectToString(m.get("param2"));
			param3 = TypeUtil.ObjectToString(m.get("param3"));
			param4 = TypeUtil.ObjectToString(m.get("param4"));
			param5 = TypeUtil.ObjectToString(m.get("param5"));
			param6 = TypeUtil.ObjectToString(m.get("param6"));
			param7 = TypeUtil.ObjectToString(m.get("param7"));
			param8 = TypeUtil.ObjectToString(m.get("param8"));
			param9 = TypeUtil.ObjectToString(m.get("param9"));
		}

	}

	public static void addFavoriteItem(String type, String city, String name,
			String value) {
		Favorite fav = new Favorite(type, city, name, value,
				UserAppSession.cursession().userName);
		fav.saveToCache();
	}

	public static String getFavoriteItemId(String type, String city,
			String name, String user) {
		return "&type=" + UserAppSession.urlEncode(type) + "city="
				+ UserAppSession.urlEncode(city) + "&name="
				+ UserAppSession.urlEncode(name) + "&user="
				+ UserAppSession.urlEncode(user);
	}

	private static String[] FavListToArray(List<Favorite> lst) {
		String[] arr = new String[lst.size()];
		for (int i = 0; i < lst.size(); i++) {
			arr[i] = lst.get(i).toString();
		}
		return arr;
	}

	public static void chooseFavorite(Context ctx, String type, String city,
			OnFavoriteChosenEvent evt) {
		chooseFavoriteEx(ctx, type, city, evt, null);
	}

	public static void chooseFavoriteEx(Context ctx, String type, String city,
			final OnFavoriteChosenEvent evt, String extraNames[]) {
		String url = Favorite.getLoadCacheUrl(type, city,
				UserAppSession.cursession().userName);
		final List<Favorite> favs = new ArrayList<Favorite>();
		if (extraNames != null && extraNames.length > 0)
			for (String n : extraNames) {
				Favorite fav = new Favorite(type, city, n, "", "");
				favs.add(fav);
			}
		Object o = UserAppSession.cursession().executeCacheUrl(url);
		if (o instanceof List) {
			List<Map<String, Object>> ds = TypeUtil.CastToList_SO(o);
			for (Map<String, Object> m : ds) {
				Favorite fav = new Favorite();
				fav.loadFromMap(m);
				favs.add(fav);
			}
		}

		if (favs.size() == 0) {
			UserAppSession.showToast("您的收藏记录是空的");
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle("收藏夹");
		builder.setSingleChoiceItems(FavListToArray(favs), 0,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
						if (evt != null) {
							Favorite fav = favs.get(arg1);
							evt.onFavoriteChosen(fav);
						}
					}
				});

		builder.create().show();
	}
}
