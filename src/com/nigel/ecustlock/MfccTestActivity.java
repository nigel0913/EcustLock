package com.nigel.ecustlock;

// √ª”–”√

import com.support.mfcc.MelBankm;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MfccTestActivity extends Activity {

	Button melbankm = null;
	TextView testInfo = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mfcc_test);
		
		melbankm = (Button) super.findViewById(R.id.melbankm);
		testInfo = (TextView) super.findViewById(R.id.testinfo);
		
		melbankm.setOnClickListener(new ClickListenerImpl());
		testInfo.setMovementMethod(new ScrollingMovementMethod());
	}
	
	private class ClickListenerImpl implements OnClickListener {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			
				case R.id.melbankm:
					double[][] bank = MelBankm.melbankm(24, 256, 8000, 0, 0.5);
					String text = "";
					if (bank != null) {
						int height = bank.length;
						int width = bank[0].length;
						for (int i=0; i<height; i++) {
							for (int j=0; j<width; j++) {
								if (bank[i][j] != 0) {
									text += "" + i + " " + j + " " + bank[i][j] + "\n";
								}
							}
						}
					}
					testInfo.setText(text);
					break;
				default:
					break;
			
			}
		}
		
	}
	
}
