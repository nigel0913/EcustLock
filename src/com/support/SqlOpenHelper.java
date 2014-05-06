package com.support;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlOpenHelper extends SQLiteOpenHelper {

	public static final String DBNAME = "elockdb.sqlite";
	public static final int VERSION = 1;
	
	public static final String TABLE_USERINFO = "user_info";
	public static final String USER_ID = "id";
	public static final String USER_NAME = "username";
	public static final String USER_PWD = "password";
	public static final String USER_TRAIN_TIME = "train_time";
	public static final String USER_LOGIN_TIME = "last_login_time";
	
	public SqlOpenHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createDatabase(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_USERINFO);
		onCreate(db);
	}

	
	private void createDatabase(SQLiteDatabase db) {
		db.execSQL("create table " + TABLE_USERINFO + " ("
				+ USER_ID + " integer primary key autoincrement not null,"
				+ USER_NAME + " varchar(16),"
				+ USER_PWD + " varchar(16),"
				+ USER_TRAIN_TIME + " timestamp,"
				+ USER_LOGIN_TIME + " timestamp"
				+ ");");
		
		ContentValues values = new ContentValues();
		values.put("username", "admin");
		values.put("password", "0000");
		db.insert(TABLE_USERINFO, null, values);
	}
}
