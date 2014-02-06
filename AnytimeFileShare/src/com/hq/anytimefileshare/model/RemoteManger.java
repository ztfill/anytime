package com.hq.anytimefileshare.model;

import java.util.ArrayList;

import com.hq.anytimefileshare.model.dao.RemoteInfo;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RemoteManger {
	static final String TABLE_NAME = "remote";
	static final String REMOTE_ID = "remote_id";
	static final String REMOTE_ADDR = "remote_addr";
	static final String REMOTE_DOMAIN = "remote_domain";
	static final String REMOTE_PATH = "remote_path";
	static final String REMOTE_USERNAME = "remote_username";
	static final String REMOTE_USERPWD = "remote_userpwd";
	static SQLiteDatabase mDB = null;
	
	public RemoteManger(Context context) {
		if ((mDB == null) || (!mDB.isOpen())) {
			DatabaseHelper dh = new DatabaseHelper(context);
			mDB = dh.getWritableDatabase();
		}
	}
		
	public ArrayList<RemoteInfo> getAll() throws Exception {
		ArrayList<RemoteInfo> list = new ArrayList<RemoteInfo>();
	
		try {
			Cursor cursor = mDB.rawQuery("SELECT * FROM "+ TABLE_NAME + " ORDER BY " + REMOTE_ID + " DESC", null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					RemoteInfo remoteInfo = new RemoteInfo();
					
					remoteInfo.setId(cursor.getInt(0));
					remoteInfo.setAddr(cursor.getString(1));
					remoteInfo.setDomain(cursor.getString(2));
					remoteInfo.setPath(cursor.getString(3));
					remoteInfo.setUserName(cursor.getString(4));
					remoteInfo.setUserPwd(cursor.getString(5));
					list.add(remoteInfo);
				}
				
			}
			cursor.close();
		} catch (Exception e) {
			Log.e("RemoteManger", "Select remote fail:" + e.getMessage());
			throw e;
		}
		
		return list;
	}
	
	public void insert(String addr, String domain, String path, 
						String userName, String userPwd) throws Exception {
		String sql = String.format("INSERT INTO %s(%s, %s, %s, %s, %s) values(?, ?, ?, ?, ?)", 
				TABLE_NAME, REMOTE_ADDR, REMOTE_DOMAIN, REMOTE_PATH, 
				REMOTE_USERNAME, REMOTE_USERPWD);
		try {
			mDB.execSQL(sql, new Object[]{addr, domain,path, userName, userPwd});
		} catch (Exception e) {
			Log.e("RemoteManger", "Insert fail:" + e.getMessage());
			throw e;
		}
	}
	
	public void delete(int id) throws Exception {
		String sql = String.format("DELETE FROM %s WHERE %s=?", TABLE_NAME, REMOTE_ID);
		try {
			mDB.execSQL(sql, new Object[]{id});
		} catch (Exception e) {
			Log.e("RemoteManger", "Delete fail:" + e.getMessage());
			throw e;
		}
	}
	
	class DatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "fileshare.db";
		private static final int DATABASE_VERSION = 1;
		private static final String DATABASE_CREATE = 
				"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
				REMOTE_ID + " integer PRIMARY KEY AUTOINCREMENT," +
				REMOTE_ADDR + " VARCHAR(256) NOT NULL," +
				REMOTE_DOMAIN + " VARCHAR(256)," +
				REMOTE_PATH + " VARCHAR(256)," +
				REMOTE_USERNAME + " VARCHAR(256)," +
				REMOTE_USERPWD + " VARCHAR(256)" +
				");";
	
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
		@Override
		public void onCreate(SQLiteDatabase arg0)  {
			try {
				arg0.execSQL(DATABASE_CREATE);
			} catch (SQLException e) {
				//throw e;
				Log.e("DatabaseHelper", e.getMessage());
			}
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/*
			try {
				db.execSQL(DATABASE_CREATE);
			} catch (SQLException e) {
				//throw e;
				Log.e("DatabaseHelper", e.getMessage());
			}
			*/
	
		}
	
	}
}
