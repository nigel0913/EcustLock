package com.nigel.ecustlock;

import com.nigel.custom.MicButton;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class AuthActivity extends Activity implements OnClickListener, OnTouchListener {

	MicButton btnAuth = null;
	TextView tvInfo = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		btnAuth = new MicButton(getApplicationContext());
		setContentView(R.layout.activity_auth);
		
		btnAuth = (MicButton) super.findViewById(R.id.micButton1);
		tvInfo = (TextView) super.findViewById(R.id.tv_test_info);
		btnAuth.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		btnAuth.setOnClickListener(this);
		btnAuth.setOnTouchListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_test_info:
				
				break;
		}
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		switch (v.getId()) {
			case R.id.micButton1:
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					tvInfo.setText("按下");
				else if (event.getAction() == MotionEvent.ACTION_UP)
					tvInfo.setText("收回");
					
				return false;
		}
		
		return false;
	}
	
}
