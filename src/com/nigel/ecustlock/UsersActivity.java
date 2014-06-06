package com.nigel.ecustlock;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.support.Cfg;
import com.support.FileAccess;
import com.support.SqlOpenHelper;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class UsersActivity extends Activity {

	List<String> users;
	ExpandableListView userListView;
	
	SQLiteDatabase database = null;
	
	static int oldPosition = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_manager);
		users = new ArrayList<String>();
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		SqlOpenHelper helper = new SqlOpenHelper(getApplicationContext());
		database = helper.getWritableDatabase();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.user_manager, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add:
				Intent intent = new Intent(UsersActivity.this, RegisterActivity.class);
				UsersActivity.this.startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		generateUserList();
		userListView = (ExpandableListView) super.findViewById(R.id.list_user);
		final UserExpandableListAdapter userListAdapter = new UserExpandableListAdapter(this, users);
		userListView.setAdapter(userListAdapter);
		userListView.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				if (oldPosition != -1 && oldPosition != groupPosition) {
					userListView.collapseGroup(oldPosition);
				}
				oldPosition = groupPosition;
			}
			
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		database.close();
	}
	
	public boolean deleteUser(int groupPosition, String name) {
		String[] args = {name};
		int res = database.delete(SqlOpenHelper.TABLE_USERINFO, "username=?", args);
		
		if (res == 0)
			return false;
		
		File userDir =new File(Cfg.getInstance().getRootDir() + Cfg.getInstance().getUsersPath() + 
				File.separator + name + File.separator);
		if (userDir.exists()) {
			FileAccess.deleteDirectory(userDir);
		}
		
		if (oldPosition != -1) {
			userListView.collapseGroup(oldPosition);
			oldPosition = -1;
		}
		
		return true;
	}
	
	private void generateUserList() {
		users.clear();
		String[] columns = {SqlOpenHelper.USER_NAME};
		Cursor result = database.query(SqlOpenHelper.TABLE_USERINFO, columns, null, null, null, null, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			String name = result.getString(0);
			if (!name.equals("admin"))
				users.add( name );
			result.moveToNext();
		}
	}
}
