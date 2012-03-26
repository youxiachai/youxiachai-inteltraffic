package com.dracode.autotraffic.serves.taxicall;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Message;

public class TaxiCallMapIndexHelper {

	public Timer taxiTimer = null;
	public TimerTask taxiTimerTask = null;
	public TaxiCallMapIndex theAct = null;
	
	public void init(Activity activi){
		theAct = (TaxiCallMapIndex) activi;
		resetTaxiTimer(2000);
	}
	
	protected void resetTaxiTimer(int tm) {
		killTaxiTimer();
		taxiTimer = new Timer();
		taxiTimerTask = new TimerTask() {
			public void run() {
				taxiTimer = null;
				theAct.handler.sendMessage(Message.obtain(theAct.handler, TaxiCallMapIndex.SEARCH_REFRESH));
			}
		};
		taxiTimer.schedule(taxiTimerTask, tm);
	}

	protected void killTaxiTimer() {
		if (taxiTimer != null) {
			taxiTimer.cancel();
		}
	}
}
