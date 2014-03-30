package com.nigel.service;

import java.util.List;

import com.nigel.ecustlock.LockActivity;
import com.nigel.ecustlock.MainActivity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class LockService extends Service {

	private final String LOG_TAG = "LocalService";
	private BroadcastReceiver receiverOff, receiverOn;
	private IntentFilter filterOff, filterOn;
	private ActivityManager mActivityManager = null;
	private String mPackageName = "com.nigel.ecustlock.LockAcitity";
	
	private final static String PREF_IS_RUNNING = "ServiceRunning";
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "onCreate()");
		super.onCreate();
		registerIntentReceivers();
		setRunning(true);
		
		Toast.makeText(getApplicationContext(), "声音认证服务已经启动", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
//		return START_STICKY;
		return START_REDELIVER_INTENT;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "onDestroy()");
		super.onDestroy();
		setRunning(false);
		Toast.makeText(getApplicationContext(), "声音认证服务已经关闭", Toast.LENGTH_SHORT).show();
	}
	
	private void setRunning(boolean running) {
	    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    SharedPreferences.Editor editor = pref.edit();

	    editor.putBoolean(PREF_IS_RUNNING, running);
	    editor.apply();
	}

	public static boolean isRunning(Context ctx) {
	    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
	    return pref.getBoolean(PREF_IS_RUNNING, false);
	}

	private void registerIntentReceivers() {
		Log.i(LOG_TAG, "registerIntentReceivers()");
		filterOff = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		filterOn = new IntentFilter(Intent.ACTION_SCREEN_ON);

		receiverOff = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				Log.d(LOG_TAG, "receive SCREEN_OFF");
					Log.d(LOG_TAG, "is not TopRunning");
					Intent startMain = new Intent(context, LockActivity.class);
					startMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(startMain);
			}
		};
//		receiverOn = new BroadcastReceiver() {
//			public void onReceive(Context context, Intent intent) {
//				Log.d(LOG_TAG, "receive SCREEN_ON");
//				if ( !isTopRunning(getApplicationContext()) ) {
//					Intent startMain = new Intent(context, LockActivity.class);
//					startMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					context.startActivity(startMain);
//				}
//				setTopRunning(false);
//			}
//		};
//		registerReceiver(receiverOn, filterOn);
		registerReceiver(receiverOff, filterOff);
	}
	

}
