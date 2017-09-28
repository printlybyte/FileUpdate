/**   
 * @Title: DBHelper.java 
 * @Package com.android.updatadb 
 * @Description: TODO
 * @author Long Li  
 * @date 2015-4-30 下午4:30:02 
 * @version V1.0   
 */
package com.hutu.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * TODO<数据库操作基本类>
 * 
 * @author Long Li
 * @data: 2015-4-30 下午4:30:02
 * @version: V1.0
 */
public class DBHelper extends SQLiteOpenHelper {

	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DBHelper(Context context) {
		super(context, "updatefile.db", null, 1);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table localfile_info(_id integer PRIMARY KEY AUTOINCREMENT, fName varchar, "
				+ "fSize integer, fPath varchar, fType integer, fState integer, fProgress integer, cTime long, fTime long)");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
