package com.nigel.ecustlock;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DeveloperActivity extends Activity {

	EditText etUsername;
	Button btnRecord30;
	Button btnRecord10;
	Button btnStop;
	ProgressBar pbLeftTime;
	TextView tvLeftTime;
	Button btnTrainNew;
	Button btnTrainAll;
	Button btnTrainOne;
	TextView tvSelectedUsername;
	Button btnCalAll;
	TextView tvThreshold;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_developer);
		
		etUsername = (EditText) super.findViewById(R.id.dev_username);
		btnRecord30 = (Button) super.findViewById(R.id.btn_record_30);
		btnRecord10 = (Button) super.findViewById(R.id.btn_record_10);
		btnStop = (Button) super.findViewById(R.id.dev_btn_stop);
		pbLeftTime = (ProgressBar) super.findViewById(R.id.dev_time_progressbar);
		tvLeftTime = (TextView) super.findViewById(R.id.dev_left_time);
	}
	
}
