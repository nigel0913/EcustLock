package com.nigel.service;

import java.io.File;

import com.nigel.ecustlock.LoginActivity;
import com.nigel.ecustlock.R;
import com.support.Cfg;
import com.support.FileAccess;
import com.support.Recognition;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

public class TrainService extends Service {

	public static final String EXTRA_TRAINER = "EXTRA_TRAINER";
	public static final String ACTION_FINISH_TRAIN = "com.nigel.ecustlock.ACTION_FINISH_TRAIN";
	
	static String trainer = "";
	

	ModelTask task = null;
	
	public static String getTrainer() {
		return trainer;
	}
	
	final static String tag = "TrainService";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		trainer = intent.getStringExtra(EXTRA_TRAINER);
		task = new ModelTask();
		task.execute();
		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (task != null) {
			task.cancel(true);
			task = null;
		}
		status = Status.STOP;
//		stopForeground(true);
	}
	
	public class ModelTask extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			TrainService.setStatus(TrainService.Status.TRAINING);
			this.publishProgress("正在训练" + trainer + "...");
			String rootDir = Cfg.getInstance().getRootDir();
			String tmpPath = rootDir + Cfg.getInstance().getTmpPath() + File.separator;
			Recognition.TrainGmm(
					rootDir + Cfg.getInstance().getWorldMdlPath() + File.separator,
					tmpPath,
					tmpPath,
					trainer
					);
			if (isCancelled()) {
				return null;
			}
			FileAccess.Move(tmpPath + trainer + Cfg.getInstance().getFeaSuf(), rootDir + Cfg.getInstance().getUsersPath() + File.separator + trainer + File.separator);
			FileAccess.Move(tmpPath + trainer + Cfg.getInstance().getMdlSuf(), rootDir + Cfg.getInstance().getUsersPath() + File.separator + trainer + File.separator);
			this.publishProgress("训练完成");
			TrainService.setStatus(TrainService.Status.RUNNING);
			Intent intent = new Intent();
			intent.setAction(ACTION_FINISH_TRAIN);
			sendBroadcast(intent);
			
			TrainService.this.stopSelf();
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			showNotification(values[0]);
			Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_SHORT).show();
			super.onProgressUpdate(values);
		}
		
	}
	
	private void showNotification(String info) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(TrainService.this);
		builder.setSmallIcon(R.drawable.ic_hourglass);
		builder.setContentTitle("训练模型");
		builder.setContentText(info);
		
		Intent resultIntent = new Intent(this, LoginActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(LoginActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		builder.setAutoCancel(false);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(913, builder.build());
//		startForeground(913, builder.build());
	}
	
	public enum ModelProcess {
		START, END
	}
	
	public enum Status {
		STOP, RUNNING, TRAINING
	}
	private static Status status = Status.STOP;
	public static void setStatus(Status status) {
		TrainService.status = status;
	}
	public static Status getStatus() {
		return status;
	}
}
