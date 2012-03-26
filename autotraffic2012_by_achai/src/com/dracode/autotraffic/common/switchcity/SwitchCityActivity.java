package com.dracode.autotraffic.common.switchcity;

import android.app.Activity;
import android.content.Context;

import java.util.regex.Pattern;  
import android.graphics.PixelFormat;  
import android.os.Bundle;  
import android.os.Handler;  
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;  
import android.view.WindowManager;  
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;  
import android.widget.RelativeLayout;
import android.widget.TextView;  

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.switchcity.MyLetterListView.OnTouchingLetterChangedListener;

public class SwitchCityActivity extends Activity {

	public SwitchCityActivityHelper theHelper = new SwitchCityActivityHelper(); 
    public ListView personList;  
    public TextView overlay;  
    private MyLetterListView letterListView;
    private Handler handler;  
    private OverlayThread overlayThread;  
    
    private TextView title_view;
    private ImageView fresh_img;
    private TextView fresh_text;
    private EditText input_view;
    private RelativeLayout back_relative;
    private RelativeLayout fresh_relative;

	private boolean isToClose = true;
    
    @Override    
    public void onCreate(Bundle savedInstanceState) {    
        super.onCreate(savedInstanceState);    
        setContentView(R.layout.act_switch_city);    
   	
    	initControls();
        handler = new Handler();  
        overlayThread = new OverlayThread();  
        initOverlay(); 
	    theHelper.init(this);
    }    
    private void initControls(){
          personList = (ListView) findViewById(R.id.list_view);  
          letterListView = (MyLetterListView) findViewById(R.id.MyLetterListView01);  
          letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());  
            
          title_view = (TextView) findViewById(R.id.middle_title);

          title_view.setText(UserAppSession.cur_CityName);
          fresh_img = (ImageView) findViewById(R.id.right_img);
          fresh_img.setBackgroundResource(R.drawable.icon_refresh);
          
          fresh_text = (TextView) findViewById(R.id.right_text);
          fresh_text.setText("刷新");
          
          back_relative = (RelativeLayout) findViewById(R.id.left_layout);
          back_relative.setOnClickListener(new OnClickListener(){
  			@Override
  			public void onClick(View v) {
  				finish();
  			}
  		});
          fresh_relative = (RelativeLayout) findViewById(R.id.right_layout);
          fresh_relative.setOnClickListener(new OnClickListener(){
  			@Override
  			public void onClick(View v) {
  				UserAppSession.location_city = "";
  				theHelper.getSortList(true);
  				 //personList.setSelection(0);  
  			}
  		});
          input_view = (EditText) findViewById(R.id.search);
          input_view.addTextChangedListener(new TextWatcher() {                        

              @Override
              public void onTextChanged(CharSequence s, int start, int before, int count) {
                 String str = input_view.getText().toString();
                 if(!"".endsWith(str)){
              	   String letter = str.substring(0, 1).toUpperCase();    	   
              	   if(theHelper.alphaIndexer!=null && theHelper.alphaIndexer.get(letter) != null) {  
                         int position = theHelper.alphaIndexer.get(letter);  
                         //定位到选择的位置
                         personList.setSelection(position+1);  
                   }
                 }
              }              
              @Override
              public void beforeTextChanged(CharSequence s, int start, int count, int after) {
              }                    
              @Override
              public void afterTextChanged(Editable s) {
              }
          });   
          
    }
    @Override    
    protected void onResume() {    
    	isToClose = true;
        super.onResume();    
    }    
    WindowManager windowManager;
    //初始化汉语拼音首字母弹出提示框   
    private void initOverlay() {  
        LayoutInflater inflater = LayoutInflater.from(this);  
        overlay = (TextView) inflater.inflate(R.layout.switchcity_letter_selected, null);  
        overlay.setVisibility(View.INVISIBLE);  
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(  
                LayoutParams.WRAP_CONTENT,   
                LayoutParams.WRAP_CONTENT,  
                WindowManager.LayoutParams.TYPE_APPLICATION,  
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,  
                PixelFormat.TRANSLUCENT);  
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);  
        windowManager.addView(overlay, lp);  
    }  
      
    private class LetterListViewListener implements OnTouchingLetterChangedListener{  
  
        @Override  
        public void onTouchingLetterChanged(final String s) {
        	if(theHelper.alphaIndexer==null)
        		return;
            if(theHelper.alphaIndexer.get(s) != null) {  
                int position = theHelper.alphaIndexer.get(s);  
                //定位到选择的位置
                personList.setSelection(position+1);  
                overlay.setText(theHelper.sections[position]);
        	
                overlay.setText(s);
                overlay.setVisibility(View.VISIBLE);  
                handler.removeCallbacks(overlayThread);  
                //延迟一秒后执行，让overlay为不可见   
                handler.postDelayed(overlayThread, 1500);  
            }
        }  
          
    }  
     
    //设置overlay不可见   
    private class OverlayThread implements Runnable {  
  
        @Override  
        public void run() {  
            if(isToClose){
            	overlay.setVisibility(View.GONE);
            }            
        }  
          
    }    
    //获得汉语拼音首字母   
    public String getAlpha(String str) {    
        if (str == null) {    
            return "#";    
        }    
    
        if (str.trim().length() == 0) {    
            return "#";    
        }    
    
        char c = str.trim().substring(0, 1).charAt(0);    
        // 正则表达式，判断首字母是否是英文字母     
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");    
        if (pattern.matcher(c + "").matches()) {    
            return (c + "").toUpperCase();    
        } else {    
            return "#";    
        }    
    }    
    
    @Override
    public void onDestroy(){
    	if(overlay!=null){
       	 windowManager.removeView(overlay);
        }   
    	isToClose = false;    	
    	super.onDestroy(); 	 
    }
}
