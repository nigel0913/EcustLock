package com.nigel.ecustlock;

import java.lang.reflect.Method;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class LockActivity extends Activity {
	
	TextView time = null;
	TextView date = null;
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
		
		this.time = (TextView) super.findViewById(R.id.tTime);
		this.date = (TextView) super.findViewById(R.id.tDate);
		setTopRunning(true);
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
		this.time.setText("" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
		this.date.setText("" + c.get(Calendar.YEAR) + "Äê" + (c.get(Calendar.MONTH)+1) + "ÔÂ" + c.get(Calendar.DAY_OF_MONTH)+ "ÈÕ");
        setTopRunning(true);
    }
    
	@Override
    protected void onPause() {
        super.onPause();
        
        setTopRunning(false);
    }
	
    @Override
    protected void onStop() {
        super.onStop();
        
        setTopRunning(false);
    }
	
	private void setTopRunning(boolean running) {
	    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    SharedPreferences.Editor editor = pref.edit();

	    editor.putBoolean(PREF_IS_TOP_RUNNING, running);
	    editor.commit();
	}

	public static boolean isTopRunning(Context ctx) {
	    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
	    return pref.getBoolean(PREF_IS_TOP_RUNNING, false);
	}

	
	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        try {
            Object service = getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method test = statusbarManager.getMethod("collapse");
            test.invoke(service);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
