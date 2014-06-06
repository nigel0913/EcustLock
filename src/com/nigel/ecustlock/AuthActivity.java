package com.nigel.ecustlock;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.nigel.custom.MicButton;
import com.support.Cfg;
import com.support.Recognition;
import com.support.SqlOpenHelper;
import com.support.mfcc.Mfcc;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AuthActivity extends Activity 
		implements OnClickListener, OnTouchListener {

	MicButton btnAuth;
	TextView tvInfo;
	TextView tvWelcome;
	ImageView ivAvatar;
	ProgressBar pbLoading;
	TextView timeView = null;
	TextView dateView = null;
	EditText etAuthPwd;
	DecimalFormat decimalFormat;
	static String[] weekDaysName = { "������", "����һ", "���ڶ�", "������", "������", "������",
				"������" };
	
	Animator mCurrentAnimator;
	int mShortAnimationDuration;
	
	int audioSource = MediaRecorder.AudioSource.MIC;
	int sampleRateInHz = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int bufferSizeInBytes = 0;
	boolean isRecording = false;
	AudioRecord audioRecord = null;
	
	SQLiteDatabase database = null;
	AuthTask authTask = null;
	
	HashMap<String, Float> mapScore = new HashMap<String, Float>();
	
	boolean pressed = false;
	
	String rootDir = null;
	String tmpPath = null;
	
	float threshold;
	float highest;
	
	Button btnSlide = null;
	boolean isPasswordLayout = false;
	
	View layoutPassword;
	View layoutRecognition;
	
	final String ac_tag = "AuthActivity";
	
//	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
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
//		this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);
		setContentView(R.layout.activity_auth);
		
		btnAuth = (MicButton) super.findViewById(R.id.micButton1);
		tvInfo = (TextView) super.findViewById(R.id.tv_test_info);
		tvWelcome = (TextView) super.findViewById(R.id.tv_welcome);
		ivAvatar = (ImageView) super.findViewById(R.id.auth_avater_big);
		pbLoading = (ProgressBar) super.findViewById(R.id.loading_spinner);
		timeView = (TextView) super.findViewById(R.id.tv_time);
		dateView = (TextView) super.findViewById(R.id.tv_date);
		btnSlide = (Button) super.findViewById(R.id.btn_slide);
		layoutPassword = super.findViewById(R.id.layout_password);
		layoutRecognition = super.findViewById(R.id.layout_recognition);
		etAuthPwd = (EditText) super.findViewById(R.id.et_auth_password);
		
		decimalFormat = new DecimalFormat("00");
		
		btnAuth.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		btnAuth.setOnTouchListener(this);
		btnSlide.setOnClickListener(this);
		
		tvWelcome.setText("�밴ס¼������¼��");
		
		ivAvatar.setVisibility(View.INVISIBLE);
		pbLoading.setVisibility(View.INVISIBLE);
		
		// Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        
		SqlOpenHelper helper = new SqlOpenHelper(getApplicationContext());
		database = helper.getReadableDatabase();
		
		String[] columns = {SqlOpenHelper.USER_PWD};
		String[] params = {"admin"};
		Cursor result = database.query(SqlOpenHelper.TABLE_USERINFO, columns, SqlOpenHelper.USER_NAME + "=?", params, null, null, null);
		result.moveToFirst();
		String pwd = "0000";
		while (!result.isAfterLast()) {
			pwd = result.getString(0);
			result.moveToNext();
		}
		result.close();
		etAuthPwd.addTextChangedListener(new PasswordCheck(pwd));
		
		rootDir = Cfg.getInstance().getRootDir();
		tmpPath = rootDir + Cfg.getInstance().getTmpPath() + File.separator;
		
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.s_settingsPreferences), Context.MODE_PRIVATE);
		String key = getString(R.string.s_settingsThreshold);
		threshold = sharedPref.getFloat(key, -50);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_test_info:
				break;
			case R.id.btn_slide:
				if (isPasswordLayout) {
					btnSlide.setText(getString(R.string.numerical_password));
					isPasswordLayout = false;
					layoutPassword.setVisibility(View.INVISIBLE);
					layoutRecognition.setVisibility(View.VISIBLE);
				}
				else {
					btnSlide.setText("ת������ʶ��ģʽ");
					isPasswordLayout = true;
					layoutRecognition.setVisibility(View.INVISIBLE);
					layoutPassword.setVisibility(View.VISIBLE);
				}
				break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.i(ac_tag, "");
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// return true ��ʾ���η��ؼ�
			return false;
		case KeyEvent.KEYCODE_MENU:
			return false;
