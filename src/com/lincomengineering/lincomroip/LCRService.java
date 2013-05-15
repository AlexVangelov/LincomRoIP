package com.lincomengineering.lincomroip;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class LCRService extends Service {

	private final IBinder mBinder = new LocalBinder();

//	static {
//	  System.loadLibrary("lincomroip");
//	}
	
	public class LocalBinder extends Binder {
		LCRService getService() {
			return LCRService.this;
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onCreate() {
	    super.onCreate();
	    try {
	    	//System.loadLibrary("stlport_shared");
	    	System.loadLibrary("lincomroip");
	    	Log.d("LincomRoIP","Library load OK\n");
	    	//Toast.makeText(getApplicationContext(), "Library load OK", Toast.LENGTH_SHORT).show();
	    } catch (UnsatisfiedLinkError e) {
	    	Log.d("LincomRoIP",e.getMessage());
	    	return;
	    }
	    Log.e("LincomRoIP", "Service Started.. ");
	    lincomroip.initLincomRoIP("","","");
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    lincomroip.destroyLincomRoIP();
	    Log.e("LincomRoIP", "Service Destroyed.. ");
	}
}
