package com.dracode.autotraffic.common.switchcity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.andrdce.ctact.CvtDataHelper;
import com.dracode.andrdce.ctact.CvtDataHelper.OnCvtDataEvent;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.map.LocationHelper;
import com.dracode.autotraffic.common.map.LocationHelper.OnLocationFoundEvent;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SwitchCityActivityHelper {

	public CvtDataHelper cvtHelper = new CvtDataHelper();
	public SwitchCityActivity theAct = null;
	private SharedPreferences city_Pro;
	private List<Map<String, String>> cityList = null;

	public void init(SwitchCityActivity act) {
		theAct = act;
		city_Pro = theAct.getSharedPreferences("CITY_INFO",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);

		// ["ID","PROVICECODE","NAME","ENNAME","SHORTNAME","ALIAS","CITY_CODE","LETTER","GEOINFO"]
		cvtHelper.init(theAct, 610061);
		cvtHelper.cacheExpireTm = UserAppSession.CAHCE_EXPIRE_TIME_DAY;

		getSortList(false);
	}

	private void startLocationFinder() {
		if (UserAppSession.location_city == null
				|| "".equals(UserAppSession.location_city)) {
			LocationHelper lh = new LocationHelper();
			lh.convertAddress = 2;
			lh.fireEventOnError = true;
			OnLocationFoundEvent evt = new OnLocationFoundEvent() {
				@Override
				public void onLocationFound(double lon, double lat, String addr) {
					if (addr == null || addr.length() == 0) {
						loacation_City.setText("定位失败");
						return;
					}
					setLocationCityCode(addr);
				}

				@Override
				public void onCanceled() {
					loacation_City.setText("已取消定位");
				}

				@Override
				public void onError(String msg) {
					loacation_City.setText("定位出错");
					UserAppSession.showToast(theAct,msg);
				}

			};
			lh.init(theAct, evt);
		}
	}

	private void setLocationCityCode(String cityCode) {
		if (cityList == null || cityCode == null)
			return;
		cityCode = cityCode.trim();
		for (Map<String, String> city : cityList) {
			if (cityCode.equals(city.get("CITY_CODE"))
					|| cityCode.equals(city.get("NAME"))
					|| cityCode.equals(city.get("NAME") + "市")) {
				UserAppSession.cur_CityCode = city.get("CITY_CODE");
				UserAppSession.cur_CityGeoInfo = city.get("GEOINFO");
				UserAppSession.location_city = city.get("NAME");
				loacation_City.setText(UserAppSession.location_city);
				return;
			}
		}

		loacation_City.setText(cityCode);
	}

	/**
	 * 获取分类列表
	 */
	public void getSortList(boolean refresh) {
		if (!refresh) {
			if (cvtHelper.tryLoadFromCache()) {
				cityList = cvtHelper.getCurrentDataListSS();
				showCityList(cityList);
				startLocationFinder();
				return;
			}
		}

		OnCvtDataEvent evt = new OnCvtDataEvent() {
			@Override
			public void onDataCanceled() {
				UserAppSession.showToast(theAct, "操作被中止");
			}

			@Override
			public void onDataError(String msg) {
				UserAppSession.showToast(theAct, msg);
			}

			@Override
			public void onDataLoaded(CvtDataHelper cvth) {
				cityList = cvtHelper.getCurrentDataListSS();
				showCityList(cityList);
				startLocationFinder();
			}

			@Override
			public void afterFetchData() {
			}
		};
		cvtHelper.setCvtDataEvt(evt);
		cvtHelper.loadingMessage = "正在获取城市列表...";
		if (refresh)
			cvtHelper.refreshData();
		else
			cvtHelper.loadData();
	}

	private void showCityList(List<Map<String, String>> citys) {
		// 实例化adapter
		ListAdapter adaper = new ListAdapter(theAct, citys);
		theAct.personList.setAdapter(adaper);
		adaper.notifyDataSetChanged();
	}

	// 存放存在的汉语拼音首字母和与之对应的列表位置
	public HashMap<String, Integer> alphaIndexer;
	// 存放存在的汉语拼音首字母
	public String[] sections;

	public class ListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private List<Map<String, String>> dataList;
		SwitchCityActivity activity;

		public ListAdapter(Context context, List<Map<String, String>> list) {
			this.inflater = LayoutInflater.from(context);
			this.dataList = list;
			alphaIndexer = new HashMap<String, Integer>();
			sections = new String[list.size()];
			activity = (SwitchCityActivity) context;
			for (int i = 0; i < list.size(); i++) {
				// 当前汉语拼音首字母
				String letter = list.get(i).get("LETTER");
				String currentStr = activity.getAlpha(letter);
				// 上一个汉语拼音首字母，如果不存在为""
				String previewStr = (i - 1) >= 0 ? activity.getAlpha(list.get(
						i - 1).get("LETTER")) : " ";
				if (!previewStr.equals(currentStr)) {
					String name = activity.getAlpha(letter);
					alphaIndexer.put(name, i);
					sections[i] = name;
				}
			}
		}

		@Override
		public int getCount() {
			return dataList.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			return dataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position + 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.switch_city_item, null);
				holder = new ViewHolder();
				holder.letter = (TextView) convertView
						.findViewById(R.id.letter);
				holder.city = (TextView) convertView
						.findViewById(R.id.city_name);
				holder.relative = (RelativeLayout) convertView
						.findViewById(R.id.relative);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (position == 0) {
				holder.letter.setVisibility(View.VISIBLE);
				holder.letter.setText("定位城市");
				if ("".equals(UserAppSession.location_city)
						|| UserAppSession.location_city == null) {
					holder.city.setText("正在定位城市...");
				} else {
					holder.city.setText(UserAppSession.location_city);
				}
				loacation_City = holder.city;
				holder.relative.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if ("".equals(UserAppSession.location_city)
								|| UserAppSession.location_city == null
								|| cityList == null) {

						} else {
							UserAppSession.cur_CityName = UserAppSession.location_city;
							city_Pro.edit()
									.putString("cur_city",
											UserAppSession.cur_CityName).commit();

							for (Map<String, String> city : cityList) {
								if (UserAppSession.location_city.equals(city
										.get("NAME"))) {
									UserAppSession.cur_CityCode = city
											.get("CITY_CODE");
									UserAppSession.cur_CityGeoInfo = city
											.get("GEOINFO");
									break;
								}
							}
							city_Pro.edit()
									.putString("cur_code",
											UserAppSession.cur_CityCode).commit();
							city_Pro.edit()
									.putString("cur_cityGeoInfo",
											UserAppSession.cur_CityGeoInfo)
									.commit();
							theAct.finish();
						}
					}
				});
				return convertView;
			} else {
				final Map<String, String> city = dataList.get(position - 1);
				holder.city.setText(city.get("NAME"));

				String currentStr = activity.getAlpha(city.get("LETTER"));
				String previewStr = (position - 2) >= 0 ? activity
						.getAlpha(dataList.get(position - 2).get("LETTER"))
						: " ";
				if (!previewStr.equals(currentStr)) {
					holder.letter.setVisibility(View.VISIBLE);
					holder.letter.setText(currentStr);
				} else {
					holder.letter.setVisibility(View.GONE);
				}
				holder.relative.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String city_name = city.get("NAME");
						UserAppSession.cur_CityName = city_name;
						city_Pro.edit().putString("cur_city", city_name)
								.commit();
						String city_code = city.get("CITY_CODE");
						UserAppSession.cur_CityCode = city_code;
						city_Pro.edit().putString("cur_code", city_code)
								.commit();
						UserAppSession.cur_CityGeoInfo = city.get("GEOINFO");
						city_Pro.edit()
								.putString("cur_cityGeoInfo",
										UserAppSession.cur_CityGeoInfo).commit();
						theAct.finish();
					}
				});
				return convertView;
			}
		}

		public class ViewHolder {
			TextView city;
			TextView letter;
			RelativeLayout relative;
		}

	}

	public TextView loacation_City;
}
