package com.dracode.autotraffic.nearby;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.QueryHistoryHelper;
import com.dracode.autotraffic.common.map.PoiListActivity;
import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechError;
import com.iflytek.ui.RecognizerDialogListener;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class NearbyQueryActivityHelper {

	public NearbyDataIntf nearbyDataIntf=new NearbyDataIntf();
	
	public NearbyQueryActivity theAct = null;
	public QueryHistoryHelper historyHelper = new QueryHistoryHelper();
	/** adapter.*/
	private NearbySortAdapter mAdapter;
	/** 分组信息.*/
	private List<Map<String,Object>> groups;
	/** 数据适配器 .*/
	private ArrayAdapter<String> distanceAdapter;
	/** 搜索距离 .*/
	private int distance;
	
	/** 距离下拉列表数据 .*/
	private final String[] distanceArray = {"500M", "1KM", "2KM", "3KM", "5KM"};

	public void init(NearbyQueryActivity act) {
		theAct = act;
		setListener();
		
		nearbyDataIntf.init(theAct, this);
		nearbyDataIntf.getSortList(false);// 加载缓存
	}
	
	public void setListener() {
		// 初始化距离
		String distanceStr = distanceArray[0];
		double _distance = 0;
		if (distanceStr.length() > 0) {
			_distance = Double.parseDouble(distanceStr.substring(0, distanceStr.length() - 1));
		}
		String _distanceStr = String.valueOf(_distance);
		_distanceStr = _distanceStr.substring(0, _distanceStr.indexOf("."));
		distance = Integer.parseInt(_distanceStr);
		// 将可选内容与ArrayAdapter连接起来   
		distanceAdapter = new ArrayAdapter<String>(theAct, R.layout.spinner_style, distanceArray);   
        // 设置下拉列表的风格   
		distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
		distanceAdapter.notifyDataSetChanged();
        // 设置默认值   
		theAct.distanceSprinner.setAdapter(distanceAdapter);
		theAct.distanceSprinner.setSelection(0,true);
		theAct.distanceSprinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view,int position, long id) {
				String distanceStr = distanceArray[position];
				double _distance = 0;
				if (distanceStr.length() > 0) {
					if (position == 0) {
						_distance = Double.parseDouble(distanceStr.substring(0, distanceStr.length() - 1));
					} else {
						_distance = Double.parseDouble(distanceStr.substring(0, distanceStr.length() - 2)) * 1000;
					}
				}
				String _distanceStr = String.valueOf(_distance);
				_distanceStr = _distanceStr.substring(0, _distanceStr.indexOf("."));
				distance = Integer.parseInt(_distanceStr);
				if(mAdapter!=null)
					mAdapter.distance = distance;
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {				
			}
        });
		
		theAct.searchImgBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				theAct.hideKeyboard(v);
				if ("search".equals(theAct.searchImgBtn.getTag())) {
					doSearch(theAct.searchEdit.getText().toString(), distance);
				} else {
					theAct.showIsrDialog();
				}
			}
		});
	}
	/**
	 * 进行搜索
	 */
	public void doSearch(String keywords, int distance) {
		if (keywords.length() == 0) {
			UserAppSession.showToast(theAct, "请输入关键字！");
			return;
		}
		
		Bundle bundle = new Bundle();
		bundle.putString("keywords", keywords);
		bundle.putInt("distance", distance);
		AppUtil.startActivity(theAct, PoiListActivity.class, false, bundle);
	}
	
	RecognizerDialogListener recoListener = new RecognizerDialogListener() {
		// 识别内容
		String recoText = "";
		@Override
		public void onResults(ArrayList<RecognizerResult> results, boolean isLast) {
			String _text = results.get(0).text;
			if (_text != null && _text.length() > 0) {
				recoText = _text;
			}
		}
		
		@Override
		public void onEnd(SpeechError error) {
			if(error == null) {
				theAct.searchEdit.setText(recoText);
				//doSearch(recoText, distance);
			}
		}
	};
	/**
	 * 显示数据
	 * @param resData
	 */
	public void fillData(List<Map<String,Object>> typeList) {
		if(typeList==null)
			return;
		//生成分组
		groups = new ArrayList<Map<String,Object>>();
		for (Map<String,Object> item: typeList) {
			if ("0".equals(item.get("PID"))) 
				groups.add(item);
		}

		//每一组的子项
		for(Map<String,Object> g:groups){
			String id=(String)g.get("ID");
			List<Map<String, Object>> childs = new ArrayList<Map<String,Object>>();
			for (Map<String,Object> item: typeList) {
				if (id.equals(item.get("PID"))) 
					childs.add(item);
			}
			g.put("childs", childs);
			g.put("isOpen", "0");
		}
		
		showSort();
	}
	private void showSort() {
		// 实例化adapter
		mAdapter = new NearbySortAdapter(theAct, groups, distance, nearbyDataIntf.iconList);
		ExpandableListView expandableListView = (ExpandableListView) theAct.findViewById(R.id.list);
		expandableListView.setOnGroupCollapseListener(onGroupCollapseListener);  
		expandableListView.setOnGroupExpandListener(onGroupExpandListener); 
		expandableListView.setGroupIndicator(null);
		expandableListView.setChildDivider(theAct.getResources().getDrawable(R.drawable.line));
		expandableListView.setOnChildClickListener(new OnChildClickListener() {
			//监听下拉事件
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				return false;
			}
		});
		// 为列表设置adapter
		expandableListView.setAdapter(mAdapter);
	}
	/**
	 * 合并监听.
	 */
	OnGroupCollapseListener onGroupCollapseListener = new OnGroupCollapseListener() {  
        @Override  
        public void onGroupCollapse(int groupPosition) {
    		Map<String, Object> g = groups.get(groupPosition);
    		g.put("isOpen", "0");  
        }  
    };  
    /**
	 * 展开监听.
	 */  
    OnGroupExpandListener onGroupExpandListener = new OnGroupExpandListener() {  
        @Override  
        public void onGroupExpand(int groupPosition) {
    		Map<String, Object> g = groups.get(groupPosition);
    		g.put("isOpen", "1");
        }  
    };
}
