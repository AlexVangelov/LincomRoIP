package com.lincomengineering.lincomroip;


import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lincomengineering.lincomroip.LCRService.LocalBinder;

public class MainActivity extends Activity {
	public native void setEnv();
	private ImageButton btnPtt;
	private SeekBar seekSql;
	private TextView txtContact, txtChan;
	private Button btnConnect, btnWatt;
	private int current_call_id = -1;
	LCRService mService;
    boolean mBound = false;
    private Context context;
    static Handler h;
    private int connected = 0;

    static {
        System.loadLibrary("lincomroip");
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        txtContact = (TextView) findViewById(R.id.txtContact);
        txtContact.setText("");
        txtChan = (TextView) findViewById(R.id.textChan);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnWatt = (Button) findViewById(R.id.btnWatt);
        btnWatt.setText("--W");
        btnPtt = (ImageButton) findViewById(R.id.btnPtt);
        h = new CallbackHandler(context, txtChan, btnWatt);
        btnPtt.setOnTouchListener(new OnTouchListener() {
        	
			@Override
			public boolean onTouch(View v, MotionEvent event) {
    	        switch ( event.getAction() ) {
    		        case MotionEvent.ACTION_DOWN: 
    		        	if (current_call_id != -1) {
    		        		//lincomroip.sendmsgLincomRoIP(current_call_id, "J1");
    		        		SendLinCmd("J1");
    		        	} else {
    		        		Toast.makeText(getApplicationContext(), "No active connection!", Toast.LENGTH_SHORT).show();
    		        	}
    		        	break;
    		        case MotionEvent.ACTION_UP: 
    		        	if (current_call_id != -1) {
    		        		//lincomroip.sendmsgLincomRoIP(current_call_id, "J0");
    		        		SendLinCmd("J0");
    		        	} else {
    		        		Toast.makeText(getApplicationContext(), "No active connection!", Toast.LENGTH_SHORT).show();
    		        	}
    		        	break;
    	        }
    	        return false;
    	    }
        });
        seekSql = (SeekBar) findViewById(R.id.seekSql);
        seekSql.setProgress(30);
        seekSql.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int progress = ((SeekBar)v).getProgress();
    	        switch ( event.getAction() ) {
    		        case MotionEvent.ACTION_UP: 
    		        	if (current_call_id != -1) {
    		        		String.format("S%X", (progress*15)/100);
    		        		//lincomroip.sendmsgLincomRoIP(current_call_id, String.format("S%X", (progress*15)/100));
    		        		SendLinCmd(String.format("S%X", (progress*15)/100));
    		        	} else {
    		        		Toast.makeText(getApplicationContext(), "No active connection!", Toast.LENGTH_SHORT).show();
    		        	}
    		        	break;
    	        }
    	        return false;
    	    }
        });
        /* Load native library */
//        try {
//        	//System.loadLibrary("stlport_shared");
//        	System.loadLibrary("lincomroip");
//        	Log.d("LincomRoIP","Library load OK\n");
//        	Toast.makeText(getApplicationContext(), "Library load OK", Toast.LENGTH_SHORT).show();
//        } catch (UnsatisfiedLinkError e) {
//        	Log.d("LincomRoIP",e.getMessage());
//        	this.finish();
//        	return;
//        }
        //setEnv();
        //lincomroip.initLincomRoIP();

        //lincomroip.setToastCallback((SWIGTYPE_p_void)toastCallback);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
//        Intent intent = new Intent(this, LCRService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //setEnv();
        lincomroip.initLincomRoIP();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
        lincomroip.destroyLincomRoIP();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //lincomroip.destroyLincomRoIP();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.exit:
        	exitApp();
            return true;
        case R.id.settings:
        	Intent settingsActivity = new Intent(getBaseContext(),MainPreferences.class);
        	startActivity(settingsActivity);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void exitApp() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Are you sure you want to exit?")
    	       .setCancelable(false)
    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   MainActivity.this.finish();
    	           }
    	       })
    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	builder.setIcon(R.drawable.ic_launcher);
    	builder.setTitle(R.string.app_name);
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    public void doConnect(View v) {
    	if (connected == 0) {
	    	String contact = String.format("sip:%s", txtContact.getText().toString());
	    	current_call_id = lincomroip.connectLincomRoIP(contact);
	    	btnConnect.setText("Disconnect");
	    	connected = 1;
    	} else {
    		lincomroip.disconnectLincomRoIP();
    		btnConnect.setText("Connect");
    		btnWatt.setText("--W");
    		txtChan.setText("--");
    		connected = 0;
    	}
	}
    
    public void chanUp(View v) {
    	if (current_call_id != -1) {
    		//lincomroip.sendmsgLincomRoIP(current_call_id, "1");
    		SendLinCmd("1");
    	} else {
    		Toast.makeText(getApplicationContext(), "No active connection!", Toast.LENGTH_SHORT).show();
    	}
	}
    
    public void chanDown(View v) {
    	if (current_call_id != -1) {
    		//lincomroip.sendmsgLincomRoIP(current_call_id, "5");
    		SendLinCmd("5");
    	} else {
    		Toast.makeText(getApplicationContext(), "No active connection!", Toast.LENGTH_SHORT).show();
    	}
	}
    
    public void changeWatt(View v) {
    	if (current_call_id != -1) {
    		//lincomroip.sendmsgLincomRoIP(current_call_id, "W");
    	} else {
    		Toast.makeText(getApplicationContext(), "No active connection!", Toast.LENGTH_SHORT).show();
    	}
	}
    
    public void sendMsg(View v) {
    	lincomroip.sendmsgLincomRoIP(current_call_id, "alabala");
	}
    
    public void toastCallback(String s) {
    	Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    
    public void pagerCallback(String s) {
    	//Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    	Log.d("LincomRoIP","pagerCallback\n");
    }

    public void SendLinCmd(String s) {
    	btnWatt.setText("--W");
		txtChan.setText("--");
    	lincomroip.sendmsgLincomRoIP(current_call_id, s);
    }

    public void callBack(String s) {
    	Log.d("LincomRoIP",s);
    	try {
    		Bundle b = new Bundle();
            b.putString("callback_string", s);
            Message m = Message.obtain();
            m.setData(b);
            m.setTarget(h);
            m.sendToTarget();
    	} catch (Exception e) {
    		Log.e("LincomRoIP","eeeerrrorrr");
    	}
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            //mService.retrieveClockHS();
            //ClockHSActivity.this.loadWeb();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    static class CallbackHandler extends Handler {
    	TextView ctxtChan; 
    	Button cbtnWatt;
    	Context ccontext;
    	CallbackHandler(Context ctxt, TextView chan, Button watt) {
    		ccontext = ctxt;
    		ctxtChan = chan;
    		cbtnWatt = watt;
        }
    	
    	@Override
    	public void handleMessage(Message msg) {
       	 int chan;
            Bundle b = msg.getData();
            String status = b.getString("callback_string");
            //Toast.makeText(context, status.substring(0,8), Toast.LENGTH_SHORT).show();
            if (((String)status.substring(0,8)).equals("Channel:")) {
	             chan = Integer.parseInt(status.substring(8, 11))-233;
	             ctxtChan.setText(String.format("%02d", chan));
	             cbtnWatt.setText(String.format("%sW", status.substring(12, 14)));
       	 } else Toast.makeText(ccontext, status, Toast.LENGTH_SHORT).show();
        }
    }
}
