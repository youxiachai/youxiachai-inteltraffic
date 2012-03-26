package com.dracode.autotraffic.my;

import java.util.ArrayList;
import java.util.List;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MyMessageActivity extends Activity {

	/** 附近商家列表.*/
	private ListView hornListView;
	/** 列表数据集合.*/
	private List<HornSquare> imageAndTexts;
	private BaseActivityHelper baseHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_user_message_list);

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);
		
		initControls();
		fillData();
		GridViewAdapter adapter = new GridViewAdapter(this,imageAndTexts,hornListView);
		hornListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	private void initControls() {
		hornListView = (ListView) findViewById(R.id.list_contents);
		baseHelper.right_layout.setVisibility(View.GONE);
		baseHelper.title_view.setText("我的消息");
	}
	/**
	 * 模拟列表数据
	 */
	private void fillData(){
		imageAndTexts = new ArrayList<HornSquare>();
		for(int i = 0;i<15;i++){
			HornSquare comInfo = new HornSquare();
			comInfo.setLogo_url("");
			if(i==0){
				comInfo.setBusiness_name("宝泽会");
				comInfo.setDate_time("09-11 22:45");
				comInfo.setComment_pic_url("");
				comInfo.setContent("祝福宝马爱心基金公益路更加坚定悦行！！！ ————看到宝马爱心的博文《真诚践行JOY 让爱永续相承————宝马爱心基金三周年爱心之夜在渝隆重举行》有感而发的评论。");
				comInfo.setComment_times(611);
				comInfo.setTransmit_times(899);
			} else if( i==1 ) {
				comInfo.setBusiness_name("九江天源比亚迪4S店");
				comInfo.setDate_time("09-10 09:29");
				comInfo.setComment_pic_url("");
				comInfo.setContent("比亚迪九江天源店，G6新款到店了7.98-11.28万。全新搭载涡轮增压");
				comInfo.setComment_times(990);
				comInfo.setTransmit_times(899);
			} else{
				comInfo.setBusiness_name("宝泽会");
				comInfo.setDate_time("09-11 22:45");
				comInfo.setComment_pic_url("");
				comInfo.setContent("仅200元即可享受原价2000元广州宝泽宝马4S店爱心大礼包一份，购买后200元由广州宝泽捐到中华慈善总会宝马爱心基金，并有中华慈善总会出具证明，只要人人都献出一份爱心，世界将变的多么美好啊！！！");
				comInfo.setComment_times(611);
				comInfo.setTransmit_times(899);
			}
			imageAndTexts.add(comInfo);
		}	
	}
	public class GridViewAdapter extends ArrayAdapter<HornSquare> {

//		private ListView listView;
//		private AsyncLoadImgUtil asyncImageLoader;
		private List<HornSquare> imageAndTexts;

		public GridViewAdapter(Activity activity,List<HornSquare> imageAndTexts, ListView listView) {
			super(activity, 0, imageAndTexts);
//			this.listView = listView;
			this.imageAndTexts = imageAndTexts;
//			asyncImageLoader = new AsyncLoadImgUtil(context);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// Inflate the views from XML
			if (convertView == null) {
				LayoutInflater inflater = (baseHelper.theAct).getLayoutInflater();
				convertView = inflater.inflate(R.layout.user_message_list_item, null);
			} 
			
			final HornSquare imageAndText = imageAndTexts.get(position);

			// Load the image and set it on the ImageView
//			String imageUrl = imageAndText.getLogoUrl();
			ImageView imageView = (ImageView) convertView.findViewById(R.id.left_logo);
			imageView.setBackgroundResource(R.drawable.peo);

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
			TextView business_name = (TextView) convertView.findViewById(R.id.business_name);
			business_name.setText(imageAndText.getBusiness_name());
			
			TextView date_time_tx = (TextView) convertView.findViewById(R.id.date_time);
			date_time_tx.setText(imageAndText.getDate_time());
			
		    TextView content = (TextView) convertView.findViewById(R.id.main_content);
		    content.setText(imageAndText.getContent());
		    
		    ImageView content_pic = (ImageView) convertView.findViewById(R.id.content_pic);
		    if( position%2!=0){
		    	content_pic.setBackgroundResource(R.drawable.upfile);
		    	content_pic.setVisibility(View.VISIBLE);
		    } else {			
		    	content_pic.setVisibility(View.GONE);
		    }
			
		    convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View arg0) {
				}
			});
			TextView comment_times = (TextView) convertView.findViewById(R.id.comment_times);
			comment_times.setText(imageAndText.getComment_times()+"");
			
			TextView transmit_times = (TextView) convertView.findViewById(R.id.transmit_times);
			transmit_times.setText(imageAndText.getTransmit_times()+"");
			
			LinearLayout client = (LinearLayout) convertView.findViewById(R.id.kehuduan);
			if(position%2!=0){
				client.setVisibility(View.VISIBLE);
			}
			return convertView;
		}
	}
}
