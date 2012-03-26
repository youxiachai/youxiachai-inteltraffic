package com.dracode.autotraffic.serves.passenger;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class PassengerCarResultListActivity extends Activity {

	/** 页面列表.*/
	private ListView listView;
	/** 列表集合数据.*/
	private List<ListItemDatas> list_datas;
	private BaseActivityHelper baseHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_passenger_query_result_list);
		
		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);
		
		fillListData();
		initContros();
	}
	
	private void initContros(){
		baseHelper.title_view.setText("选择班次");
		
		baseHelper.right_img.setBackgroundResource(R.drawable.icon_sort);
		baseHelper.right_text.setText("排序");
		
		listView = (ListView) findViewById(R.id.list_contents);
		ListAdapter adapter = new ListAdapter(this,list_datas);
		listView.setAdapter(adapter);
	}
	/** 获取列表数据.*/
	private void fillListData(){
		list_datas = new ArrayList<ListItemDatas>();
		for(int i=0;i<20;i++){
			ListItemDatas item = new ListItemDatas();
			item.setCar_Type("中巴");
			item.setCarNumber("B1234");
			item.setStart_Station("广州南站");
			item.setEnd_Station("珠海拱北站");
			item.setStart_Time("10:00");
			item.setEnd_Time("15:00");
			item.setPrice("￥138");
			item.setRebate("6.5折");
			item.setLeave_Ticket("10");
			item.setWay_Station("途径中山，顺德");
			list_datas.add(item);
		}
	}
	/** 客车列表实体类.*/
	public class ListItemDatas {
		//车次
		private String carNumber;
		//车型
		private String car_Type;
		//出发站
		private String start_Station;
		//终点站 
		private String end_Station;
		//出发时间
		private String start_Time;
		//到达时间
		private String end_Time;
	    //票价
		private String price;
		//折扣
		private String rebate;
		//余票
		private String leave_Ticket;
		//途径站点名
		private String way_Station;
		public String getCarNumber() {
			return carNumber;
		}
		public void setCarNumber(String carNumber) {
			this.carNumber = carNumber;
		}
		public String getCar_Type() {
			return car_Type;
		}
		public void setCar_Type(String car_Type) {
			this.car_Type = car_Type;
		}
		public String getStart_Station() {
			return start_Station;
		}
		public void setStart_Station(String start_Station) {
			this.start_Station = start_Station;
		}
		public String getEnd_Station() {
			return end_Station;
		}
		public void setEnd_Station(String end_Station) {
			this.end_Station = end_Station;
		}
		public String getStart_Time() {
			return start_Time;
		}
		public void setStart_Time(String start_Time) {
			this.start_Time = start_Time;
		}
		public String getEnd_Time() {
			return end_Time;
		}
		public void setEnd_Time(String end_Time) {
			this.end_Time = end_Time;
		}
		public String getPrice() {
			return price;
		}
		public void setPrice(String price) {
			this.price = price;
		}
		public String getRebate() {
			return rebate;
		}
		public void setRebate(String rebate) {
			this.rebate = rebate;
		}
		public String getLeave_Ticket() {
			return leave_Ticket;
		}
		public void setLeave_Ticket(String leave_Ticket) {
			this.leave_Ticket = leave_Ticket;
		}
		public String getWay_Station() {
			return way_Station;
		}
		public void setWay_Station(String way_Station) {
			this.way_Station = way_Station;
		}	
	}
	
	public class ListAdapter extends ArrayAdapter<ListItemDatas> {

		private List<ListItemDatas> dataArray;

		public ListAdapter(Activity activity, List<ListItemDatas> dataArray) {
			super(activity, 0, dataArray);
			this.dataArray = dataArray;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = baseHelper.theAct.getLayoutInflater();
				convertView = inflater.inflate(R.layout.passenger_query_result_list_item, null);
			}
			
			final ListItemDatas entity = dataArray.get(position);
			
			TextView car_num = (TextView) convertView.findViewById(R.id.pass_num);
			car_num.setText(entity.getCarNumber());
			
			TextView car_type = (TextView)  convertView.findViewById(R.id.pass_type);
			car_type.setText(entity.getCar_Type());
			
			TextView start_station = (TextView)  convertView.findViewById(R.id.start_station);
			start_station.setText(entity.getStart_Station());
			
			TextView end_station = (TextView)  convertView.findViewById(R.id.end_station);
			end_station.setText(entity.getEnd_Station());
			
			TextView start_time = (TextView)  convertView.findViewById(R.id.start_time);
			start_time.setText(entity.getStart_Time());
			
			TextView end_time = (TextView)  convertView.findViewById(R.id.end_time);
			end_time.setText(entity.getEnd_Time());
			
			TextView price = (TextView)convertView.findViewById(R.id.price);
			price.setText(entity.getPrice());
			
			TextView rebate = (TextView) convertView.findViewById(R.id.peice_01);
			rebate.setText(entity.getRebate());
			
			TextView leave_ticket = (TextView)convertView.findViewById(R.id.ticket);
			leave_ticket.setText(entity.getLeave_Ticket());
			
			TextView way_station = (TextView)convertView.findViewById(R.id.middle_stations);
			way_station.setText(entity.getWay_Station());
			
			convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(baseHelper.theAct,PredetermineTicketActivity.class);
					startActivity(intent);
				}
			});
			return convertView;
		}
	}
}
