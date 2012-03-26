package com.dracode.andrdce.ct;

import android.content.Context;

public class DceJni {

	public static Context appContext=null;
	public static DceJni dceJni=null;

    public native String jniCall(String cmd, String param);

	public static Object getActMain() {
		if(appContext==null)
			return null;
		else{
			return appContext;
		}
	}
	
	public static DceJni getDceJni(){
		if(getActMain()==null)
			return null;
		if(dceJni==null)
			dceJni=new DceJni();
		return dceJni;
	}

    static {
        System.loadLibrary("andrdce-jni");
    }
}
