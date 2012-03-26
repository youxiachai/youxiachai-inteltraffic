package com.dracode.autotraffic.bus.buschange;

import java.util.List;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.bus.buschange.BusChangeInfo.ExchangePlan;
import com.dracode.autotraffic.bus.buschange.BusChangeInfo.Segment;
import com.dracode.autotraffic.bus.busline.BusLineResultActivity;
import com.dracode.autotraffic.bus.busstation.BusStationResultActivity;
import com.dracode.autotraffic.common.helpers.ShareHelper;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BusChangeAdapter extends ArrayAdapter<ExchangePlan> {

		protected ListView listView;
		private List<ExchangePlan> dataList;
		private BusChangeInfo curBusChgInfo;
		private Activity context;
		private int busStationCounnt = 0;
		private RelativeLayout relative01, relative02, relative03;

		public BusChangeAdapter(Activity activity,BusChangeInfo busChgInfo, ListView listView) {
			super(activity, 0, busChgInfo.exchangePlans);
			this.context = activity;
			this.listView = listView;
			this.dataList = busChgInfo.exchangePlans;
			curBusChgInfo=busChgInfo;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// Inflate the views from XML
			if (convertView == null) {
				LayoutInflater inflater = ((BusChangeResultActivity) context).getLayoutInflater();
				convertView = inflater.inflate(R.layout.bus_change_query_result_list_item, null);
			} 	
			
			final ExchangePlan exchgPlan = dataList.get(position);
			List<Segment> segmentList = exchgPlan.segmentList;		
			
			relative01 = (RelativeLayout) convertView.findViewById(R.id.layout_01);		
			relative02 = (RelativeLayout) convertView.findViewById(R.id.layout_02);		
			relative03 = (RelativeLayout) convertView.findViewById(R.id.layout_03);
			
			switch(segmentList.size()){
			case 1:
				fillBusData01( segmentList.get(0),convertView );
				busStationCounnt = Integer.valueOf(segmentList.get(0).getPassDepotCount());
				break;
			case 2:
				fillBusData01( segmentList.get(0),convertView );
				relative01.setVisibility(View.VISIBLE);
				fillBusData02( segmentList.get(1),convertView );
				busStationCounnt = Integer.valueOf(segmentList.get(0).getPassDepotCount())
				                   + Integer.valueOf(segmentList.get(1).getPassDepotCount());
				break;
			case 3:
				fillBusData01( segmentList.get(0),convertView );
				relative01.setVisibility(View.VISIBLE);
				fillBusData02( segmentList.get(1),convertView );
				relative02.setVisibility(View.VISIBLE);
				fillBusData03( segmentList.get(2),convertView );
				busStationCounnt = Integer.valueOf(segmentList.get(0).getPassDepotCount())
                                   + Integer.valueOf(segmentList.get(1).getPassDepotCount())
                                   + Integer.valueOf(segmentList.get(2).getPassDepotCount());
				break;
			case 4:
				fillBusData01( segmentList.get(0),convertView );
				relative01.setVisibility(View.VISIBLE);
				fillBusData02( segmentList.get(1),convertView );
				relative02.setVisibility(View.VISIBLE);
				fillBusData03( segmentList.get(2),convertView );
				relative03.setVisibility(View.VISIBLE);
				fillBusData04( segmentList.get(3),convertView );
				busStationCounnt = Integer.valueOf(segmentList.get(0).getPassDepotCount())
					                + Integer.valueOf(segmentList.get(1).getPassDepotCount())
					                + Integer.valueOf(segmentList.get(2).getPassDepotCount())
					                + Integer.valueOf(segmentList.get(3).getPassDepotCount());
				break;
			}
			TextView text1 = (TextView) convertView.findViewById(R.id.text01);				
			text1.setText("方案"+(position+1)+"：约("+busStationCounnt+"站)");
			
			/** 复制.*/
			ImageButton copy_bt = (ImageButton) convertView.findViewById(R.id.list_bt);
			copy_bt.setTag(exchgPlan);
			copy_bt.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Object o=v.getTag();
					if(o instanceof ExchangePlan){
						ShareHelper.copyMsgToClipboard(context, "BUSCHANGEPLAN", curBusChgInfo.start+'-'+curBusChgInfo.end+":"+((ExchangePlan)o).getShareText());
					}
				}
			});

			/** 分享.*/
			ImageButton share_bt = (ImageButton) convertView.findViewById(R.id.map_bt);
			share_bt.setTag(exchgPlan);
			share_bt.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Object o=v.getTag();
					if(o instanceof ExchangePlan){
						ShareHelper.shareMessage(context, "BUSCHANGEPLAN", curBusChgInfo.start+'-'+curBusChgInfo.end+":"+((ExchangePlan)o).getShareText());
					}
				}
			});
			return convertView;
		}
		
		private void fillBusData01( Segment segmentObj01,View convertView ){
			TextView text2 = (TextView) convertView.findViewById(R.id.text02);
			showTextLink(text2,segmentObj01.getStartName(),segmentObj01.getShortBusName()
					,segmentObj01.getEndName(),1);
		}
		
		private void fillBusData02( Segment segmentObj02,View convertView ){
			TextView text3 = (TextView) convertView.findViewById(R.id.text03);
			showTextLink(text3,segmentObj02.getStartName(),segmentObj02.getShortBusName()
					,segmentObj02.getEndName(),2);
		}
		
		private void fillBusData03( Segment segmentObj03,View convertView ){
			TextView text14 = (TextView) convertView.findViewById(R.id.text4);
			showTextLink(text14,segmentObj03.getStartName(),segmentObj03.getShortBusName()
					,segmentObj03.getEndName(),3);
		}
		
		private void fillBusData04( Segment segmentObj04,View convertView ){
			TextView text5 = (TextView) convertView.findViewById(R.id.text5);
			showTextLink(text5,segmentObj04.getStartName(),segmentObj04.getShortBusName()
					,segmentObj04.getEndName(),4);
		}
		//组装显示的文字
		String htmlLinkText = "";
		/**
		 * 设置文字超链接的效果
		 * @param textView
		 * @param showtext
		 * @param showtext1
		 * @param showtext2
		 */
		private void showTextLink(TextView textView,String showtext,String showtext1,String showtext2,int n){
			if(n==1){
				htmlLinkText =
				  "在  "+"<a style=\"{text-decoration:none;color:red;}\" href=\""+showtext+"公交站点"+"\">"+showtext+"</a>"
				+" 坐  "+"<a href=\""+showtext1+"公交线路"+"\">"+showtext1+"</a>"
				+" 到  "+"<a href=\""+showtext2+"公交站点"+"\">"+showtext2+"</a>";
			} else {
				htmlLinkText = "换乘  "+"<a href=\""+showtext1+"公交线路"+"\">"+showtext1+"</a>"
				+" 到  "+"<a href=\""+showtext2+"公交站点"+"\">"+showtext2+"</a>";
			}
			
			textView.setText(Html.fromHtml(htmlLinkText));
			textView.setMovementMethod(LinkMovementMethod.getInstance());
			CharSequence text = textView.getText();
			  if (text instanceof Spannable) {
				    int end = text.length();
				    Spannable sp = (Spannable) textView.getText();
				    URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
				    SpannableStringBuilder style = new SpannableStringBuilder(text);
				    style.clearSpans();// should clear old spans
				    for (URLSpan url : urls) {
					     MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
					     style.setSpan(myURLSpan, sp.getSpanStart(url), sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					     style.setSpan(new ForegroundColorSpan(Color.rgb(58, 147, 199)), sp.getSpanStart(url), sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				    }
				    textView.setText(style);
			 }
		}
		public class MyURLSpan extends ClickableSpan {

			protected String mUrl;

			MyURLSpan(String url) {
				mUrl = url;
			}

			@Override
			public void onClick(View widget) {
				int index = mUrl.indexOf("公交站点");
				if(index!=-1){	
					Bundle bundle = new Bundle();
					bundle.putString("QueryStationName", mUrl.substring(0, index));
					bundle.putString("QueryCityName", UserAppSession.cur_CityCode);
					bundle.putString("QueryOptions", "[NOHIST]");
					AppUtil.startActivity(context, BusStationResultActivity.class, false,
							bundle);
				}
				int index1 = mUrl.indexOf("公交线路");
				if( index1!=-1 ){
					Bundle bundle = new Bundle();
					bundle.putString("QueryBusName", mUrl.substring(0, index1));
					bundle.putString("QueryCityName", UserAppSession.cur_CityCode);
					bundle.putString("QueryBusDir", "0");
					bundle.putString("QueryOptions", "[NOHIST]");
					AppUtil.startActivity(context, BusLineResultActivity.class, false,
							bundle);
				}
			}
		}
}
