package com.dracode.autotraffic.bus.busline;


import java.util.List;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.bus.busstation.BusStationInfo;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BusStationMapAdapter extends BaseAdapter {

	List<BusStationInfo> stations = null;
	Context context = null;
	private LayoutInflater mInflater=null;

	public BusStationMapAdapter(Context context, List<BusStationInfo> stations) {
		this.context = context;
		this.stations = stations;                
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return stations.size();
	}

	public Object getItem(int position) {
		return stations.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		BusStationInfo st=stations.get(position);
		return st.stationType;
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		final BusStationInfo station = stations.get(position);
		int type = station.stationType;
		if (convertView != null) {
			holder = (ViewHolder) convertView.getTag();
			if(holder==null || holder.stationType!=type)
				convertView=null;
		}
		if (convertView == null) {
			switch(type){
			case BusStationInfo.StationType_Start:
				convertView = mInflater.inflate(R.layout.busstatstart, null);
				break;
			case BusStationInfo.StationType_End:
				convertView = mInflater.inflate(R.layout.busstatend, null);
				break;
			default:
				convertView = mInflater.inflate(R.layout.busstatitem, null);
				break;
			}
			holder = new ViewHolder();
			holder.txtStationName = (TextView) convertView.findViewById(R.id.textViewStationName);
			holder.imgBusInStation = (ImageView) convertView.findViewById(R.id.imageViewBusInStation);
//			holder.txtInStationCount = (TextView) convertView.findViewById(R.id.textViewInStationCount);
			holder.imgBusLeaveStation = (ImageView) convertView.findViewById(R.id.imageViewBusLeaveStation);
//			holder.txtLeaveStationCount = (TextView) convertView.findViewById(R.id.textViewLeaveStationCount);
			holder.inStationNameLayout = (RelativeLayout) convertView.findViewById(R.id.instationtime_);
			holder.txtInStationNaame = (TextView) convertView.findViewById(R.id.station_name);
			holder.inStationTimeLayout = (RelativeLayout) convertView.findViewById(R.id.instationtime_01);
			holder.txtInStationTime = (TextView) convertView.findViewById(R.id.inStationTime);
			holder.texInLastTime = (TextView) convertView.findViewById(R.id.last_time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.setStationInfo(station);
		return convertView;
	}

	static class ViewHolder {
		int stationType;
		BusStationInfo station;
		TextView txtStationName;
		ImageView imgBusInStation;
//		TextView txtInStationCount;
		ImageView imgBusLeaveStation;
//		TextView txtLeaveStationCount;
		RelativeLayout inStationNameLayout;
		TextView txtInStationNaame;
		RelativeLayout inStationTimeLayout;
		TextView txtInStationTime;
		TextView texInLastTime;//最后进站时间
		
		public void setStationInfo(BusStationInfo aStation) {
			station=aStation;
			stationType=0;
			if(station==null)
				return;
			stationType=station.stationType;
			txtStationName.setText(station.stationName);
			if(imgBusInStation!=null){
				if(station.inStationCount>0){
					imgBusInStation.setVisibility(View.VISIBLE);
//					txtInStationCount.setVisibility(View.VISIBLE);
//					txtInStationCount.setText("("+Integer.toString(station.inStationCount)+")");
					inStationNameLayout.setVisibility(View.VISIBLE);
					txtInStationNaame.setVisibility(View.VISIBLE);
					txtInStationNaame.setText(station.stationName);
					inStationTimeLayout.setVisibility(View.VISIBLE);
					txtInStationTime.setVisibility(View.VISIBLE);
					String htmlStr = "已进站公交车<font color=#ff0000>"+station.inStationCount+"</font>辆";
					txtInStationTime.setText(Html.fromHtml(htmlStr));
					texInLastTime.setText(station.lastInTime);
				}
				else{
					imgBusInStation.setVisibility(View.GONE);
//					txtInStationCount.setVisibility(View.INVISIBLE);
					inStationNameLayout.setVisibility(View.GONE);
					txtInStationNaame.setVisibility(View.GONE);
					inStationTimeLayout.setVisibility(View.GONE);
					txtInStationTime.setVisibility(View.GONE);
				}
			}

			if(imgBusLeaveStation!=null){
				if(station.leaveStationCount>0){
					imgBusLeaveStation.setVisibility(View.VISIBLE);
//					txtLeaveStationCount.setVisibility(View.VISIBLE);
//					txtLeaveStationCount.setText("("+Integer.toString(station.leaveStationCount)+")");
				}
				else{
					imgBusLeaveStation.setVisibility(View.GONE);
//					txtLeaveStationCount.setVisibility(View.INVISIBLE);
				}
			}
		}
	}
}
