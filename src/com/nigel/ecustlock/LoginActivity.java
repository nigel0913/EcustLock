package com.nigel.ecustlock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.support.Config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	Button btnLogin = null;
	EditText evUsername = null;
	EditText evPassword = null;
	
	/**
	 * these used for checking file directory
	 */
	File innovationSaveFileDir = null;
	String recDir = "aEcustLock";
	
	String LOG_TAG = "LoginActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Log.v(LOG_TAG, "onCreate()");
		
		this.btnLogin = (Button) super.findViewById(R.id.btn_login);
		this.evUsername = (EditText) super.findViewById(R.id.et_username);
		this.evPassword = (EditText) super.findViewById(R.id.et_password);
		
		this.btnLogin.setOnClickListener( new LoginOnClickListener() );
		Init();
	}
	
	public void Init() {
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.s_settingsPreferences), Context.MODE_PRIVATE);
		String key = getString(R.string.s_settingsPasswordKey);
		// set default password = 0000
		if (!sharedPref.contains(key)) {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(key, "0000");
			editor.commit();
		}
		boolean sdcardExists = false;
		if (sdcardExists = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			String rootDir = Environment.getExternalStorageDirectory().toString() + File.separator + this.recDir + File.separator;
			this.innovationSaveFileDir = new File(rootDir);
			if (!this.innovationSaveFileDir.exists()){
				this.innovationSaveFileDir.mkdirs();
			}
			
			Config.setRootDir(rootDir);
			InitFiles();
		}
	}
	
	class LoginOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			SharedPreferences sharedPref = getSharedPreferences(getString(R.string.s_settingsPreferences), Context.MODE_PRIVATE);
			String key = getString(R.string.s_settingsPasswordKey);
			String pwd = sharedPref.getString(key, "0000");
			
			// TODO add user name verify
			
			if (evPassword.getText().toString().equals(pwd)) {
				Log.v(LOG_TAG, "in");
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				// TODO modify user_name tags
				intent.putExtra("user_name", evUsername.getText().toString());
				LoginActivity.this.startActivity(intent);
				finish();
			}
			else {
				Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	/**
	 * 初始化文件列表
	 */
	private void InitFiles() {
		File fDir = null;
		for (String dirName : Config.getDirLists()) {
			fDir = new File(Config.getRootDir() + File.separator + dirName + File.separator);
			if ( !fDir.exists() ) {
				fDir.mkdirs();
			}
		}
		
		File worldModel = new File(Config.getRootDir() + File.separator + Config.getWorldModelPath() + File.separator + Config.getWorldModelFile());
		if ( !worldModel.exists() ) {
			copyAssets(Config.getWorldModelFile(), Config.getRootDir() + File.separator + Config.getWorldModelPath());
		}
	}
	
	/**
	 * copy assets/filename to dir/filename
	 * @param filename  
	 * @param dir 输出目录
	 */
	private void copyAssets(String filename, String dir) {
		AssetManager assetManager = getAssets();

		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(filename);
			File outFile = new File(dir, filename);
			out = new FileOutputStream(outFile);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch (IOException e) {
			Log.e("tag", "Failed to copy asset file: " + filename, e);
		}
	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
}
