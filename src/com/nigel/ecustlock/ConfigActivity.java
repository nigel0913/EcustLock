package com.nigel.ecustlock;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;

public class ConfigActivity extends Activity {
	
	EditText etThreshold;
	
	// ƒ¨»œ„–÷µ
	float defaultThreshold = -50;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		
		etThreshold = (EditText) super.findViewById(R.id.et_threshold);
		etThreshold.setKeyListener(null);
		
		Init();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void Init() {
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.s_settingsPreferences), Context.MODE_PRIVATE);
		String key = getString(R.string.s_settingsThreshold);
		if (!sharedPref.contains(key)) {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putFloat(key, defaultThreshold);
			editor.commit();
			
			etThreshold.setText(String.valueOf(defaultThreshold));
		}
		else {
			float threshold = sharedPref.getFloat(key, defaultThreshold);
			etThreshold.setText( String.valueOf(threshold) );
		}
	}
}
