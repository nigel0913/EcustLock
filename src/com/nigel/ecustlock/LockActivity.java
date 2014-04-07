package com.nigel.ecustlock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

import com.support.Config;
import com.support.Config.DOTYPE;
import com.support.Recognition;
import com.support.SRecord;
import com.support.SRecord.EResultType;
import com.support.SThread;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LockActivity extends Activity {

	TextView timeView = null;
	TextView dateView = null;
	TextView progressView = null;

	EditText mPassView = null;
	ProgressBar pbCircle = null;
	
	private int mShortAnimationDuration;

	DecimalFormat decimalFormat;
	static String[] weekDaysName = { "������", "����һ", "���ڶ�", "������", "������", "������",
			"������" };
	
	static String[] statusString = { "����¼��...", "������ȡ����...", "����ʶ��..."};

	double score = -1;
	int audioSource = MediaRecorder.AudioSource.MIC;
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int bufferSizeInBytes = 0;
	boolean isRecording = false;
	AudioRecord audioRecord = null;

	String ac_tag = "activity life";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(ac_tag, "onCreate");
		super.onCreate(savedInstanceState);
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		win.setAttributes(winParams);
		setContentView(R.layout.activity_lock);

		this.timeView = (TextView) super.findViewById(R.id.tTime);
		this.dateView = (TextView) super.findViewById(R.id.tDate);
		this.progressView = (TextView) super.findViewById(R.id.tProgress);
		this.mPassView = (EditText) findViewById(R.id.editPass);
		this.pbCircle = (ProgressBar) super.findViewById(R.id.pbCircle);
		this.mPassView.addTextChangedListener(new CheckPassword());

		decimalFormat = new DecimalFormat("00");

		// Initially hide the content view.
		this.mPassView.setVisibility(View.GONE);
		
		// Retrieve and cache the system's default "short" animation time.
		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);

		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		audioRecord = new AudioRecord(audioSource, sampleRateInHz,
				channelConfig, audioFormat, bufferSizeInBytes);
		if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
			// TODO audio record initialized failed
			isRecording = false;
		} else {
			audioRecord.startRecording();
			isRecording = true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
//		Log.i(ac_tag, "onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.i(ac_tag, "onResume");
		super.onResume();

		Calendar c = Calendar.getInstance();
		int weekIndex = c.get(Calendar.DAY_OF_WEEK) - 1;

		this.timeView.setText(""
				+ decimalFormat.format(c.get(Calendar.HOUR_OF_DAY)) + ":"
				+ decimalFormat.format(c.get(Calendar.MINUTE)));
		this.dateView.setText(""
				+ decimalFormat.format(c.get(Calendar.MONTH) + 1) + "��"
				+ decimalFormat.format(c.get(Calendar.DAY_OF_MONTH)) + "��  "
				+ weekDaysName[weekIndex]);

		this.mPassView.setVisibility(View.GONE);

		this.pbCircle.setVisibility(View.INVISIBLE);
		this.progressView.setAlpha(1f);
		this.progressView.setVisibility(View.VISIBLE);
		this.progressView.setText("����0");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = pm.isScreenOn();
		if (isScreenOn) {
			AuthenTask ATask = new AuthenTask();
			ATask.execute();
		}

	}

	@Override
	protected void onPause() {
//		Log.i(ac_tag, "onPause");
		super.onPause();
	}
	
	@Override
	protected void onStop() {
//		Log.i(ac_tag, "onStop");
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.i(ac_tag, "");
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			isRecording = false;
			return true;
		case KeyEvent.KEYCODE_MENU:
			isRecording = false;
			if (audioRecord != null) {
				audioRecord.stop();
				audioRecord.release();
				audioRecord = null;
			}
			crossfade();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class AuthenTask extends AsyncTask<Void, Integer, String> {

		@Override
		protected void onPreExecute() {
			// ��һ��ִ�з���
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... progress) {
			String result = "�÷�Ϊ��";
			this.publishProgress(0);

			byte[] audioData = new byte[bufferSizeInBytes];
			int readsize = 0;
			FileOutputStream fos = null;
			try {
				File file = new File(Config.getRootDir() + Config.getRawPath()
						+ File.separator + Config.getUserName()
						+ Config.getRawSuf());
				if (file.exists()) {
					file.delete();
				}
				fos = new FileOutputStream(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
			while (isRecording == true) {
				readsize = audioRecord.read(audioData, 0, bufferSizeInBytes);

				if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {

					// TODO vad
					// isRecording = false;

					try {
						fos.write(audioData);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.publishProgress(1);
			// mfcc
			String iFile = Config.getRootDir() + Config.getRawPath()
					+ File.separator + Config.getUserName()
					+ Config.getRawSuf();
			String oFile;
			if (Config.getType() == DOTYPE.TRAIN) {
				oFile = Config.getRootDir() + Config.getFeaturePath()
						+ File.separator + Config.getUserName()
						+ Config.getFeaSuf();
			} else {
				oFile = Config.getRootDir() + Config.getTestFeaturePath()
						+ File.separator + Config.getUserName()
						+ Config.getFeaSuf();
			}

			Log.v("getMfcc", "start\n" + iFile + "\n" + oFile);
			Recognition.getMfcc(iFile, oFile);
			Log.v("getMfcc", "end");

			this.publishProgress(2);
			// recognize
			Log.v("recognize result", "start");
			score = Recognition.recognition(Config.getRootDir(), Config.getUserName());
			Log.v("recognize result", "end");
			Log.v("recognize result", ""+score);
			return result + score;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			// ���������doInBackground����publishProgressʱ��������Ȼ����ʱֻ��һ������
			// ��������ȡ������һ������,����Ҫ��progesss[0]��ȡֵ
			// ��n����������progress[n]��ȡֵ
			if (progress[0] == 1) {
				pbCircle.animate().alpha(1f).setDuration(0)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								pbCircle.setVisibility(View.VISIBLE);
							}
						});
			}
			LockActivity.this.progressView.setText(statusString[ progress[0] ]);
			super.onProgressUpdate(progress);
		}

		@Override
		protected void onPostExecute(String result) {
			// doInBackground����ʱ���������仰˵������doInBackgroundִ����󴥷�
			// �����result��������doInBackgroundִ�к�ķ���ֵ������������"ִ�����"
			pbCircle.animate().alpha(0f).setDuration(0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							pbCircle.setVisibility(View.INVISIBLE);
						}
					});
			LockActivity.this.progressView.setText(result);
			
			super.onPostExecute(result);
		}

	}

	private void crossfade() {
		// Animate the loading view to 0% opacity. After the animation ends,
		// set its visibility to GONE as an optimization step (it won't
		// participate in layout passes, etc.)
		this.progressView.animate().alpha(0f)
				.setDuration(0)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						progressView.setVisibility(View.GONE);
					}
				});
		
		// Set the content view to 0% opacity but visible, so that it is visible
		// (but fully transparent) during the animation.
		this.mPassView.setAlpha(0f);
		this.mPassView.setVisibility(View.VISIBLE);

		// Animate the content view to 100% opacity, and clear any animation
		// listener set on the view.
		this.mPassView.animate().alpha(1f).setDuration(mShortAnimationDuration)
				.setListener(null);

	}

	public class CheckPassword implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			try {
				Log.d("Password", "input: " + s);
				String lockPass = "6666";
				if (s.toString().equals(lockPass)) {
					Log.d("Password", "LockActivity finish()");

					LockActivity.this.finish();
				}
			} catch (NumberFormatException nfe) {
				// none
			}
		}

	}

}