//		case KeyEvent.KEYCODE_HOME:
//			Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
//			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		switch (v.getId()) {
			case R.id.micButton1:
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
//					tvInfo.setText("����");
					pressed = true;
					if (authTask != null) {
						if (authTask.getStatus() != AsyncTask.Status.FINISHED) {
							return false;
						}
					}
					
					pbLoading.setVisibility(View.INVISIBLE);
					
					if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
						if (isRecording == false) {
							audioRecord.startRecording();
							isRecording = true;
							authTask = new AuthTask();
							authTask.execute();
						}
						
					}
				}
				else if (event.getAction() == MotionEvent.ACTION_UP) {
//					tvInfo.setText("�ջ�");
					pressed = false;
					if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
						if (isRecording == true) {
							audioRecord.stop();
							isRecording = false;
						}
					}
				}
				return false;
		}
		
		return false;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		Log.d(ac_tag, "onStart");
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		audioRecord = new AudioRecord(audioSource, sampleRateInHz,
				channelConfig, audioFormat, bufferSizeInBytes);
		
		if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
			tvWelcome.setText("¼���豸��ʼ��ʧ��");
		}
		else {
			tvWelcome.setText("�밴ס¼������¼��");
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		Log.d(ac_tag, "onStop");
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
	
	@Override
	protected void onResume() {
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
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		database.close();
	}
	
	public class PasswordCheck implements TextWatcher {

		String password;
		
		public PasswordCheck(String pwd){
			password = pwd;
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			String inputpwd = s.toString();
			if (inputpwd.equals(password)) {
				Toast.makeText(getApplicationContext(), "��ӭ", Toast.LENGTH_SHORT).show();
				AuthActivity.this.finish();
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
	}
	
	public class AuthTask extends AsyncTask<Void, String, String> {

		@Override
		protected String doInBackground(Void... progress) {
			String result = "ʶ�����";
			highest = -1000;
			this.publishProgress("����¼��...");

			short[] audioData = new short[bufferSizeInBytes+1];
			int readsize = 0;

			File file = new File(rootDir + Cfg.getInstance().getTmpPath()
					+ File.separator + "tmp" + Cfg.getInstance().getFeaSuf());
			if (file.exists()) {
				file.delete();
			}
			Log.d("file.name", file.getName());
			double[] inSamples = new double[bufferSizeInBytes + 1];
			while (isRecording == true) {
				readsize = audioRecord.read(audioData, 0, bufferSizeInBytes);

				if (AudioRecord.ERROR_INVALID_OPERATION != readsize
						&& AudioRecord.ERROR_BAD_VALUE != readsize) {

					for (int i=0; i<readsize; i++){
						inSamples[i+1] = audioData[i];
					}

					Mfcc.getInstance().write(file, inSamples, readsize);
				}
			}

			if (isCancelled()) {
				return "��ȡ��";
			}
			
			this.publishProgress("����ʶ��...");

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
					if (tmpscore > highest) {
						result = username;
						highest = tmpscore;
					}
				}
				
			}
			
			if (isCancelled()) {
				return "��ȡ��";
			}
			
			Log.d("highest", "highest="+highest+",threshold="+threshold);
			
			if (threshold > highest)
				result = "���ܾ�";
			
			return result;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			tvWelcome.setText(values[0]);
			if ( values[0].equals("����ʶ��...") ) {
				pbLoading.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			pbLoading.setVisibility(View.INVISIBLE);
			if (result.equals("���ܾ�")) {
				tvWelcome.setText("�ܾ�����"+" <"+highest+","+threshold+">");
			}
			else {
				tvWelcome.setText("��ӭ" + result);
				Toast.makeText(getApplicationContext(), "��ӭ"+result+" <"+highest+","+threshold+">", Toast.LENGTH_SHORT).show();
				AuthActivity.this.finish();
			}
		}
	}
}
