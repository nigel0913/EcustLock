package com.nigel.ecustlock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.nigel.service.TrainService;
import com.support.Cfg;
import com.support.GetMfcc;
import com.support.mfcc.Mfcc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TrainActivity extends Activity {

	Button btnTrain = null;
	TextView tvInfo = null;
	
	int audioSource = MediaRecorder.AudioSource.MIC;
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int bufferSizeInBytes = 0;
	boolean isRecording = false;
	AudioRecord audioRecord = null;
	
	MfccTask mfccTask = null;
	TrainBroadcastReceiver trainReceiver = new TrainBroadcastReceiver();

	String ac_tag = "TrainActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_train);

		Log.v(ac_tag, "onCreate()");
		
		this.btnTrain = (Button) super.findViewById(R.id.btn_train);
		this.tvInfo = (TextView) super.findViewById(R.id.tv_info);
		
		this.btnTrain.setOnClickListener(new TrainOnClickListenserImpl());
//		this.btnTest.setOnClickListener(null);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
	}

	@Override
	protected void onDestroy() {
		Log.v(ac_tag, "onDestroy()");
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(TrainService.ACTION_FINISH_TRAIN);
		registerReceiver(trainReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver(trainReceiver);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		audioRecord = new AudioRecord(audioSource, sampleRateInHz,
				channelConfig, audioFormat, bufferSizeInBytes);
		isRecording = false;
		
		if (TrainService.getStatus() == TrainService.Status.TRAINING) {
			String trainer = TrainService.getTrainer();
			tvInfo.setText("正在训练" + trainer + "中");
			btnTrain.setText("正在训练" + trainer + "中");
			btnTrain.setEnabled(false);
		}
		else {
			btnTrain.setText("开始录音");
			btnTrain.setEnabled(true);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (mfccTask != null && mfccTask.getStatus() == AsyncTask.Status.RUNNING) {
			mfccTask.cancel(true);
		}
		isRecording = false;
		if (audioRecord != null) {
			audioRecord.stop();
			audioRecord.release();
			audioRecord = null;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.i(ac_tag, "");
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			return super.onKeyDown(keyCode, event);
		case KeyEvent.KEYCODE_MENU:
			return super.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private class TrainOnClickListenserImpl implements OnClickListener {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
				case R.id.btn_train:
					Log.d("TrainService status", ""+TrainService.getStatus());
					if (isRecording == true) {
						btnTrain.setText("开始录音");
						isRecording = false;
						if (audioRecord != null) {
							audioRecord.stop();
						}
	
					} else {
						if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
							isRecording = false;
							TrainActivity.this.tvInfo.setText("录音设备初始化失败");
						} 
						else {
							audioRecord.startRecording();
							btnTrain.setText("结束录音");
							btnTrain.setEnabled(true);
							isRecording = true;
							Log.d("isRecording", ""+isRecording);
							
//							if (mfccTask != null && mfccTask.getStatus() == AsyncTask.Status.RUNNING) {
//								mfccTask.cancel(true);
//								mfccTask = null;
//							}
							mfccTask = new MfccTask();
							mfccTask.execute();
						}
					}
					break;
					
				default:
					break;
				
			}
			
		}

	}
	
	public class MfccTask extends AsyncTask<Void, String, String> {

		@Override
		protected String doInBackground(Void... progress) {
			
//			String rootDir = Cfg.getInstance().getRootDir();
//			String username = Cfg.getInstance().getUserName();
			
			this.publishProgress("正在录音...");
			
			short[] audioData = new short[bufferSizeInBytes+1];
			double[] inSamples = new double[bufferSizeInBytes+1];
			int readsize = 0;
			
			// write raw file
			PrintWriter writer = null;
			try {
				File txt = new File(Cfg.getInstance().getRootDir() + Cfg.getInstance().getTmpPath() 
						+ File.separator + Cfg.getInstance().getUserName() + ".txt");
				if (txt.exists()) {
					txt.delete();
				}
				writer = new PrintWriter(txt);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			// feature file
			File fileMfcc = new File(Cfg.getInstance().getRootDir() + Cfg.getInstance().getTmpPath() + 
					File.separator + Cfg.getInstance().getUserName() + Cfg.getInstance().getFeaSuf());
			if (fileMfcc.exists()) {
				fileMfcc.delete();
			}
			
			while (isRecording == true) {
				readsize = audioRecord.read(audioData, 0, bufferSizeInBytes);
				
				if (AudioRecord.ERROR_INVALID_OPERATION != readsize
						&& AudioRecord.ERROR_BAD_VALUE != readsize) {

					Log.d("readsize", ""+readsize);
					for (int i=0; i<readsize; i++){
						inSamples[i+1] = audioData[i];
						
						writer.println(""+audioData[i]);
					}

					Mfcc.getInstance().write(fileMfcc, inSamples, readsize);
				}
			}
			
			writer.close();
			
			if (isCancelled()) {
				Log.d("MfccTask", "isCanceled()2");
				return null;
			}
			
			return "正在训练模型...";
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			if (isCancelled()) {
				TrainActivity.this.tvInfo.setText("被取消");
				return ;
			}
			TrainActivity.this.tvInfo.setText(values[0]);
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(String result) {
			TrainActivity.this.btnTrain.setEnabled(false);
			TrainActivity.this.tvInfo.setText(result);
			super.onPostExecute(result);
			
			Intent trainService = new Intent(TrainActivity.this, TrainService.class);
			trainService.putExtra(TrainService.EXTRA_TRAINER, Cfg.getInstance().getUserName());
			TrainActivity.this.startService(trainService);
		}
		
	}
	
	public class TrainBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context content, Intent intent) {
			String action = intent.getAction();
			if (action.equals(TrainService.ACTION_FINISH_TRAIN)) {
				TrainActivity.this.btnTrain.setText("开始录音");
				TrainActivity.this.btnTrain.setEnabled(true);
				TrainActivity.this.tvInfo.setText("训练完成");
			}
		}
		
	}

}
