package com.nigel.ecustlock;

import java.text.DecimalFormat;
import java.util.Calendar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class LockActivity extends Activity {
	
	TextView timeView = null;
	TextView dateView = null;
	TextView progressView = null;
	
	View mPassView = null;
	private int mShortAnimationDuration;
	
	DecimalFormat decimalFormat;
	
	private final static String PREF_IS_TOP_RUNNING = "ActivityTopRunning";
	
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
		this.mPassView = findViewById(R.id.editPass);
		
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
		Calendar c = Calendar.getInstance();
		this.timeView.setText("" + decimalFormat.format(c.get(Calendar.HOUR_OF_DAY)) + ":" + decimalFormat.format(c.get(Calendar.MINUTE)) );
		this.dateView.setText("" + decimalFormat.format(c.get(Calendar.MONTH)+1) + "月" + decimalFormat.format(c.get(Calendar.DAY_OF_MONTH))+ "日");
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		this.mPassView.setVisibility(View.GONE);
		this.progressView.setAlpha(1f);
		this.progressView.setVisibility(View.VISIBLE);
		this.progressView.setText("进度0");
		AuthenTask ATask = new AuthenTask();
		ATask.execute(100,30);
	}
	
	@Override
    protected void onPause() {
        super.onPause();
    }
	
    @Override
    protected void onStop() {
        super.onStop();
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
    
	public class AuthenTask extends AsyncTask<Integer, Integer, String> {

		@Override  
	    protected void onPreExecute() {  
	        //第一个执行方法  
	        super.onPreExecute();  
	    }  
	      
		@Override
		protected String doInBackground(Integer... progress) {
			// TODO Auto-generated method stub
			String s = "处理成功";
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
	         //这个函数在doInBackground调用publishProgress时触发，虽然调用时只有一个参数  
	         //但是这里取到的是一个数组,所以要用progesss[0]来取值  
	         //第n个参数就用progress[n]来取值  
	         LockActivity.this.progressView.setText("进度"+progress[0]);  
	         super.onProgressUpdate(progress);  
	     }  

	     @Override  
	     protected void onPostExecute(String result) {  
	         //doInBackground返回时触发，换句话说，就是doInBackground执行完后触发  
	         //这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"  
	    	 LockActivity.this.progressView.setText(result);  
	    	 crossfade();
	         super.onPostExecute(result);  
	     }  


	}
}
