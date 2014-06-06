package com.nigel.ecustlock;


import com.support.Cfg;
import com.support.SqlOpenHelper;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordActivity extends Activity {

	private Button m_ModifyPwd;
	private EditText m_EditPwdOld;
	private EditText m_EditPwdNew;
	private EditText m_EditPwdComfirm;
	
	private String sPwdOld;
	private String sPwdNew;
	private String sPwdComfirm;
	
	SQLiteDatabase database = null;
	
	String ac_tag = "PasswordActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_passwd);
		
		Log.v(ac_tag, "onCreate()");
		
		m_ModifyPwd = (Button) this.findViewById(R.id.btn_UM_modify_pwd);
		m_ModifyPwd.setOnClickListener(new ModifyOnClickListener());
		m_EditPwdOld = (EditText) this.findViewById(R.id.et_UM_old_pwd);
		m_EditPwdNew = (EditText) this.findViewById(R.id.et_UM_new_pwd);
		m_EditPwdComfirm = (EditText) this.findViewById(R.id.et_UM_comfirm_pwd);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		SqlOpenHelper helper = new SqlOpenHelper(getApplicationContext());
		database = helper.getWritableDatabase();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		database.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	class ModifyOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			sPwdOld = m_EditPwdOld.getText().toString();
			sPwdNew = m_EditPwdNew.getText().toString();
			sPwdComfirm = m_EditPwdComfirm.getText().toString();
			
			String name = Cfg.getInstance().getUserName();
			String[] columns = {SqlOpenHelper.USER_NAME, SqlOpenHelper.USER_PWD};
			String[] params = {name};
			Cursor result = database.query(SqlOpenHelper.TABLE_USERINFO, columns, "username=?", params, null, null, null);
			
			if ( result.getCount() == 0 ) {
				Toast.makeText(PasswordActivity.this, "该用户不存在", Toast.LENGTH_SHORT).show();
				return ;
			}
			
			if ( !sPwdNew.equals(sPwdComfirm) ) {
				Toast.makeText(PasswordActivity.this, "确认密码不一致", Toast.LENGTH_SHORT).show();
				return ;
			}
			
			if (sPwdNew.length() < 4) {
				Toast.makeText(getApplicationContext(), "密码长度不能小于4", Toast.LENGTH_SHORT).show();
				return ;
			}
			
			
			result.moveToFirst();
			while ( !result.isAfterLast() ) {
				String pwd = result.getString(1);
				if ( !sPwdOld.equals(pwd) ) {
					Toast.makeText(PasswordActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
					return ;
				}
				result.moveToNext();
			}
			
			ContentValues values = new ContentValues();
			values.put(SqlOpenHelper.USER_NAME, name);
			values.put(SqlOpenHelper.USER_PWD, sPwdNew);
			database.update(SqlOpenHelper.TABLE_USERINFO, values, "username=?", params);
			
			Toast.makeText(PasswordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}
	
	
}
