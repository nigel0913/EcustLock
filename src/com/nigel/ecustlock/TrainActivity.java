package com.nigel.ecustlock;

import java.io.File;

import com.support.Config;
import com.support.SRecord;
import com.support.SThread;
import com.support.Config.DOTYPE;
import com.support.SRecord.EResultType;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TrainActivity extends Activity {

	Button btnTrain = null;
	TextView tvInfo = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_train);
		
		this.btnTrain = (Button) super.findViewById(R.id.btn_train);
		this.btnTrain.setOnClickListener(new TrainOnClickListenserImpl());
		
		EResultType result = SRecord.getInstance().createRecord();
		if (result == EResultType.SUCCESS) {
			this.tvInfo = (TextView)super.findViewById(R.id.tv_info);
			this.tvInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
			
			SRecord.getInstance().setDirs( Config.getRootDir() + File.separator + Config.getRawPath() );
			SRecord.getInstance().setFileName( Config.getUserName() );
		}
		else {
			this.tvInfo.setText("录音设备初始化失败！");
			this.btnTrain.setEnabled(false);
		}
	}
	
	@Override
	protected void onDestroy() {
		SRecord.getInstance().releaseRecord();
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	private class TrainOnClickListenserImpl implements OnClickListener {

		@Override
		public void onClick(View v) {
			Config.setType(DOTYPE.TRAIN);
			
			if (v == btnTrain) {
				if ( SRecord.getInstance().isRecording() ) {
					SRecord.getInstance().closeRecord();
					SRecord.getInstance().releaseRecord();
					
					SThread mfcc = new SThread();
					mfcc.getMfcc();
					
					btnTrain.setText("开始录音");
				}
				else {
					if ( !SRecord.getInstance().isCreate() )
						SRecord.getInstance().createRecord();
					SRecord.getInstance().startRecord();
					btnTrain.setText("结束录音");
				}
			}
		}
		
	}
	
}
