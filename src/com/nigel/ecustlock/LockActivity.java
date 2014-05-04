package com.nigel.ecustlock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.nigel.ecustlock.ResultDialog.ResultDialogListener;
import com.support.Cfg;
import com.support.GetMfcc;
import com.support.Recognition;
import com.support.SqlOpenHelper;
import com.support.Test;
import com.support.mfcc.Mfcc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LockActivity extends FragmentActivity implements ScoreDialog.ScoreDialogListener {

	TextView timeView = null;
	TextView dateView = null;
	TextView progressView = null;

	EditText mPassView = null;
	ProgressBar pbCircle = null;
	
	Test test = null;
	
	private int mShortAnimationDuration;

	DecimalFormat decimalFormat;
	static String[] weekDaysName = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五",
			"星期六" };
	
	static String[] statusString = { "正在录音...", "正在识别..."};

	HashMap<String, Float> mapScore = new HashMap<String, Float>();
	float score = -1;
	int audioSource = MediaRecorder.AudioSource.MIC;
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int bufferSizeInBytes = 0;
	boolean isRecording = false;
	AudioRecord audioRecord = null;
	AuthenTask ATask = null;
	
	SQLiteDatabase database = null;

	String ac_tag = "LockActivity life";
	String async_tag = "AuthenAsyncTask life";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(ac_tag, "onCreate");
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
		
		SqlOpenHelper helper = new SqlOpenHelper(getApplicationContext());
		database = helper.getReadableDatabase();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		Log.v(ac_tag, "onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.v(ac_tag, "onResume");
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

		this.pbCircle.setVisibility(View.INVISIBLE);
		this.progressView.setAlpha(1f);
		this.progressView.setVisibility(View.VISIBLE);
		this.progressView.setText("正在初始化...");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = pm.isScreenOn();
		if (isScreenOn) {
			if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
				isRecording = false;
			} else {
				if (isRecording == false) {
					audioRecord.startRecording();
					isRecording = true;
				}
				ATask = new AuthenTask();
				ATask.execute();
			}
		}

	}

	@Override
	protected void onPause() {
		Log.v(ac_tag, "onPause");
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.v(ac_tag, "onStop");
		if (ATask != null) {
			ATask.cancel(true);
		}
		isRecording = false;
		if (audioRecord != null) {
			if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
				audioRecord.stop();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		database.close();
		if (audioRecord != null) {
			if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
				audioRecord.stop();
			}
			audioRecord.release();
			audioRecord = null;
		}
		
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
//			crossfade();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class AuthenTask extends AsyncTask<Void, Integer, String> {

		@Override
		protected void onPreExecute() {
			// 第一个执行方法
			Log.v(async_tag, "onPreExecute");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... progress) {
			String result = "识别结束";
			Log.v(async_tag, "doInBackground");
			this.publishProgress(0);

			String rootDir = Cfg.getInstance().getRootDir();
			short[] audioData = new short[bufferSizeInBytes+1];
			int readsize = 0;

			File file = new File(rootDir + Cfg.getInstance().getTmpPath()
					+ File.separator + "tmp" + Cfg.getInstance().getFeaSuf());
			if (file.exists()) {
				file.delete();
			}
			Log.d("file.name", file.getName());
			double[] inSamples = new double[1024];
			while (isRecording == true) {
				readsize = audioRecord.read(audioData, 0, bufferSizeInBytes);

				if (AudioRecord.ERROR_INVALID_OPERATION != readsize
						&& AudioRecord.ERROR_BAD_VALUE != readsize) {

					for (int i=0; i<readsize; i++){
						inSamples[i+1] = audioData[i];
					}

//					getMfcc.writemfcc(file, inSamples, readsize);
					Mfcc.getInstance().write(file, inSamples, readsize);
				}
			}

			if (isCancelled()) {
				return "被取消";
			}
			
			this.publishProgress(1);

			mapScore.clear();
			String[] columns = {SqlOpenHelper.USER_NAME};
			List<String> namelist = new ArrayList<String>();
			Cursor cursor = database.query(SqlOpenHelper.TABLE_USERINFO, columns, null, null, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				String name = cursor.getString(0);
				namelist.add(name);
				cursor.moveToNext();
			}
			// recognize
			String tmpPath = rootDir + Cfg.getInstance().getTmpPath() + File.separator;
			Log.d("Test", "start: tmpPath=" + tmpPath);
			for (String username : namelist) {
				String userFileDir = rootDir + Cfg.getInstance().getUsersPath() + File.separator
						+ username + File.separator;
				File featrue = new File(userFileDir + username + Cfg.getInstance().getFeaSuf());
				File model = new File(userFileDir + username + Cfg.getInstance().getMdlSuf());
				if ( featrue.exists() && model.exists() ) {
					float tmpscore = (float) Recognition.Test(rootDir + Cfg.getInstance().getWorldMdlPath() + File.separator,
							rootDir + Cfg.getInstance().getTmpPath() + File.separator,
							rootDir + Cfg.getInstance().getUsersPath() + File.separator + username + File.separator,
							"tmp",
							username);
					
					mapScore.put(username, tmpscore);
				}
				
			}
			Log.d("recognize result", ""+score);
			return result;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			// 这个函数在doInBackground调用publishProgress时触发，虽然调用时只有一个参数
			// 但是这里取到的是一个数组,所以要用progesss[0]来取值
			// 第n个参数就用progress[n]来取值
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
			
			Log.v(async_tag, "onProgressUpdate");
			super.onProgressUpdate(progress);
		}

		@Override
		protected void onPostExecute(String result) {
			// doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
			// 这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
			pbCircle.animate().alpha(0f).setDuration(0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							pbCircle.setVisibility(View.INVISIBLE);
						}
					});
			LockActivity.this.progressView.setText(result);
			LockActivity.this.showResultDialog();
			
			Log.v(async_tag, "onPostExecute");
			super.onPostExecute(result);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			// TODO release audio record
			Log.v(async_tag, "onCancelled");
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

	public void showResultDialog() {
		DialogFragment dialog = new ScoreDialog();
		dialog.show(getSupportFragmentManager(), "score");
	}
