package com.dracode.autotraffic.common.map;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PoiListActivity extends Activity {

	public TextView locView;
	/** 标题. */
	public TextView text_Title;
	public TextView text_NoData;
	public ListView list_Contents;
	private BaseActivityHelper baseHelper;
	public PoiListActivityHelper theHelper = new PoiListActivityHelper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_nearby_result);

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);

		initControls();
		theHelper.init(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		// 标题
		text_Title = (TextView) findViewById(R.id.middle_title);
		text_Title.setLayoutParams(new RelativeLayout.LayoutParams(220,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		ImageView img_Title = (ImageView) findViewById(R.id.more_city);
		img_Title.setVisibility(View.GONE);
		// 返回
		RelativeLayout layout_Back = (RelativeLayout) findViewById(R.id.left_layout);
		layout_Back.setVisibility(View.VISIBLE);
		layout_Back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		// 当前位置
		locView = (TextView) findViewById(R.id.query_result);
		list_Contents = (ListView) findViewById(R.id.list_contents);
		text_NoData = (TextView) findViewById(R.id.no_data);
	}

	public static class PoiListAdapter extends ArrayAdapter<PoiInfo> {

		private List<PoiInfo> dataList;
		private PoiListActivity act;

		public PoiListAdapter(PoiListActivity activity, List<PoiInfo> dataList) {
			super(activity, 0, dataList);
			act = activity;
			this.dataList = dataList;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = act.getLayoutInflater();

			final PoiInfo entity = dataList.get(position);
			if (entity != null) {
				convertView = inflater.inflate(
						R.layout.nearby_result_list_item, null);
				ImageView imageView = (ImageView) convertView
						.findViewById(R.id.icon);
				imageView.setBackgroundResource(entity.getIcon());

				TextView textView = (TextView) convertView
						.findViewById(R.id.name);
				textView.setText((position + 1+(act.theHelper.curPage-1)*20) + "." + entity.getName());

				TextView addressView = (TextView) convertView
						.findViewById(R.id.address);
				if (entity.getAddress() == null
						|| entity.getAddress().length() == 0
						|| "null".equals(entity.getAddress().toLowerCase())) {
					addressView.setText("中国");
				} else {
					addressView.setText(entity.getAddress());
				}

				TextView distanceView = (TextView) convertView
						.findViewById(R.id.distance);
				String distanceStr = "";
				if (entity.getDistance() >= 1000) {
					double _dis = TypeUtil.round(entity.getDistance() / 1000.0,
							2, BigDecimal.ROUND_HALF_UP);
					distanceStr = _dis + "公里";
				} else {
					distanceStr = entity.getDistance() + "米";
				}
				distanceView.setText("约" + distanceStr);

				ImageView phoneImgView = (ImageView) convertView
						.findViewById(R.id.phone);
				if (entity.getPhone() == null
						|| entity.getPhone().length() == 0
						|| "null".equals(entity.getPhone().toLowerCase())) {
					entity.setPhone(null);
					phoneImgView.setBackgroundResource(R.drawable.telb);
				} else {
					phoneImgView.setBackgroundResource(R.drawable.tela);
				}

				phoneImgView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (entity.getPhone() != null) {
							Intent intent = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel:" + entity.getPhone()));
							v.getContext().startActivity(intent);
						}
					}
				});
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						PoiMapActivity.poiInfoList = new ArrayList<PoiInfo>();
						PoiMapActivity.poiInfoList.addAll(dataList);
						Bundle bundle = new Bundle();
						bundle.putString("focusPoiId", entity.getTypeCode()
								+ ":" + entity.getPoiId());
						AppUtil.startActivity(act, PoiMapActivity.class, false,
								bundle);
					}
				});
			} else {
				convertView = inflater.inflate(
						R.layout.nearby_result_list_page, null);
				final int pageCount = (int) Math
						.ceil(act.theHelper.recCount / 20.0);
				final Integer page = act.theHelper.curPage;

				// 下一页
				Button nextPageBtn = (Button) convertView
						.findViewById(R.id.next_page_btn);
				// 上一页
				Button prePageBtn = (Button) convertView
						.findViewById(R.id.previous_page_btn);
				// 在首页
				if (page == 1)
					prePageBtn.setEnabled(false);
				// 在末页
				if (pageCount == page)
					nextPageBtn.setEnabled(false);

				nextPageBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int _page = page + 1 > pageCount ? pageCount : page + 1;
						act.theHelper.setCurPage(_page);
					}
				});
				prePageBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int _page = page - 1 < 1 ? 1 : page - 1;
						act.theHelper.setCurPage(_page);
					}
				});
			}
			return convertView;
		}
	}
}
