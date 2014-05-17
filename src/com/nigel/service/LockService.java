package com.nigel.service;


import com.nigel.ecustlock.AuthActivity;
import com.nigel.ecustlock.LockActivity;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LockService extends Service {

	private final String LOG_TAG = "LocalService";
	private BroadcastReceiver receiverOff;
	private IntentFilter filterOff;
	
	public enum Status {
		STOP, RUNNING
	}
	
	private static Status status = Status.STOP;
	
	public static Status getStatus() {
		return status;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.v(LOG_TAG, "onCreate()");
		super.onCreate();
		registerIntentReceivers();
		
		// prevent service killed by other app
		// use startForeground(), this format will prevent successfully and won't show notification
		// this can use settings and let user decide which format (whether show the notification)
		startForeground(1, new Notification());
		status = Status.RUNNING;
		Toast.makeText(getApplicationContext(), "声音认证服务已经启动", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.v(LOG_TAG, "onDestroy()");
		super.onDestroy();
		unregisterReceiver(receiverOff);
		stopForeground(true);
		status = Status.STOP;
		Toast.makeText(getApplicationContext(), "声音认证服务已经关闭", Toast.LENGTH_SHORT).show();
	}
	
	private void registerIntentReceivers() {
		Log.v(LOG_TAG, "registerIntentReceivers()");
		filterOff = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		receiverOff = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				Log.d(LOG_TAG, "receive SCREEN_OFF");
				Intent startMain = new Intent(context, AuthActivity.class);
				startMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(startMain);
			}
		};
		registerReceiver(receiverOff, filterOff);
	}
	

}
