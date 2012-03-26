package com.dracode.autotraffic.serves.icarcar;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class IcarGoodsListActivity  extends Activity {

	
	/** 列表对象.*/
	private ListView listView;
	/** 商品列表数据集合.*/
	private List<GoodsListInfo> list_datas;
	/** 返回按钮.*/
	private RelativeLayout back_layout;
	private BaseActivityHelper baseHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_service_icard_goods_list);

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);

		fillData();
		initControl();	
	}
	/**
	 * 初始化页面控件和事件
	 */
	private void initControl(){
		back_layout = (RelativeLayout) findViewById(R.id.left_layout);
		back_layout.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		listView = (ListView) findViewById(R.id.list_contents);
		GridViewAdapter adapter = new GridViewAdapter(this, list_datas);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	//填充列表数据
	private void fillData(){	
		list_datas = new ArrayList<GoodsListInfo>();
		for (int i = 0;i<30;i++){
			GoodsListInfo listItem = new GoodsListInfo();
			listItem.setLogo_url("");
			listItem.setOriginal_cost("￥205");
			listItem.setPreferential_price("118元");
			listItem.setEvaluateLeve(4);
			listItem.setBrif("洗车洗车洗车洗车洗车洗车洗车");
			list_datas.add(listItem);
		}
	}
	
	public class GoodsListInfo {
		/** 爱卡卡商品列表.*/
		private String logo_url;
		/** 优惠价.*/
		private String preferential_price;
		/** 原价.*/
		private String original_cost;
		/** 评分等级.*/
		private int evaluateLeve;
		/** 简介.*/
		private String brif;
		
		public String getLogo_url() {
			return logo_url;
		}
		public void setLogo_url(String logo_url) {
			this.logo_url = logo_url;
		}
		public String getPreferential_price() {
			return preferential_price;
		}
		public void setPreferential_price(String preferential_price) {
			this.preferential_price = preferential_price;
		}
		public String getOriginal_cost() {
			return original_cost;
		}
		public void setOriginal_cost(String original_cost) {
			this.original_cost = original_cost;
		}
		public int getEvaluateLeve() {
			return evaluateLeve;
		}
		public void setEvaluateLeve(int evaluateLeve) {
			this.evaluateLeve = evaluateLeve;
		}
		public String getBrif() {
			return brif;
		}
		public void setBrif(String brif) {
			this.brif = brif;
		}
	}
	public class GridViewAdapter extends ArrayAdapter<GoodsListInfo> {

		private List<GoodsListInfo> dataArray;

		public GridViewAdapter(Activity activity, List<GoodsListInfo> dataArray) {
			super(activity, 0, dataArray);
			this.dataArray = dataArray;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = baseHelper.theAct.getLayoutInflater();
				convertView = inflater.inflate(R.layout.service_icard_goods_list_item, null);
			}
			
			final GoodsListInfo entity = dataArray.get(position);
			
//			ImageView logo = (ImageView) convertView.findViewById(R.id.list_logo);
//			logo.setBackgroundResource(itemIconArray[position]);
			
			TextView textView = (TextView) convertView.findViewById(R.id.preferential_price);
			textView.setText(entity.getPreferential_price());
		
			TextView costView = (TextView) convertView.findViewById(R.id.original_cost);
			costView.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG );
			costView.setText(entity.getOriginal_cost());
			
			ImageView image = (ImageView) convertView.findViewById(R.id.star);
			image.setBackgroundResource(R.drawable.star04);
			
			TextView brifView = (TextView) convertView.findViewById(R.id.brief);
			brifView.setText(entity.getBrif());
			
			convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(baseHelper.theAct,IcarGoodsDetailActivity.class);
					startActivity(intent);
				}
			});
			return convertView;
		}
	}
}
