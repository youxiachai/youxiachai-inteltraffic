package com.dracode.autotraffic.common.map;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.vedio.PlayVedioActivity;
import com.mapabc.mapapi.MapView;
import com.mapabc.mapapi.PoiItem;
import com.mapabc.mapapi.MapView.LayoutParams;

public class MyPoiOverlay extends com.mapabc.mapapi.PoiOverlay {
	private Context context;
	private Drawable drawable;
//	private int number = 0;
	private List<PoiItem> poiItem;
	private LayoutInflater mInflater;
	private int height;
	private String showingItemLink = null;
	public String lastPopupItemId=null;

	@Override
	public void closePopupWindow() {
		lastPopupItemId=null;
		super.closePopupWindow();
	}

	public MyPoiOverlay(Context context, Drawable drawable,List<PoiItem> poiItem) {
		super(drawable, poiItem);
		this.context = context;
		this.poiItem = poiItem;
		mInflater = LayoutInflater.from(context);
		height = drawable.getIntrinsicHeight();
	}

	@Override
	protected Drawable getPopupBackground() {
		drawable = context.getResources().getDrawable(R.drawable.tip_pointer_button);
		return drawable;
	}

	@Override
	protected View getPopupView(PoiItem item) {
		View view = null;
		if ("TAXI".equals(item.getTypeCode())){
			view = setTaxiPopupView(item);
		} else {
			view = setOtherPopupView(item);
		}
		return view;
	}
	/**
	 * 获取其他
	 * @param item
	 * @return
	 */
	private View setOtherPopupView(final PoiItem item) {
		View view = mInflater.inflate(R.layout.poi_popup, null);
		// 初始化控件
		TextView nameTextView = (TextView) view.findViewById(R.id.PoiName);
		TextView addressTextView = (TextView) view.findViewById(R.id.poiAddress);
		ImageView division2Img = (ImageView) view.findViewById(R.id.division02);
		ImageView divisionImg = (ImageView) view.findViewById(R.id.division01);
		ImageButton phoneImgBt = (ImageButton) view.findViewById(R.id.dial_bt);
		ImageButton linePlanningImgBt = (ImageButton) view.findViewById(R.id.line_planning_btn);
		
		nameTextView.setText(item.getTitle());
		String address = item.getSnippet();
		if (address == null || address.length() == 0) {
			address = "中国";
		}
		if (linePlanningImgBt != null && !"ROAD_CAM".equals(item.getTypeCode())) {
			divisionImg.setVisibility(View.VISIBLE);
			linePlanningImgBt.setVisibility(View.VISIBLE);
			linePlanningImgBt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
//					SysConstants.GoNextActivity = BusChangeResultActivity.class;
//					Bundle _bundle = new Bundle();
//					_bundle.putString("destination", item.getTitle());
//					AppUtil.startActivity(v.getContext(), BusChangeActivity.class, false, _bundle);
				}
			});
		}
		if (item.getTel() != null && item.getTel().length() > 0) {
			division2Img.setVisibility(View.VISIBLE);
			phoneImgBt.setVisibility(View.VISIBLE);
			phoneImgBt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + item.getTel()));
					v.getContext().startActivity(intent);
				}
			});
		}
		String pid = item.getTypeCode() + ":" + item.getPoiId();
//		if(pid.equals(lastPopupItemId)) {
//			lastPopupItemId = null;
//		} else {
			lastPopupItemId = pid;
//		}
		if ("ROAD_CAM".equals(item.getTypeCode())) {
			showingItemLink = item.getPoiId();
			address = "道路视频监控";
		} else {
			showingItemLink = null;
		}
		addressTextView.setText(address);
		
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.LinearLayoutPopup);
		layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (showingItemLink != null && !"".equals(showingItemLink)) {
					Intent _intent = new Intent();
					Bundle _bundle = new Bundle();

					_bundle.putString("vedioUrl", showingItemLink);
					_intent.setClass(v.getContext(), PlayVedioActivity.class);
					_intent.putExtras(_bundle);
					_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

					v.getContext().startActivity(_intent);
				}
			}
		});
		return view;
	}
	/**
	 * 获取出租电召
	 * @param item
	 * @return
	 */
	private View setTaxiPopupView(final PoiItem item){
		View view = mInflater.inflate(R.layout.poi_popup_taxi, null);
		TextView nameTextView = (TextView) view.findViewById(R.id.PoiName);
		TextView distance = (TextView) view.findViewById(R.id.distance);
		ImageButton dial_bt = (ImageButton) view.findViewById(R.id.dial_bt);

		if ("TAXI".equals(item.getTypeCode())) {
			if(item.getPoiId().indexOf("粤")==-1)
			    nameTextView.setText(" 粤"+item.getPoiId());
			else
				nameTextView.setText(item.getPoiId());
			distance.setText(item.getTitle()+"M");
			dial_bt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if ("TAXI".equals(item.getTypeCode())) {
						Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + item.getSnippet()));
						v.getContext().startActivity(intent);
					}
				}
			});
		}
		return view;
	}
	@Override
	public void addToMap(MapView arg0) {
		super.addToMap(arg0);
	}
	@Override
	protected LayoutParams getLayoutParam(int arg0) {
		LayoutParams params = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
													   MapView.LayoutParams.WRAP_CONTENT, 
													   poiItem.get(arg0).getPoint(), 
													   0, 
													   -height, 
													   LayoutParams.BOTTOM_CENTER);

		return params;
	}
	@Override
	protected Drawable getPopupMarker(PoiItem arg0) {
		return super.getPopupMarker(arg0);
	}
	@Override
	protected boolean onTap(int index) {
//		number = index;
		return super.onTap(index);
	}

}