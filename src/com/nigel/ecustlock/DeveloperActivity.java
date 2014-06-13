package com.nigel.ecustlock;

// 正在开发中

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.support.Cfg;
import com.support.mfcc.Mfcc;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DeveloperActivity extends Activity implements OnClickListener {

	EditText etUsername;
	Button btnRecord30;
	Button btnRecord10;
	Button btnStop;
	ProgressBar pbLeftTime;
	TextView tvLeftTime;
	Button btnTrainNew;
	Button btnTrainAll;
	Button btnTrainOne;
	TextView tvSelectedUsername;
	Button btnCalAll;
	TextView tvThreshold;
	
	int audioSource = MediaRecorder.AudioSource.MIC;
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int bufferSizeInBytes = 0;
	boolean isRecording = false;
	AudioRecord audioRecord = null;
	String rootDir = null;
	
	Handler mHandler = new Handler();
	int mProgressStatus = 0;
	
	RecordTask task;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_developer);
		
		etUsername = (EditText) super.findViewById(R.id.dev_username);
		btnRecord30 = (Button) super.findViewById(R.id.btn_record_30);
		btnRecord10 = (Button) super.findViewById(R.id.btn_record_10);
		btnStop = (Button) super.findViewById(R.id.dev_btn_stop);
		pbLeftTime = (ProgressBar) super.findViewById(R.id.dev_time_progressbar);
		tvLeftTime = (TextView) super.findViewById(R.id.dev_left_time);
		
		btnRecord10.setOnClickListener(this);
		btnRecord30.setOnClickListener(this);
		btnStop.setOnClickListener(this);
		
		btnStop.setEnabled(false);
		tvLeftTime.setVisibility(View.INVISIBLE);
		rootDir = Cfg.getInstance().getRootDir();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		audioRecord = new AudioRecord(audioSource, sampleRateInHz,
				channelConfig, audioFormat, bufferSizeInBytes);
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if (audioRecord != null &&
				audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
			if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
				audioRecord.stop();
			}
			Log.d("audioRecord", "release");
			audioRecord.release();
			audioRecord = null;
		}
	}
	
	public class RecordTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... progress) {
			
			String name = progress[0];
			short[] audioData = new short[bufferSizeInBytes+1];
			int readsize = 0;
//			double[] inSamples = new double[bufferSizeInBytes + 1];
			PrintWriter writer = null;
			try {
				File file = new File(rootDir + "developer"
						+ File.separator + name + ".raw");
				if (file.exists()) {
					file.delete();
				}
				writer = new PrintWriter(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			while (isRecording == true) {
				readsize = audioRecord.read(audioData, 0, bufferSizeInBytes);

				if (AudioRecord.ERROR_INVALID_OPERATION != readsize
						&& AudioRecord.ERROR_BAD_VALUE != readsize) {

					for (int i=0; i<readsize; i++){
//						inSamples[i+1] = audioData[i];
						
						writer.println(""+audioData[i]);
					}

				}
			}
			
			writer.close();
			
			return null;
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		String name = "";
		switch (id) {
			case R.id.btn_record_30:
				name = etUsername.getText().toString();
				if (name.equals("")) {
					Toast.makeText(getApplicationContext(), "文件名空", Toast.LENGTH_SHORT).show();
					return ;
				}
				audioRecord.startRecording();
				isRecording = true;
				task = new RecordTask();
				task.execute(name);
				mProgressStatus = 0;
				tvLeftTime.setText("30秒");
				btnRecord10.setEnabled(false);
				btnRecord30.setEnabled(false);
				btnStop.setEnabled(true);
				tvLeftTime.setVisibility(View.VISIBLE);
				new Thread(new Runnable() {
					public void run() {
						while (mProgressStatus < 30) {
							mProgressStatus++;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
	
							// Update the progress bar
							mHandler.post(new Runnable() {
								public void run() {
									pbLeftTime.setProgress(mProgressStatus * 10 / 3);
									tvLeftTime.setText("" + (30 - mProgressStatus)
											+ "秒");
									if (mProgressStatus >= 30) {
										if (audioRecord != null) {
											isRecording = false;
											audioRecord.stop();
										}
										btnRecord10.setEnabled(true);
										btnRecord30.setEnabled(true);
										btnStop.setEnabled(false);
									}
								}
							});
						}
					}
				}).start();
				break;
			case R.id.btn_record_10:
				name = etUsername.getText().toString();
				if (name.equals("")) {
					Toast.makeText(getApplicationContext(), "文件名空", Toast.LENGTH_SHORT).show();
					return ;
				}
				audioRecord.startRecording();
				isRecording = true;
				task = new RecordTask();
				task.execute(name);
				mProgressStatus = 0;
				tvLeftTime.setText("10秒");
				btnRecord10.setEnabled(false);
				btnRecord30.setEnabled(false);
				btnStop.setEnabled(true);
				tvLeftTime.setVisibility(View.VISIBLE);
				new Thread(new Runnable() {
					public void run() {
						while (mProgressStatus < 10) {
							mProgressStatus++;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							// Update the progress bar
							mHandler.post(new Runnable() {
								public void run() {
									pbLeftTime.setProgress(mProgressStatus * 10);
									tvLeftTime.setText("" + (10 - mProgressStatus)
											+ "秒");
									if (mProgressStatus >= 10) {
										if (audioRecord != null) {
											isRecording = false;
											audioRecord.stop();
										}
										btnRecord10.setEnabled(true);
										btnRecord30.setEnabled(true);
										btnStop.setEnabled(false);
									}
								}
							});
						}
					}
				}).start();
				break;
			case R.id.dev_btn_stop:
				isRecording = false;
				audioRecord.stop();
				task.cancel(true);
				mProgressStatus = 60;
				btnRecord10.setEnabled(true);
				btnRecord30.setEnabled(true);
				btnStop.setEnabled(false);
				pbLeftTime.setProgress(0);
				tvLeftTime.setVisibility(View.INVISIBLE);
				break;
			default:
				break;
		}
	}
	
}
