package com.dracode.autotraffic.main;

import android.content.Intent;
import android.os.Handler;
import android.widget.ImageView;

import com.dracode.andrdce.ct.AutoUpdateUtil;
import com.dracode.autotraffic.R;

public class WelcomeActivityHelper {
	public WelcomeActivity theAct;

	private boolean autoUpdateInited=false;
	public void init(WelcomeActivity act) {
		theAct=act;
	}
	
	private Handler mHandler = new Handler();

	public void startProgressAnim() {
		mAniRunning = true;
		imgAniCounter = -1;

		imgAniPP=new ImageView[8];
		imgAniPP[0]=(ImageView)theAct.findViewById(R.id.imgLoadingP0);
		imgAniPP[1]=(ImageView)theAct.findViewById(R.id.imgLoadingP1);
		imgAniPP[2]=(ImageView)theAct.findViewById(R.id.imgLoadingP2);
		imgAniPP[3]=(ImageView)theAct.findViewById(R.id.imgLoadingP3);
		imgAniPP[4]=(ImageView)theAct.findViewById(R.id.imgLoadingP4);
		imgAniPP[5]=(ImageView)theAct.findViewById(R.id.imgLoadingP5);
		imgAniPP[6]=(ImageView)theAct.findViewById(R.id.imgLoadingP6);
		imgAniPP[7]=(ImageView)theAct.findViewById(R.id.imgLoadingP7);
		
		mHandler.postDelayed(mAniTask, 200);
	}
	
	public void showMainAct(){
		if(theAct.isFinishing())
			return;
		theAct.startActivity(new Intent(theAct, MainActivity.class));
		theAct.finish();
	}

	private boolean mAniRunning = true;
	public ImageView imgAniPP[] = null;
	private int imgAniCounter = 0;
	private int totalAniCounter=0;
	private Runnable mAniTask = new Runnable() {
		public void run() {
			if(!autoUpdateInited){
				autoUpdateInited=true;
				AutoUpdateUtil.currentUpdater().checkAutoUpdate(theAct, theAct.getResources().getString(R.string.app_compare_version));
			}
			imgAniCounter++;
			totalAniCounter++;
			if(totalAniCounter>=8){
				showMainAct();
				return;
			}
			if (imgAniCounter == 7) {
				imgAniCounter = 0;
			}
			for(int i=0;i<8;i++){
				if(i==imgAniCounter || i==imgAniCounter+1)
					imgAniPP[i].setSelected(true);
				else
					imgAniPP[i].setSelected(false);
			}
			if (mAniRunning) {
				mHandler.postDelayed(this, 200);
			}
		}
	};

	public void stopProgressAnim() {
		mAniRunning = false;
		mHandler.removeCallbacks(mAniTask);
	}
	
}
