package com.nigel.ecustlock;

import com.support.SqlOpenHelper;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {

	EditText username = null;
	EditText passwd = null;
	EditText passwd2 = null;
	Button register = null;
	
	SQLiteDatabase database = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		username = (EditText) super.findViewById(R.id.reg_username);
		passwd = (EditText) super.findViewById(R.id.reg_password);
		passwd2 = (EditText) super.findViewById(R.id.reg_password2);
		register = (Button) super.findViewById(R.id.btn_register);
		
		register.setOnClickListener(this);
		
		SqlOpenHelper helper = new SqlOpenHelper(getApplicationContext());
		database = helper.getWritableDatabase();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		database.close();
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.btn_register:
				registerUser();
				break;
			default:
				break;
		}
	}
	
	void registerUser() {
		
		String name = username.getText().toString();
		String pwd1 = passwd.getText().toString();
		String pwd2 = passwd2.getText().toString();
		
		Log.d("register", "["+name + "][" + pwd1 + "][" + pwd2 + "]");
		if (name.equals("")) {
			Toast.makeText(getApplicationContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
			return ;
		}
		
		String[] columns = {SqlOpenHelper.USER_NAME};
		String[] params = {name};
		Cursor result = database.query(SqlOpenHelper.TABLE_USERINFO, columns, "username=?", params, null, null, null);
		if (result.getCount() > 0) {
			Toast.makeText(getApplicationContext(), "该用户名已注册", Toast.LENGTH_SHORT).show();
			return ;
		}
		
		if ( !pwd1.equals(pwd2) ) {
			Toast.makeText(getApplicationContext(), "确认密码不一致", Toast.LENGTH_SHORT).show();;
			return ;
		}
		
		ContentValues values = new ContentValues();
		values.put("username", name);
		values.put("password", pwd1);
		database.insert(SqlOpenHelper.TABLE_USERINFO, null, values);
		
		Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
		finish();
	}
	
}
