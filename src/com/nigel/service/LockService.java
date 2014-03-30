package com.nigel.service;


import com.nigel.ecustlock.LockActivity;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class LockService extends Service {

	private final String LOG_TAG = "LocalService";
	private BroadcastReceiver receiverOff;
	private IntentFilter filterOff;
	
	private final static String PREF_IS_RUNNING = "ServiceRunning";
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "onCreate()");
		super.onCreate();
		registerIntentReceivers();
		setRunning(true);
		
		// prevent service killed by other app
		// use startForeground(), this format will prevent successfully and won't show notification
		// this can use settings and let user decide which format (whether show the notification)
		startForeground(1, new Notification());
		
		Toast.makeText(getApplicationContext(), "声音认证服务已经启动", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
		return START_STICKY;
//		return START_REDELIVER_INTENT;
	}
	
	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "onDestroy()");
		super.onDestroy();
		setRunning(false);
		stopForeground(true);
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
		receiverOff = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				Log.d(LOG_TAG, "receive SCREEN_OFF");
				Intent startMain = new Intent(context, LockActivity.class);
				startMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(startMain);
			}
		};
		registerReceiver(receiverOff, filterOff);
	}
	

}
