package com.nigel.ecustlock;

import java.io.File;

import com.nigel.service.LockService;
import com.support.Cfg;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	View switchLayout = null;
	Switch toggleService = null;
	Button btnModifyPwd = null;
	Button btnOpenTrain = null;
	Button btnConfig = null;
//	Button btnMfccTest = null;
	Button btnUserManager = null;
	Button btnTestAuth = null;
	Button btnDeveloper = null;
	View layTrain = null;
	TextView tvTrainDesc = null;
	
	final String life_tag = "MainActivity"; 
	
	boolean trained = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.v(life_tag, "onCreate");
		
		this.switchLayout = (View) super.findViewById(R.id.switch_layout);
		this.toggleService = (Switch) super.findViewById(R.id.service_switch);
		this.btnModifyPwd = (Button) super.findViewById(R.id.btn_modify_passwd);
		this.btnOpenTrain = (Button) super.findViewById(R.id.btn_open_train);
		this.btnConfig = (Button) super.findViewById(R.id.btn_config);
//		this.btnMfccTest = (Button) super.findViewById(R.id.btn_mfcctest);
		this.btnUserManager = (Button) super.findViewById(R.id.btn_user_manager);
		this.btnTestAuth = (Button) super.findViewById(R.id.btn_test_auth);
		this.btnDeveloper = (Button) super.findViewById(R.id.btn_record_30);
		this.tvTrainDesc = (TextView) super.findViewById(R.id.tv_train_desc);
		layTrain = super.findViewById(R.id.layout_train);
		
		this.btnModifyPwd.setOnClickListener(this);
		this.btnOpenTrain.setOnClickListener(this);
		this.btnConfig.setOnClickListener(this);
//		this.btnMfccTest.setOnClickListener(this);
		this.btnTestAuth.setOnClickListener(this);
		this.btnDeveloper.setOnClickListener(this);
		layTrain.setOnClickListener(this);

		Intent intent = getIntent();
		String userName = intent.getStringExtra("user_name");
		if ( userName.equals("admin") ) {
			this.btnUserManager.setVisibility(View.VISIBLE);
			this.switchLayout.setVisibility(View.VISIBLE);
			this.btnUserManager.setOnClickListener(this);
//			if (LockService.isRunning(getApplicationContext())) {
			if (LockService.getStatus() == LockService.Status.RUNNING) {
				this.toggleService.setChecked(true);
			}
			else {
				this.toggleService.setChecked(false);
			}
			this.toggleService.setOnCheckedChangeListener( new StartServiceOnCheckedChangeListenerImpl() );
			Cfg.getInstance().setUserName(userName);
		}
		else {
			Cfg.getInstance().setUserName(userName);
		}
		
//		userCheck();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.v(life_tag, "onStart");
		userCheck();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.v(life_tag, "onResume");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.v(life_tag, "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.v(life_tag, "onStop");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(life_tag, "onDestroy");
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.v(life_tag, "onRestart");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * 检查用户目录结构
	 */
	private void userCheck() {
		String username = Cfg.getInstance().getUserName();
		String rootDir = Cfg.getInstance().getRootDir();
		String userFileDir = rootDir + Cfg.getInstance().getUsersPath() + File.separator
				+ username + File.separator;
		File userDir = new File(userFileDir);
		Log.d("userDir", userFileDir);
		if (!userDir.exists())
			userDir.mkdirs();
		
		File featrue = new File(userFileDir + username + Cfg.getInstance().getFeaSuf());
		File model = new File(userFileDir + username + Cfg.getInstance().getMdlSuf());
		if ( featrue.exists() && model.exists() ) {
			trained = true;
			tvTrainDesc.setText("已训练");
		}
		else {
			trained = false;
			tvTrainDesc.setText("尚未训练");
		}
		Log.d("trained", ""+trained);
	}
	
	private class StartServiceOnCheckedChangeListenerImpl implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Intent service = new Intent(MainActivity.this, LockService.class);
            if (isChecked) {  
    			MainActivity.this.startService(service);
            } else {  
    			MainActivity.this.stopService(service);
            }  
		}
		
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		Intent intent = null;
		switch (id) {
			case R.id.btn_config:
				intent = new Intent(MainActivity.this, ConfigActivity.class);
				MainActivity.this.startActivity(intent);
				break;
			case R.id.btn_open_train:
			case R.id.layout_train:
				intent = new Intent(MainActivity.this, TrainActivity.class);
				MainActivity.this.startActivity(intent);
				break;
			case R.id.btn_modify_passwd:
				intent = new Intent(MainActivity.this, PasswordActivity.class);
				MainActivity.this.startActivity(intent);
				break;
//			case R.id.btn_mfcctest:
//				intent = new Intent(MainActivity.this, MfccTestActivity.class);
//				MainActivity.this.startActivity(intent);
//				break;
			case R.id.btn_user_manager:
				intent = new Intent(MainActivity.this, UsersActivity.class);
				MainActivity.this.startActivity(intent);
				break;
			case R.id.btn_test_auth:
				intent = new Intent(MainActivity.this, LockActivity.class);
				MainActivity.this.startActivity(intent);
				break;
			case R.id.btn_record_30:
				intent = new Intent(MainActivity.this, DeveloperActivity.class);
				MainActivity.this.startActivity(intent);
				break;
			default:
				break;
		}
	}

}
