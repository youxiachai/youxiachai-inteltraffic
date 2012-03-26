package com.dracode.autotraffic.serves.icarcar;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dracode.autotraffic.R;

public class IcarCarIndexActivity extends IcarBaseActivity implements Runnable {

	private int[] itemIconArray = { R.drawable.icon_01,R.drawable.icon_02,
			R.drawable.icon_03,R.drawable.icon_04,
			R.drawable.icon_05,R.drawable.icon_06};
	private String[] itemNameArray = {"清洁", "美容", "保养", "轮胎",
									  "代办", "精品"};
	private String[] itemDescribeArray = {"洗车9元起，更有超值套餐", 
										  "打蜡99元起，精心呵护爱车", 
										  "小保养198元起，省钱更省心", 
										  "马牌轮胎，性能典范",
										  "违章代缴，快速处理。", 
										  "每周一款精品，底价团"};
	private List<ListItemData> listItems = new ArrayList<ListItemData>();
	/** 列表对象.*/
	private ListView listView;
	/** 动画view.*/
	private ImageView animationView;
	/** 动画对象.*/
	private AnimationDrawable frameAnimation;
	/** 动画线程. */
	private Thread animationThread;
	private Handler handler = new Handler();

	/**
	 * @Title: onResume
	 * @Description: 重新开始
	 */
	@Override
	protected final void onResume() {
		super.onResume();
		final int DELAY_TIME = 100;
		handler.postDelayed(mRunnable, DELAY_TIME);
	}
	/** 播放图片. */
	private Runnable mRunnable = new Runnable() {
		public void run() {
	        //切换帧动画的运行状态        
			if (!frameAnimation.isRunning()) {             
				 frameAnimation.start();         
			} 
		}
	};
	@Override    
    public void onCreate(Bundle savedInstanceState) {    
	    super.onCreate(savedInstanceState);    
	    setContentView(R.layout.act_service_icarcard); 
	        
	    init();
	    initControls();  
	
		fillData();
		listView = (ListView) findViewById(R.id.list_contents);
		ListViewAdapter adapter = new ListViewAdapter(this, listItems);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	/** 
     * 初始化页面控件和添加事件
	 */
	private void initControls() {
		baseHelper.left_layout.setVisibility(View.VISIBLE);
		baseHelper.right_layout.setVisibility(View.VISIBLE);
		
		baseHelper.title_view.setText("爱卡卡");
		
		baseHelper.left_img.setBackgroundResource(R.drawable.header_back);
		baseHelper.left_text.setText("返回");
		
		baseHelper.left_layout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		baseHelper.right_img.setBackgroundResource(R.drawable.icon_car);
		baseHelper.right_text.setText("爱车");
		
		baseHelper.right_layout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(baseHelper.theAct,MyCarActivity.class);
				startActivity(intent);
			}
		});
		
		animationView = (ImageView) findViewById(R.id.animationView);	
		frameAnimation = (AnimationDrawable) animationView.getBackground(); 
		animationThread = new Thread(this);
		animationThread.start();
		
		animationView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:13727887899")); 
			    startActivity(phoneIntent);
			}
		});
	}
	
	@Override
	public void run() {
		
	}
	
	//填充列表数据
		private void fillData(){		
			for (int i = 0;i<itemNameArray.length;i++){
				ListItemData listItem = new ListItemData();
				listItem.setTopic_text(itemNameArray[i]);
				listItem.setBrif_text(itemDescribeArray[i]);
				listItems.add(listItem);
			}
		}
		private class ListItemData {
			
			private String topic_text;
			private String brif_text;
			
			public String getTopic_text() {
				return topic_text;
			}
			public void setTopic_text(String topicText) {
				topic_text = topicText;
			}
			public String getBrif_text() {
				return brif_text;
			}
			public void setBrif_text(String brifText) {
				brif_text = brifText;
			}
			
		}
		public class ListViewAdapter extends ArrayAdapter<ListItemData> {

			private List<ListItemData> dataArray;

			public ListViewAdapter(Activity activity, List<ListItemData> dataArray) {
				super(activity, 0, dataArray);
				this.dataArray = dataArray;
			}

			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					LayoutInflater inflater = baseHelper.theAct.getLayoutInflater();
					convertView = inflater.inflate(R.layout.service_icarcard_list_item, null);
				}
				
				final ListItemData entity = dataArray.get(position);
				
				ImageView logo = (ImageView) convertView.findViewById(R.id.list_logo);
				logo.setBackgroundResource(itemIconArray[position]);
				
				TextView textView = (TextView) convertView.findViewById(R.id.topic_name);
				textView.setText(entity.getTopic_text());
			
				TextView timeView = (TextView) convertView.findViewById(R.id.brifc);
				timeView.setText(entity.getBrif_text());
				
				convertView.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(baseHelper.theAct,IcarGoodsListActivity.class);
						startActivity(intent);
					}
				});
				return convertView;
			}
		}
}
