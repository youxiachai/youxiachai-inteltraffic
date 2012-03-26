package com.dracode.autotraffic.common.map;

import java.util.List;
import java.util.Map;

import com.dracode.autotraffic.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;

public class MapSelectedMenuAdapter extends BaseAdapter {

	  private Context context; 
	  private List<Map<String, Object>> list_items; 
	  private SlidingDrawer mdrawer;
	  private MapFastJumpHelper mapFastHelper;
	 
	  public MapSelectedMenuAdapter(Context context,List<Map<String, Object>> items,SlidingDrawer mdrawer,MapFastJumpHelper mapFastHelper){ 
	    this.context=context; 
	    this.list_items=items; 
	    this.mdrawer = mdrawer;
	    this.mapFastHelper = mapFastHelper;
	  } 		 
	  @Override 
	  public int getCount() { 
	    return list_items.size(); 
	  } 		 
	  @Override 
	  public Object getItem(int arg0) { 
	    return list_items.get(arg0); 
	  } 	 
	  @Override 
	  public long getItemId(int position) { 
	    return position; 
	  } 		 
	  @Override 
	  public View getView(final int position, View convertView, ViewGroup parent) { 
		    LayoutInflater factory = LayoutInflater.from(context); 
		    if(convertView==null){
		    	convertView = (View) factory.inflate(R.layout.grid_item_menu, null);//绑定自定义的layout  			    	
		    }
		  
		    ImageView img = (ImageView) convertView.findViewById(R.id.item_image); 
		    TextView txv = (TextView) convertView.findViewById(R.id.area_name); 
		    img.setImageResource(R.drawable.icon_arrow); 
		    txv.setText(list_items.get(position).get("itemName").toString()); 
		    convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					mdrawer.close();
					mapFastHelper.gotoAreaItem(position);			
				}
			});
		    return convertView; 
	  }  
}

