package com.nigel.ecustlock;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConfigActivity extends Activity {
	
	EditText etThreshold;
	Button btnSave;
	
	// 默认阈值
	float defaultThreshold = -50;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		
		etThreshold = (EditText) super.findViewById(R.id.et_threshold);
		etThreshold.setOnFocusChangeListener(new EditViewOnFocusChangeListener());
		
		btnSave = (Button) super.findViewById(R.id.btn_save);
		btnSave.setOnClickListener(new SaveOnClickListener());
		
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
	
	public class EditViewOnFocusChangeListener implements OnFocusChangeListener {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			int id = v.getId();
			switch (id) {
				case R.id.et_threshold:
					break;
				default:
					break;
			}
		}
		
	}
	
	public class SaveOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
				case R.id.btn_save:
					float threshold = Float.parseFloat(etThreshold.getText().toString());
					Log.d("threshold", ""+threshold);
					SharedPreferences sharedPref = getSharedPreferences(getString(R.string.s_settingsPreferences), Context.MODE_PRIVATE);
					String key = getString(R.string.s_settingsThreshold);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putFloat(key, threshold);
					editor.commit();
					Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
			}
		}
		
	}
}
