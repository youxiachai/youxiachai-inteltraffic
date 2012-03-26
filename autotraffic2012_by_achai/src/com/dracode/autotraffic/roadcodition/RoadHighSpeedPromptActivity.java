package com.dracode.autotraffic.roadcodition;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class RoadHighSpeedPromptActivity extends Activity{

    private ListView listView;  
    private List<ListItem> datas;
    
    private int[] imgArray = {R.drawable.g02,R.drawable.g01,R.drawable.g05,R.drawable.g03,R.drawable.g04};
    private String[] item_promt = {"当前修路维修中","当前道路管制中","当前到道路有交通事故",
    		             "当前道路由于天气原因，行驶缓慢","由于其他原因，当前行驶缓慢"};
	private BaseActivityHelper baseHelper;
	@Override    
    public void onCreate(Bundle savedInstanceState) {    
	     super.onCreate(savedInstanceState);    
	     setContentView(R.layout.act_road_prompt_list); 

			baseHelper = new BaseActivityHelper();
			baseHelper.init(this);
			
	     fillData();
	     initControls();    	        
	}
	
	/**
	 * 填充数据
	 */
	private void fillData(){
		datas = new ArrayList<ListItem>();
		for(int i=0;i<imgArray.length;i++){
			ListItem item = new ListItem();
			item.setRoad_img(imgArray[i]);
			item.setRoad_text(item_promt[i]);
			datas.add(item);
		}
	}
	/** 
     * 初始化页面控件和添加事件
	 */
	private void initControls() {
		baseHelper.left_layout.setVisibility(View.VISIBLE);
		baseHelper.right_layout.setVisibility(View.GONE);
		baseHelper.more_city.setVisibility(View.GONE);
					  
		baseHelper.left_img.setBackgroundResource(R.drawable.header_back);
		baseHelper.left_text.setText("返回");
		
		baseHelper.title_view.setText("温馨提示");
		
		listView = (ListView) findViewById(R.id.list_contents);
		ListDataAdapter listAdapter = new ListDataAdapter(RoadHighSpeedPromptActivity.this,datas);
		listView.setAdapter(listAdapter);
		listAdapter.notifyDataSetChanged();
	}
	
	private class ListItem {
		
		private int road_img;
		private String road_text;
		
		public int getRoad_img() {
			return road_img;
		}
		public void setRoad_img(int road_img) {
			this.road_img = road_img;
		}
		public String getRoad_text() {
			return road_text;
		}
		public void setRoad_text(String road_text) {
			this.road_text = road_text;
		}		
	}
	
	protected class ListDataAdapter extends ArrayAdapter<ListItem>{

		private Activity context;
		private List<ListItem> lists;
		public ListDataAdapter(Activity activity,List<ListItem> datas) {
			super(activity, 0, datas);
			this.context = activity;
			this.lists = datas;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			// Inflate the views from XML
			if (convertView == null) {
				LayoutInflater inflater = ((RoadHighSpeedPromptActivity) context).getLayoutInflater();
				convertView = inflater.inflate(R.layout.high_speed_prompt_item, null);				
			} 
			
			final ListItem item_data = lists.get(position);
			
			ImageView img = (ImageView) convertView.findViewById(R.id.img);
			img.setBackgroundResource(item_data.getRoad_img());
			
			TextView prief = (TextView) convertView.findViewById(R.id.prief);
			prief.setText(item_data.getRoad_text());
			return convertView;
		}
		
	}
}
