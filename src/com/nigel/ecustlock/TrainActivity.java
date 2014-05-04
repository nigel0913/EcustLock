package com.nigel.ecustlock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.support.Cfg;
import com.support.FileAccess;
import com.support.GetMfcc;
import com.support.Recognition;
import com.support.mfcc.Mfcc;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TrainActivity extends Activity {

	Button btnTrain = null;
	Button btnTest = null;
	TextView tvInfo = null;
	
	int audioSource = MediaRecorder.AudioSource.MIC;
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int bufferSizeInBytes = 0;
	boolean isRecording = false;
	AudioRecord audioRecord = null;
	
	MfccTask mfccTask = null;

	String ac_tag = "TrainActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_train);

		Log.v(ac_tag, "onCreate()");
		
		this.btnTrain = (Button) super.findViewById(R.id.btn_train);
		this.tvInfo = (TextView) super.findViewById(R.id.tv_info);
		this.btnTest = (Button) super.findViewById(R.id.btn_test);
		
		this.btnTrain.setOnClickListener(new TrainOnClickListenserImpl());
//		this.btnTest.setOnClickListener(null);
		this.btnTest.setEnabled(false);
		
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		audioRecord = new AudioRecord(audioSource, sampleRateInHz,
				channelConfig, audioFormat, bufferSizeInBytes);
	}

	@Override
	protected void onDestroy() {
		Log.v(ac_tag, "onDestroy()");
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mfccTask != null && mfccTask.getStatus() == AsyncTask.Status.RUNNING) {
			this.btnTrain.setEnabled(false);
			this.tvInfo.setText("正在训练中");
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
			isRecording = false;
			if (audioRecord != null) {
				audioRecord.stop();
				audioRecord.release();
				audioRecord = null;
			}
			
			if (mfccTask != null && mfccTask.getStatus() == AsyncTask.Status.RUNNING) {
				Log.d("onKeyDown", "mfccTask.cancel(true)");
				mfccTask.cancel(true);
			}
			
			return super.onKeyDown(keyCode, event);
		case KeyEvent.KEYCODE_MENU:
			isRecording = false;
//			crossfade();
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
					if (isRecording == true) {
						btnTrain.setText("开始录音");
	
						isRecording = false;
						if (audioRecord != null) {
							Log.v("release","release");
							audioRecord.stop();
							audioRecord.release();
							audioRecord = null;
						}
	
					} else {
						
						if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
							isRecording = false;
							Log.d("isRecording", ""+isRecording);
						} 
						else {
							btnTrain.setText("结束录音");
							btnTrain.setEnabled(false);
							if (audioRecord == null) {
								audioRecord = new AudioRecord(audioSource, sampleRateInHz,
										channelConfig, audioFormat, bufferSizeInBytes);
							}
							audioRecord.startRecording();
							isRecording = true;
							Log.d("isRecording", ""+isRecording);
							
							if (mfccTask != null && mfccTask.getStatus() == AsyncTask.Status.RUNNING) {
								mfccTask.cancel(true);
								mfccTask = null;
							}
							mfccTask = new MfccTask();
							mfccTask.execute();
						}
					}
					break;
				
				case R.id.btn_test:
					
					int[] Len = {320, 384, 512, 1024};
					File[] cmp = new File[4];
					File txt = new File(Cfg.getInstance().getRootDir() + Cfg.getInstance().getTmpPath() 
							+ File.separator + Cfg.getInstance().getUserName() + ".txt");
					for (int i=0; i<Len.length; i++) {
						String filename = Cfg.getInstance().getRootDir() + Cfg.getInstance().getTmpPath() + 
								File.separator + Cfg.getInstance().getUserName() + Cfg.getInstance().getFeaSuf() +Len[i];
						System.out.println(filename);
						cmp[i] = new File(filename);
						if (cmp[i].exists()) {
							cmp[i].delete();
						}
					}
					for (int i=0; i<Len.length; i++) {
						try {
							GetMfcc getMfcc = new GetMfcc();
							BufferedReader buffer = new BufferedReader(new FileReader(txt));
							
							int tLen = Len[i];
							String line = null;
							double[] idata = new double[1024];
							int j = 0;
							while ( (line = buffer.readLine()) != null ) {
								double tmp = (double) Integer.parseInt(line);
								idata[j] = tmp;
								if (j == tLen-1) {
									j = 0;
									getMfcc.writemfcc(cmp[i], idata, tLen);
								}
								else {
									j++;
								}
							}
							if (j > 0) {
								getMfcc.writemfcc(cmp[i], idata, j);
							}
							
							buffer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						Log.d("mfcc.cmp", ""+Len[i]);
					}
					
					TrainActivity.this.tvInfo.setText("GetMfcc");
					
					break;
			}
			
		}

	}
	
	public class MfccTask extends AsyncTask<Void, String, String> {

		@Override
		protected String doInBackground(Void... progress) {
			
			String rootDir = Cfg.getInstance().getRootDir();
			String username = Cfg.getInstance().getUserName();
			
			this.publishProgress("正在录音...");
			
			short[] audioData = new short[bufferSizeInBytes];
			double[] inSamples = new double[bufferSizeInBytes+1];
			int readsize = 0;
			
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
			
			File fileMfcc = new File(Cfg.getInstance().getRootDir() + Cfg.getInstance().getTmpPath() + 
					File.separator + Cfg.getInstance().getUserName() + Cfg.getInstance().getFeaSuf());
			if (fileMfcc.exists()) {
				fileMfcc.delete();
			}
			
			while (isRecording == true) {
				readsize = audioRecord.read(audioData, 0, bufferSizeInBytes);
				Log.d("readsize", ""+readsize);
				if (AudioRecord.ERROR_INVALID_OPERATION != readsize
						&& AudioRecord.ERROR_BAD_VALUE != readsize) {

					for (int i=0; i<readsize; i++){
						inSamples[i+1] = audioData[i];
						
						writer.println(""+audioData[i]);
					}

//					getMfcc.writemfcc(fileMfcc, inSamples, readsize);
					Mfcc.getInstance().write(fileMfcc, inSamples, readsize);
				}
			}
			
			writer.close();
			
			if (isCancelled()) {
				Log.d("MfccTask", "isCanceled()1");
				return null;
			}
			
			this.publishProgress("正在训练模型...");
			Recognition.TrainGmm(rootDir + Cfg.getInstance().getWorldMdlPath() + File.separator,
					rootDir + Cfg.getInstance().getTmpPath() + File.separator,
					rootDir + Cfg.getInstance().getTmpPath() + File.separator,
					Cfg.getInstance().getUserName());
			String tmpPath = rootDir + Cfg.getInstance().getTmpPath() + File.separator;
			FileAccess.Move(tmpPath + username + Cfg.getInstance().getFeaSuf(), rootDir + Cfg.getInstance().getUsersPath() + File.separator + username + File.separator);
			FileAccess.Move(tmpPath + username + Cfg.getInstance().getMdlSuf(), rootDir + Cfg.getInstance().getUsersPath() + File.separator + username + File.separator);
			
			if (isCancelled()) {
				Log.d("MfccTask", "isCanceled()2");
				return null;
			}
						
			return "训练完成";
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			if (values[0].equals("正在录音..."))
				TrainActivity.this.btnTrain.setEnabled(true);
			if (values[0].equals("正在训练模型...")) {
				TrainActivity.this.btnTrain.setEnabled(false);
//				TrainActivity.this.btnTest.setEnabled(true);
			}
			if (isCancelled()) {
				TrainActivity.this.btnTrain.setEnabled(true);
				TrainActivity.this.tvInfo.setText("被取消");
				return ;
			}
			TrainActivity.this.tvInfo.setText(values[0]);
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(String result) {
			TrainActivity.this.btnTrain.setEnabled(true);
			TrainActivity.this.tvInfo.setText(result);
			super.onPostExecute(result);
			Toast.makeText(getApplicationContext(), "训练成功", Toast.LENGTH_SHORT).show();
			TrainActivity.this.finish();
		}
		
	}

}
