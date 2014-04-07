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
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.TextView;

public class LockActivity extends Activity {

	TextView timeView = null;
	TextView dateView = null;
	TextView progressView = null;

	EditText mPassView = null;
	private int mShortAnimationDuration;

	DecimalFormat decimalFormat;
	static String[] weekDaysName = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五",
			"星期六" };

	double score = -1;
	int audioSource = MediaRecorder.AudioSource.MIC;
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	int audioFormat = AudioFormat.ENCODING_PCM_8BIT;
	int bufferSizeInBytes = 0;
	boolean isRecording = false;
	AudioRecord audioRecord = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

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
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Calendar c = Calendar.getInstance();
		int weekIndex = c.get(Calendar.DAY_OF_WEEK) - 1;

		this.timeView.setText(""
				+ decimalFormat.format(c.get(Calendar.HOUR_OF_DAY)) + ":"
				+ decimalFormat.format(c.get(Calendar.MINUTE)));
		this.dateView.setText(""
				+ decimalFormat.format(c.get(Calendar.MONTH) + 1) + "月"
				+ decimalFormat.format(c.get(Calendar.DAY_OF_MONTH)) + "日  "
				+ weekDaysName[weekIndex]);

		this.mPassView.setVisibility(View.GONE);

		this.progressView.setAlpha(1f);
		this.progressView.setVisibility(View.VISIBLE);
		this.progressView.setText("进度0");

		AuthenTask ATask = new AuthenTask();
		ATask.execute();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public boolean onkeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			isRecording = false;
			break;
		case KeyEvent.KEYCODE_BACK:
			return super.onKeyDown(keyCode, event);
		case KeyEvent.KEYCODE_MENU:
			return super.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}

	public class AuthenTask extends AsyncTask<Void, Integer, String> {

		@Override
		protected void onPreExecute() {
			// 第一个执行方法
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... progress) {
			String result = "得分为：";

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

			// recognize
			score = Recognition.recognition(Config.getRootDir(),
					Config.getUserName());

			return result + score;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			// 这个函数在doInBackground调用publishProgress时触发，虽然调用时只有一个参数
			// 但是这里取到的是一个数组,所以要用progesss[0]来取值
			// 第n个参数就用progress[n]来取值
			// LockActivity.this.progressView.setText("时间："+progress[0]+"s");
			super.onProgressUpdate(progress);
		}

		@Override
		protected void onPostExecute(String result) {
			// doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
			// 这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
			LockActivity.this.progressView.setText(result);
			crossfade();
			super.onPostExecute(result);
		}

	}

	private void crossfade() {

		// Set the content view to 0% opacity but visible, so that it is visible
		// (but fully transparent) during the animation.
		this.mPassView.setAlpha(0f);
		this.mPassView.setVisibility(View.VISIBLE);

		// Animate the content view to 100% opacity, and clear any animation
		// listener set on the view.
		this.mPassView.animate().alpha(1f).setDuration(mShortAnimationDuration)
				.setListener(null);

		// Animate the loading view to 0% opacity. After the animation ends,
		// set its visibility to GONE as an optimization step (it won't
		// participate in layout passes, etc.)
		this.progressView.animate().alpha(0f)
				.setDuration(mShortAnimationDuration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						progressView.setVisibility(View.GONE);
					}
				});
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