//	
//	@Override
//	public void onDialogPositiveClick(DialogFragment dialog) {
//		Toast.makeText(getApplicationContext(), "识别正确", Toast.LENGTH_SHORT).show();
//		LockActivity.this.finish();
//	}
//
//	@Override
//	public void onDialogNegativeClick(DialogFragment dialog) {
//		Toast.makeText(getApplicationContext(), "识别错误", Toast.LENGTH_SHORT).show();
//		LockActivity.this.finish();
//	}
//
//	@Override
//	public void onSetScore() {
//		ResultDialog dialog = (ResultDialog) getSupportFragmentManager().findFragmentByTag("result");
//		if (dialog != null) {
//			SharedPreferences sharedPref = getSharedPreferences(getString(R.string.s_settingsPreferences), Context.MODE_PRIVATE);
//			String key = getString(R.string.s_settingsThreshold);
//			float threshold = sharedPref.getFloat(key, -50);
//			dialog.UpdateScoreView(score, threshold, "admin");
//		}
//	}

	@Override
	public void onSetThreshold(DialogFragment dialog) {
		ScoreDialog dlg = (ScoreDialog) dialog;
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.s_settingsPreferences), Context.MODE_PRIVATE);
		String key = getString(R.string.s_settingsThreshold);
		float threshold = sharedPref.getFloat(key, -50);
		dlg.setThreshold(threshold);
	}

	@Override
	public void onSetScoreList(DialogFragment dialog) {
		ScoreDialog dlg = (ScoreDialog) dialog;
		dlg.setScoreList(mapScore);
	}

	@Override
	public void onFinish() {
		LockActivity.this.finish();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		LockActivity.this.finish();
	}

}
