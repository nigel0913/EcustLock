package com.nigel.ecustlock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.support.Cfg;
import com.support.SqlOpenHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
	
	SQLiteDatabase database = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Log.v(LOG_TAG, "onCreate()");
		
		this.btnLogin = (Button) super.findViewById(R.id.btn_login);
		this.evUsername = (EditText) super.findViewById(R.id.et_username);
		this.evPassword = (EditText) super.findViewById(R.id.et_password);
		
		this.btnLogin.setOnClickListener( new LoginOnClickListener() );
		
		SqlOpenHelper helper = new SqlOpenHelper(getApplicationContext());
		database = helper.getReadableDatabase();
		Init();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		database.close();
	}
	
	public void Init() {
		boolean sdcardExists = false;
		if (sdcardExists = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			String rootDir = Environment.getExternalStorageDirectory().toString() + File.separator + this.recDir + File.separator;
			this.innovationSaveFileDir = new File(rootDir);
			if (!this.innovationSaveFileDir.exists()){
				this.innovationSaveFileDir.mkdirs();
			}
			
			Cfg.getInstance().setRootDir(rootDir);
			boolean firstRun = false;
			SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
					getString(R.string.s_settingsPreferences),
					Context.MODE_PRIVATE
					);
			firstRun = sharedPref.getBoolean(getString(R.string.s_settingsFirstRun), true);
			if (firstRun) {
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean(getString(R.string.s_settingsFirstRun), false);
				editor.commit();
			}
			InitFiles(firstRun);
		}
	}
	
	class LoginOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			String username = evUsername.getText().toString();
			String password = evPassword.getText().toString();
			
			if (username.equals("")) {
				Toast.makeText(LoginActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
				return ;
			}
			
			String[] columns = {"username", "password"};
			String[] params = {username};
			Cursor cursor = database.query(
					SqlOpenHelper.TABLE_USERINFO, columns, "username=?", params, null, null, null);
			
			if ( cursor.getCount() == 0 ) {
				Toast.makeText(LoginActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
				return ;
			}
			
			String correctPwd = "";
			
			// TODO need to judge cursor.getCount()
			cursor.moveToFirst();
			while ( !cursor.isAfterLast() ) {
				correctPwd = cursor.getString(1);
				cursor.moveToNext();
			}
//			Log.d("database", cursor.getCount() + "[" + username + "][" + password + "][" + correctPwd + "]");
			if (password.equals(correctPwd)) {
				Log.v(LOG_TAG, "in");
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				// TODO modify user_name tags
				intent.putExtra("user_name", username);
				LoginActivity.this.startActivity(intent);
				finish();
			}
			else {
				Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	/**
	 * 初始化系统文件结构
	 */
	private void InitFiles(boolean firstRun) {
		File fDir = null;
		for (String dirName : Cfg.getInstance().getDirLists()) {
			fDir = new File(Cfg.getInstance().getRootDir() + File.separator + dirName + File.separator);
			if ( !fDir.exists() ) {
				fDir.mkdirs();
			}
		}
		
		File worldModel = new File(Cfg.getInstance().getRootDir() + File.separator + Cfg.getInstance().getWorldMdlPath() + File.separator + Cfg.getInstance().getWorldMdlFile());
		if ( !worldModel.exists() || firstRun ) {
			copyAssets(Cfg.getInstance().getWorldMdlFile(), Cfg.getInstance().getRootDir() + File.separator + Cfg.getInstance().getWorldMdlPath());
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
