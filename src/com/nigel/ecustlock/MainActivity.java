package com.nigel.ecustlock;

import com.nigel.service.LockService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class MainActivity extends Activity implements OnClickListener {

	Switch toggleService = null;
	Button btnUserManager = null;
	Button btnOpenTrain = null;
	Button btnConfig = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.toggleService = (Switch) super.findViewById(R.id.service_switch);
		this.btnUserManager = (Button) super.findViewById(R.id.btn_user_manager);
		this.btnOpenTrain = (Button) super.findViewById(R.id.btn_open_train);
		this.btnConfig = (Button) super.findViewById(R.id.btn_config);
		
		if (LockService.isRunning(getApplicationContext())) {
			this.toggleService.setChecked(true);
		}
		else {
			this.toggleService.setChecked(false);
		}
		this.toggleService.setOnCheckedChangeListener( new StartServiceOnCheckedChangeListenerImpl() );
		
		this.btnUserManager.setOnClickListener(this);
		this.btnOpenTrain.setOnClickListener(this);
		this.btnConfig.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
				intent = new Intent(MainActivity.this, TrainActivity.class);
				MainActivity.this.startActivity(intent);
				break;
			case R.id.btn_user_manager:
				intent = new Intent(MainActivity.this, PasswordActivity.class);
				MainActivity.this.startActivity(intent);
			default:
				break;
		}
	}

}
