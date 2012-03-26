package com.dracode.autotraffic.my;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class MyOrderListActivity extends Activity {

	/** 附近商家列表.*/
	private ListView listView;
	/** 列表数据集合.*/
	private List<OrderBean> list_datas;
	/** 一月前.*/
	private ImageButton month_Pre;
	/** 一月内.*/
	private ImageButton month_in;
	private BaseActivityHelper baseHelper;
	@Override    
	public void onCreate(Bundle savedInstanceState) {    
	    super.onCreate(savedInstanceState);    
	    setContentView(R.layout.act_my_order_list); 

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
		baseHelper.title_view.setText("我的订单");
		
		month_Pre = (ImageButton) findViewById(R.id.order_last_bt);
		month_in = (ImageButton) findViewById(R.id.order_pre_bt);
		month_in.setSelected(true);
		month_Pre.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				month_Pre.setSelected(true);
				month_in.setSelected(false);
			}
		});
		
		month_in.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				month_Pre.setSelected(false);
				month_in.setSelected(true);
			}
		});
		ListViewAdapter adapter = new ListViewAdapter(this,list_datas,listView);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	private class OrderBean {
		//订单主题
		private String title;
		//订单数量
		private int account;
		//总计
		private double totalize;
		//图片地址
		private String logoUrl;
		//是否付款
		private boolean isPayoff;
		
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public int getAccount() {
			return account;
		}
		public void setAccount(int account) {
			this.account = account;
		}
		public double getTotalize() {
			return totalize;
		}
		public void setTotalize(double totalize) {
			this.totalize = totalize;
		}
		@SuppressWarnings("unused")
		public String getLogoUrl() {
			return logoUrl;
		}
		public void setLogoUrl(String logoUrl) {
			this.logoUrl = logoUrl;
		}
		public boolean getPayoff() {
			return isPayoff;
		}
		public void setPayoff(boolean isPayoff) {
			this.isPayoff = isPayoff;
		}
		
	}
	
	/**
	 * 模拟列表数据
	 */
	private void fillData(){
		list_datas = new ArrayList<OrderBean>();
		for(int i = 0;i<15;i++){
			OrderBean comInfo = new OrderBean();
			comInfo.setLogoUrl("");
			comInfo.setTitle("0元抽奖，巴厘岛6日浪漫...");
			comInfo.setAccount(3);
			comInfo.setTotalize(20);
			if(i%2==0){
				comInfo.setPayoff(true);
			} else {
				comInfo.setPayoff(false);
			}		
			list_datas.add(comInfo);
		}	
	}
	
	
	public class ListViewAdapter extends ArrayAdapter<OrderBean> {

//		private ListView listView;
//		private AsyncLoadImgUtil asyncImageLoader;
		private List<OrderBean> lists;

		public ListViewAdapter(Activity activity,List<OrderBean> datas, ListView listView) {
			super(activity, 0, datas);
//			this.listView = listView;
			this.lists = datas;
//			asyncImageLoader = new AsyncLoadImgUtil(context);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// Inflate the views from XML
			if (convertView == null) {
				LayoutInflater inflater = baseHelper.theAct.getLayoutInflater();
				convertView = inflater.inflate(R.layout.my_order_list_item, null);
			} 
			
			final OrderBean item_data = lists.get(position);

			// Load the image and set it on the ImageView
//			String imageUrl = imageAndText.getLogoUrl();
			ImageView imageView = (ImageView) convertView.findViewById(R.id.logo_img);
			imageView.setBackgroundResource(R.drawable.pic_s);

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
			TextView title = (TextView) convertView.findViewById(R.id.title);
			title.setText(item_data.getTitle());
			
			TextView account = (TextView) convertView.findViewById(R.id.amount_value);
			account.setText(Integer.toString(item_data.getAccount()));
			
		    TextView payTotal = (TextView) convertView.findViewById(R.id.totalize_value);
		    payTotal.setText(item_data.getTotalize()+"元");	    
		
		    Button payOff_Bt = (Button) convertView.findViewById(R.id.pay_Bt);
		    if(item_data.getPayoff()){
		    	 payOff_Bt.setText("已付款");
		    } else {
		    	 payOff_Bt.setText("未付款");
		    }		   
		    convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					
				}
			});
			return convertView;
		}
	}	
}
