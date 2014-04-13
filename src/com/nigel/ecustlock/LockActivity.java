package com.nigel.ecustlock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

import com.support.Config;
import com.support.GetMfcc;
import com.support.Recognition;
import com.support.Test;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
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

public class LockActivity extends Activity {

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

	double score = -1;
	int audioSource = MediaRecorder.AudioSource.MIC;
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int bufferSizeInBytes = 0;
	boolean isRecording = false;
	AudioRecord audioRecord = null;
	AuthenTask ATask = null;

	String ac_tag = "LockActivity life";
	String async_tag = "AuthenAsyncTask life";
	
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

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		Log.i(ac_tag, "onStart");
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
				+ decimalFormat.format(c.get(Calendar.MONTH) + 1) + "月"
				+ decimalFormat.format(c.get(Calendar.DAY_OF_MONTH)) + "日  "
				+ weekDaysName[weekIndex]);

		this.mPassView.setVisibility(View.GONE);

		this.pbCircle.setVisibility(View.INVISIBLE);
		this.progressView.setAlpha(1f);
		this.progressView.setVisibility(View.VISIBLE);
		this.progressView.setText("进度0");
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
		Log.i(ac_tag, "onPause");
		super.onPause();
		
//		isRecording = false;
//		if (audioRecord != null) {
//			audioRecord.stop();
//			audioRecord.release();
//			audioRecord = null;
//		}
	}
	
	@Override
	protected void onStop() {
		Log.i(ac_tag, "onStop");
		if (ATask != null) {
			ATask.cancel(true);
		}
		isRecording = false;
		if (audioRecord != null) {
			if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
				audioRecord.stop();
			}
		}
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		
		if (audioRecord != null) {
			if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
				audioRecord.stop();
			}
			audioRecord.release();
			audioRecord = null;
		}
		
		super.onDestroy();
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
			// 第一个执行方法
			Log.v(async_tag, "onPreExecute");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... progress) {
			String result = "得分为：";
			Log.v(async_tag, "doInBackground");
			this.publishProgress(0);

			short[] audioData = new short[bufferSizeInBytes/2];
			int readsize = 0;
			FileOutputStream fos = null;
			GetMfcc getMfcc = new GetMfcc();
			try {
				File file = new File(Config.getRootDir() + Config.getTestFeaturePath()
						+ File.separator + Config.getUserName()
						+ Config.getFeaSuf());
				if (file.exists()) {
					file.delete();
				}
				fos = new FileOutputStream(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
						
			// ----------------------
			SharedPreferences sharedPref = getSharedPreferences(getString(R.string.s_settingsPreferences), Context.MODE_PRIVATE);
			String key = Config.getLastTrainSetting();
			String testRoot = sharedPref.getString(key, "");
			Log.v("testRoot", testRoot);
			test = new Test();
			test.startBackupAuth(testRoot);
			// ------------------------------------
			
			double[] inSamples = new double[1024];
			while (isRecording == true) {
				readsize = audioRecord.read(audioData, 0, bufferSizeInBytes/2);

				if (AudioRecord.ERROR_INVALID_OPERATION != readsize
						&& AudioRecord.ERROR_BAD_VALUE != readsize) {

					// TODO vad
					// isRecording = false;
					test.backupShortData(
							test.getAuthDirPath() + File.separator + Config.getUserName()+".short",
							audioData, 
							readsize);
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
							
							// next is used for backup mfcc
							float[] backupData = new float[width];
							for (int i=0; i<height; i++) {
								for (int j=0; j<width; j++) {
									backupData[j] = (float) ans[i+1][j+1];
								}
								test.backupFloatData(
										test.getAuthDirPath() + File.separator + Config.getUserName()+".mfcc",
										backupData,
										width);
							}
								
							for (int i=0; i<height; i++){
								for (int j=0; j<width; j++) {
									
									tmp = (float) ans[i+1][j+1];
									int tmpInt = Float.floatToIntBits(tmp);
									
									for (int k=0; k<4; k++) {
										xx[k] = (byte) (tmpInt & 255);
										tmpInt >>= 8;
									}
									fos.write(xx);
								}
							}
							
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			if (isCancelled()) {
				return "被取消";
			}
			
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.publishProgress(1);

			// recognize
			Log.v("recognize result", "start");
//			score = Recognition.Test(Config.getRootDir(), Config.getUserName());
			test.backupLog(score);
			Log.v("recognize result", "end");
			Log.v("recognize result", ""+score);
			return result + score;
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
	
	private static Bitmap takeScreenShot(Activity activity) {
		
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		

		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
				- statusBarHeight);
		view.destroyDrawingCache();
		return b;
	}

	public Bitmap fastblur(Bitmap sentBitmap, int radius) {
		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);

		return (bitmap);
	}

}
