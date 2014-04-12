package com.nigel.ecustlock;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import com.support.Config;
import com.support.GetMfcc;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_train);

		this.btnTrain = (Button) super.findViewById(R.id.btn_train);
		this.tvInfo = (TextView) super.findViewById(R.id.tv_info);
		
		this.btnTrain.setOnClickListener(new TrainOnClickListenserImpl());
		
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		audioRecord = new AudioRecord(audioSource, sampleRateInHz,
				channelConfig, audioFormat, bufferSizeInBytes);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class TrainOnClickListenserImpl implements OnClickListener {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.btn_train) {
				if (isRecording == true) {
					btnTrain.setText("��ʼ¼��");

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
					} 
					else {
						btnTrain.setText("����¼��");
						if (audioRecord == null) {
							audioRecord = new AudioRecord(audioSource, sampleRateInHz,
									channelConfig, audioFormat, bufferSizeInBytes);
						}
						audioRecord.startRecording();
						isRecording = true;
						Log.v("isRecording", ""+isRecording);
						MfccTask mfccTask = new MfccTask();
						mfccTask.execute();
					}
				}
			}
		}

	}
	
	public class MfccTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... progress) {
			short[] audioData = new short[bufferSizeInBytes/2];
			int readsize = 0;
			FileOutputStream fos = null;
			FileOutputStream fos_bak = null;
			DataOutputStream dos_bak = null;
			PrintWriter writer = null;
			GetMfcc getMfcc = new GetMfcc();
			try {
				File file = new File(Config.getRootDir() + Config.getFeaturePath()
						+ File.separator + Config.getUserName()
						+ Config.getFeaSuf());
				if (file.exists()) {
					file.delete();
				}
				fos = new FileOutputStream(file);
				
				File file_bak = new File(Config.getRootDir()+Config.getFeaturePath()
						+ File.separator + Config.getUserName() + ".data");
				if (file_bak.exists())
					file_bak.delete();
				
				File file_txt = new File(Config.getRootDir()+Config.getFeaturePath()
						+ File.separator + Config.getUserName() + ".txt");
				if (file_txt.exists())
					file_txt.delete();
				fos_bak = new FileOutputStream(file_bak);
				dos_bak = new DataOutputStream(fos_bak);
				writer = new PrintWriter(file_txt);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			double[] inSamples = new double[1024];
			int v = -100;
			while (isRecording == true) {
				readsize = audioRecord.read(audioData, 0, bufferSizeInBytes/2);
				if (AudioRecord.ERROR_INVALID_OPERATION != readsize
						&& AudioRecord.ERROR_BAD_VALUE != readsize) {

					for (int i=0; i<readsize; i++){
						inSamples[i] = audioData[i];
					}

					try {
						byte xx[] = new byte[4];
						double[][] ans = getMfcc.mfcc(inSamples, readsize);
						if (ans != null) {
							int height = getMfcc.getFramenum();
							int width = getMfcc.getDimension();
							float tmp = 0;
							for (int i=0; i<height; i++){
								for (int j=0; j<width; j++) {
									
									tmp = (float) ans[i+1][j+1];
									dos_bak.writeFloat(tmp);
									writer.println(tmp);
									int tmpInt = Float.floatToIntBits(tmp);
									
									for (int k=0; k<4; k++) {
										xx[k] = (byte) (tmpInt & 255);
										tmpInt >>= 8;
										fos.write(xx[k]);
									}
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			try {
				fos.close();
				dos_bak.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return "ѵ�����";
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			TrainActivity.this.tvInfo.setText(result);
			
			super.onPostExecute(result);
		}
		
	}

}
