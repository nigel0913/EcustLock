package com.support;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlOpenHelper extends SQLiteOpenHelper {

	public static final String DBNAME = "elockdb.sqlite";
	public static final int VERSION = 1;
	
	public static final String TABLE_USERINFO = "user_info";
	
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
				+ "id integer primary key autoincrement not null,"
				+ "username varchar(16),"
				+ "password varchar(16),"
				+ "train_time timestamp,"
				+ "last_login_time timestamp"
				+ ");");
		
		ContentValues values = new ContentValues();
		values.put("username", "admin");
		values.put("password", "0000");
		db.insert(TABLE_USERINFO, null, values);
	}
}
