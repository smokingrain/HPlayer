package com.ldw.music;

import java.io.File;

import com.ldw.music.lrc.LyricLoadHelper;
import com.ldw.music.service.ServiceManager;

import android.app.Application;
import android.os.Environment;

public class MusicApp extends Application {
	
	public static final Long SERVER=-1L;
	public static final String MSG_DISCONNECT="disconnect";
	public static final String MSG_ASK_SEND = "asksend";
	public static final String MSG_SEND_DATA = "senddata";
	public static final String MSG_SEND_END = "sendend";
	public static final String RESULT_FILE_EXISTS = "fileexists";
	public static final String RESULT_OK = "sendok";
	public static final String RESULT_OVER = "sendover";
	public static final String RESULT_NO_CLIENT = "noclient";
	public static final String RESULT_WORKING = "working";
	
	
	public static final String APP="hp";
	public static final String name = "hbb";
	
	public static boolean mIsSleepClockSetting = false;
	public static ServiceManager mServiceManager = null;
	private static String rootPath = "/mymusic";
	public static String lrcPath = "/lrc";
	public static String musicPath = "/music";
	public static String picPath = "/pic";
	public static Long cid;
	
	public static LyricLoadHelper mLyricLoadHelper = new LyricLoadHelper();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mServiceManager = new ServiceManager(this);
		initPath();
	}
	
	private void initPath() {
		String ROOT = "";
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			ROOT = Environment.getExternalStorageDirectory().getPath();
		}
		rootPath = ROOT + rootPath;
		lrcPath = rootPath + lrcPath;
		musicPath = rootPath + musicPath;
		picPath = rootPath + picPath;
		File lrcFile = new File(lrcPath);
		if(!lrcFile.exists()) {
			lrcFile.mkdirs();
		}
		File musicFile = new File(musicPath);
		if(!musicFile.exists()) {
			musicFile.mkdirs();
		}
		File picFile = new File(picPath);
		if(!picFile.exists()) {
			picFile.mkdirs();
		}
	}
}
