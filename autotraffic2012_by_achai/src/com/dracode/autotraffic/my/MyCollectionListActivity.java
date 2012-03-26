package com.dracode.autotraffic.my;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class MyCollectionListActivity extends Activity {

	/** 附近商家列表.*/
	private ListView listView;
	/** 列表数据集合.*/
	private List<ColletionBean> list_datas;
	private BaseActivityHelper baseHelper;
	
	@Override    
	public void onCreate(Bundle savedInstanceState) {    
	    super.onCreate(savedInstanceState);    
	    setContentView(R.layout.act_my_collection_list); 

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);
		
	    fillData();
	    initControls();
	}
	
	/** 
	 * 初始化页面控件和添加事件
	 */
	private void initControls() {
		listView = (ListView) findViewById(R.id.list_contents);
		baseHelper.right_layout.setVisibility(View.GONE);
		baseHelper.title_view.setText("我的收藏");
	
		ListViewAdapter adapter = new ListViewAdapter(this,list_datas,listView);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	private class ColletionBean {
		//原价
		private String cost_price;
		//优惠价
		private String preferential_price;
		//评分
		private int grade;
		//图片地址
		private String logoUrl;
		//简介
		private String prief;
		
		public String getCost_price() {
			return cost_price;
		}
		public void setCost_price(String cost_price) {
			this.cost_price = cost_price;
		}
		public String getPreferential_price() {
			return preferential_price;
		}
		public void setPreferential_price(String preferential_price) {
			this.preferential_price = preferential_price;
		}
		public int getGrade() {
			return grade;
		}
		public void setGrade(int grade) {
			this.grade = grade;
		}
		@SuppressWarnings("unused")
		public String getLogoUrl() {
			return logoUrl;
		}
		public void setLogoUrl(String logo_url) {
			this.logoUrl = logo_url;
		}
		public String getPrief() {
			return prief;
		}
		public void setPrief(String prief) {
			this.prief = prief;
		}

	}
	
	/**
	 * 模拟列表数据
	 */
	private void fillData(){
		list_datas = new ArrayList<ColletionBean>();
		for(int i = 0;i<15;i++){
			ColletionBean comInfo = new ColletionBean();
			comInfo.setLogoUrl("");
			comInfo.setPreferential_price("118元");
			comInfo.setCost_price("203");
			comInfo.setGrade(3);
			comInfo.setPrief("活动优惠价108元,您还不赶紧行动...");
			list_datas.add(comInfo);
		}	
	}
	
	public class ListViewAdapter extends ArrayAdapter<ColletionBean> {

//		private ListView listView;
//		private AsyncLoadImgUtil asyncImageLoader;
		private List<ColletionBean> lists;

		public ListViewAdapter(Activity activity,List<ColletionBean> datas, ListView listView) {
			super(activity, 0, datas);
//			this.listView = listView;
			this.lists = datas;
//			asyncImageLoader = new AsyncLoadImgUtil(context);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// Inflate the views from XML
			if (convertView == null) {
				LayoutInflater inflater = MyCollectionListActivity.this.getLayoutInflater();
				convertView = inflater.inflate(R.layout.my_collection_list_item, null);
			} 
			
			final ColletionBean item_data = lists.get(position);

			// Load the image and set it on the ImageView
//			String imageUrl = imageAndText.getLogoUrl();
			ImageView imageView = (ImageView) convertView.findViewById(R.id.list_logo);
			imageView.setBackgroundResource(R.drawable.pic_c);

//			imageView.setTag(imageUrl);
//			Drawable cachedImage = asyncImageLoader.loadDrawable(imageUrl,new ImageCallback() {
//						public void imageLoaded(Drawable imageDrawable,String imageUrl) {
//							ImageView imageViewByTag = (ImageView) listView.findViewWithTag(imageUrl);
//							if (imageViewByTag != null) {
//								imageViewByTag.setImageDrawable(imageDrawable);
//							}
//						}
//					});
//			if (cachedImage == null) {
////				imageView.setImageResource(R.drawable.loading04);
//			} else {
//				imageView.setImageDrawable(cachedImage);
//			}

			// Set the text on the TextView
			TextView pre_price = (TextView) convertView.findViewById(R.id.preferential_price);
			pre_price.setText(item_data.getPreferential_price());
			
			TextView original_cost = (TextView) convertView.findViewById(R.id.original_cost);
			original_cost.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG );
			original_cost.setText("￥"+item_data.getCost_price());
			
			ImageView grade_leve = (ImageView) convertView.findViewById(R.id.star);
			switch(item_data.getGrade()){
			case 1:
				grade_leve.setBackgroundResource(R.drawable.star01);
				break;
			case 2:
				grade_leve.setBackgroundResource(R.drawable.star02);
				break;
			case 3:
				grade_leve.setBackgroundResource(R.drawable.star03);
				break;
			case 4:
				grade_leve.setBackgroundResource(R.drawable.star04);
				break;
			case 5:
				grade_leve.setBackgroundResource(R.drawable.star05);
				break;
			}
		    TextView brief = (TextView) convertView.findViewById(R.id.brief);
		    brief.setText(item_data.getPrief());	    
				    
		    convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					
				}
			});
			return convertView;
		}
	}
	
}
