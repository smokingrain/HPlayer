/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static SQLiteDatabase mDb;
	private static DatabaseHelper mHelper;
	private static final int DB_VERSION = 4;
	private static final String DB_NAME = "musicstore_new";
	private static final String TABLE_ALBUM = "album_info";
	private static final String TABLE_ARTIST = "artist_info";
	private static final String TABLE_MUSIC = "music_info";
	private static final String TABLE_FOLDER = "folder_info";
	private static final String TABLE_FAVORITE = "favorite_info";

	public static SQLiteDatabase getInstance(Context context) {
		if (mDb == null) {
			mDb = getHelper(context).getWritableDatabase();
		}
		return mDb;
	}
	
	public static DatabaseHelper getHelper(Context context) {
		if(mHelper == null) {
			mHelper = new DatabaseHelper(context);
		}
		return mHelper;
	}

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("com.xk.hplayer", "create db!");
		db.execSQL("create table "
				+ TABLE_MUSIC
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " songid integer, albumid integer, duration integer, musicname varchar(10), "
				+ "artist char, data char, folder char, musicnamekey char, artistkey char, favorite integer)");
		Log.i("com.xk.hplayer", "create music!");
		db.execSQL("create table "
				+ TABLE_ALBUM
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "album_name char, album_id integer, number_of_songs integer, album_art char)");
		Log.i("com.xk.hplayer", "create album!");
		db.execSQL("create table "
				+ TABLE_ARTIST
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, artist_name char, number_of_tracks integer)");
		Log.i("com.xk.hplayer", "create artist!");
		db.execSQL("create table "
				+ TABLE_FOLDER
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, folder_name char, folder_path char)");
		Log.i("com.xk.hplayer", "create folder!");
		try {
			db.execSQL("create table "
					+ TABLE_FAVORITE
					+ " (_id integer,"
					+ " songid integer, albumid integer, duration integer, musicname varchar(10), "
					+ "artist char, data char, folder char, musicnamekey char, artistkey char, favorite integer)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i("com.xk.hplayer", "create error!!!" + e.getMessage());
		}
		
		Log.i("com.xk.hplayer", "db created!!");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("com.xk.hplayer", "update db!");
		if (newVersion > oldVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTIST);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUM);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSIC);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDER);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE);
			onCreate(db);
		}
		Log.i("com.xk.hplayer", "db updated");
	}
	
	public void deleteTables(Context context) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_ALBUM, null, null);
		db.delete(TABLE_ARTIST, null, null);
		db.delete(TABLE_FAVORITE, null, null);
		db.delete(TABLE_FOLDER, null, null);
		db.delete(TABLE_MUSIC, null, null);
	}

}
