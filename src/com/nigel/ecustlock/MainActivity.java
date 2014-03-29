package com.nigel.ecustlock;

import com.nigel.service.LockService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class MainActivity extends Activity {

	Switch toggleService = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.toggleService = (Switch) super.findViewById(R.id.service_switch);
		// TODO set toggleService
		if (LockService.isRunning(getApplicationContext())) {
			this.toggleService.setChecked(true);
		}
		else {
			this.toggleService.setChecked(false);
		}
		this.toggleService.setOnCheckedChangeListener(new StartServiceOnCheckedChangeListenerImpl());
		
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
			// TODO Auto-generated method stub
			Intent service = new Intent(MainActivity.this, LockService.class);
            if (isChecked) {  
    			MainActivity.this.startService(service);
            } else {  
    			MainActivity.this.stopService(service);
            }  
		}
		
	}

}
