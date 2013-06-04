package com.lincomengineering.lincomroip;


import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public native void setEnv();
	private Button btnPtt;
	private SeekBar seekSql;
	private TextView txtContact, txtChan;
	private Button btnConnect, btnWatt;
	private int current_call_id = -1;
    boolean mBound = false;
    private Context context;
    private CustomHandler h;
    private int connected = 0;
    SharedPreferences preferences;

    static {
        System.loadLibrary("lincomroip");
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main2);
        //txtContact = (TextView) findViewById(R.id.txtContact);
        //txtContact.setText("100@78.90.112.221");
        txtChan = (TextView) findViewById(R.id.textChan);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        //btnWatt = (Button) findViewById(R.id.btnWatt);
        //btnWatt.setText("--W");
        btnPtt = (Button) findViewById(R.id.btnPtt);
        h = new CustomHandler(this);
//        h = new Handler() {
//        	           public void handleMessage(Message msg) {
//        	             int chan;
//        	               Bundle b = msg.getData();
//        	               String status = b.getString("callback_string");
//        	               //Toast.makeText(context, status.substring(0,8), Toast.LENGTH_SHORT).show();
//        	               if (status.length() > 14) {
//        	                 if (((String)status.substring(0,8)).equals("Channel:")) {
//        	                   chan = Integer.parseInt(status.substring(8, 11))-233;
//        	                   txtChan.setText(String.format("%02d", chan));
//        	                   btnWatt.setText(String.format("%sW", status.substring(12, 14)));
//        	               } else Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
//        	               } else Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
//        	           }
//        	       };
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
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
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        lincomroip.initLincomRoIP(
        		preferences.getString("sip_server", "78.90.112.221"),
        		preferences.getString("name", ""),
        		preferences.getString("password", "")
        );
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        lincomroip.destroyLincomRoIP();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    
    private void showToast(String txt) {
    	Toast.makeText(context, txt, Toast.LENGTH_SHORT).show();
    }
    
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
       	 //int chan;
            Bundle b = msg.getData();
            String status = b.getString("callback_string");
            Toast.makeText(ccontext, status.substring(0,8), Toast.LENGTH_SHORT).show();
//            if (((String)status.substring(0,8)).equals("Channel:")) {
//	             chan = Integer.parseInt(status.substring(8, 11))-233;
//	             ctxtChan.setText(String.format("%02d", chan));
//	             cbtnWatt.setText(String.format("%sW", status.substring(12, 14)));
//       	 } else Toast.makeText(ccontext, status, Toast.LENGTH_SHORT).show();
        }
    }
    
    static class CustomHandler extends Handler
    {
        WeakReference<MainActivity> mActivity;

        CustomHandler(MainActivity aFragment) 
        {
            mActivity = new WeakReference<MainActivity>(aFragment);
        }
        @Override
        public void handleMessage(Message msg) 
        {
            MainActivity theActivity = mActivity.get();
            //theActivity.updateUI();  
            //int chan;
            Bundle b = msg.getData();
            String status = b.getString("callback_string");
            //Toast.makeText(ccontext, status.substring(0,8), Toast.LENGTH_SHORT).show();
            theActivity.showToast(status.substring(0,8));
        }
    }
}
