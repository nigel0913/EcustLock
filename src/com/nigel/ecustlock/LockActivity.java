package com.nigel.ecustlock;

import java.text.DecimalFormat;
import java.util.Calendar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class LockActivity extends Activity {
	
	TextView timeView = null;
	TextView dateView = null;
	TextView progressView = null;
	
	EditText mPassView = null;
	private int mShortAnimationDuration;
	
	DecimalFormat decimalFormat;
	static String[] weekDaysName = { "������", "����һ", "���ڶ�", "������", "������", "������", "������" };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Window win = getWindow(); 
		WindowManager.LayoutParams winParams = win.getAttributes(); 
		winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD 
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED 
				| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_FULLSCREEN); 
//				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); 
		win.setAttributes(winParams); 
		setContentView(R.layout.activity_lock);
		
		this.timeView = (TextView) super.findViewById(R.id.tTime);
		this.dateView = (TextView) super.findViewById(R.id.tDate);
		this.progressView = (TextView) super.findViewById(R.id.tProgress);
		this.mPassView = (EditText) findViewById(R.id.editPass);
		this.mPassView.addTextChangedListener(new CheckPassword());
		
		decimalFormat = new DecimalFormat("00");
		
		// Initially hide the content view.
		this.mPassView.setVisibility(View.GONE);
        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    protected void onStart() {
        super.onStart();
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		Calendar c = Calendar.getInstance();
		int weekIndex = c.get(Calendar.DAY_OF_WEEK) - 1;
		
		this.timeView.setText("" + decimalFormat.format(c.get(Calendar.HOUR_OF_DAY)) + ":" + decimalFormat.format(c.get(Calendar.MINUTE)) );
		this.dateView.setText("" + decimalFormat.format(c.get(Calendar.MONTH)+1) + "��"
			+ decimalFormat.format(c.get(Calendar.DAY_OF_MONTH))+ "��  "
			+ weekDaysName[weekIndex]);
		this.mPassView.setVisibility(View.GONE);
		
		this.progressView.setAlpha(1f);
		this.progressView.setVisibility(View.VISIBLE);
		this.progressView.setText("����0");
		AuthenTask ATask = new AuthenTask();
		ATask.execute(100,30);
	}
	
    private void crossfade() {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
    	this.mPassView.setAlpha(0f);
    	this.mPassView.setVisibility(View.VISIBLE);
    	
    	// Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
    	this.mPassView.animate()
    		.alpha(1f)
    		.setDuration(mShortAnimationDuration)
    		.setListener(null);
    	
    	// Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        this.progressView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    	progressView.setVisibility(View.GONE);
                    }
                });
    }
    
    public class CheckPassword implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			try {
				Log.d("Password", "input: "+s);
				String lockPass = "6666";
				if ( s.toString().equals(lockPass) ) {
					Log.d("Password", "LockActivity finish()");
					LockActivity.this.finish();
				}
			} catch (NumberFormatException nfe) {
				// none
			}
		}

    	
    }
    
	public class AuthenTask extends AsyncTask<Integer, Integer, String> {

		@Override  
	    protected void onPreExecute() {  
	        //��һ��ִ�з���  
	        super.onPreExecute();  
	    }  
	      
		@Override
		protected String doInBackground(Integer... progress) {
			String s = "�����ɹ�";
			for(int i=0;i<=progress[1];i++){  
	            try {  
	            	this.publishProgress(i);
	                Thread.sleep(progress[0]);  
	            } catch (InterruptedException e) {  
	                e.printStackTrace();  
	            }  
	        }  
			return s;
		}
		
		 @Override  
	     protected void onProgressUpdate(Integer... progress) {  
	         //���������doInBackground����publishProgressʱ��������Ȼ����ʱֻ��һ������  
	         //��������ȡ������һ������,����Ҫ��progesss[0]��ȡֵ  
	         //��n����������progress[n]��ȡֵ  
	         LockActivity.this.progressView.setText("����"+progress[0]);  
	         super.onProgressUpdate(progress);  
	     }  

	     @Override  
	     protected void onPostExecute(String result) {  
	         //doInBackground����ʱ���������仰˵������doInBackgroundִ����󴥷�  
	         //�����result��������doInBackgroundִ�к�ķ���ֵ������������"ִ�����"  
	    	 LockActivity.this.progressView.setText(result);  
	    	 crossfade();
	         super.onPostExecute(result);  
	     }  


	}
}