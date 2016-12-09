/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.service;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

import com.ldw.music.MusicApp;
import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.activity.MainContentActivity;
import com.ldw.music.aidl.IMediaService;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.shake.ShakeDetector;
import com.ldw.music.shake.ShakeDetector.OnShakeListener;
import com.ldw.music.storage.SPStorage;
import com.ldw.music.transfer.MessageHandler;
import com.ldw.music.utils.ConnectionListener;
import com.ldw.music.utils.HTTPUtil;
import com.ldw.music.utils.MessageCallBack;
import com.ldw.music.utils.MessageListener;
import com.ldw.music.utils.MinaClient;

/**
 * 后台Service 控制歌曲的播放 控制顶部Notification的显示
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class MediaService extends Service implements IConstants, OnShakeListener {
	
	private static final String PAUSE_BROADCAST_NAME = "com.ldw.music.pause.broadcast";
	private static final String NEXT_BROADCAST_NAME = "com.ldw.music.next.broadcast";
	private static final String PRE_BROADCAST_NAME = "com.ldw.music.pre.broadcast";
	private static final int PAUSE_FLAG = 0x1;
	private static final int NEXT_FLAG = 0x2;
	private static final int PRE_FLAG = 0x3;
	
	public static final Integer FLUSH_FILE = 1;
	
	private MusicControl mMc;
	private NotificationManager mNotificationManager;
//	private Notification mNotification;
	private int NOTIFICATION_ID = 0x1;
	private RemoteViews rv;
	private ShakeDetector mShakeDetector;
	/** 当前是否正在播放 */
	private boolean mIsPlaying;
	/** 在设置界面是否开启了摇一摇的监听 */
	public boolean mShake;
	private SPStorage mSp;
	private ControlBroadcast mConrolBroadcast;
	private MusicPlayBroadcast mPlayBroadcast;
	
	/** 定时任务 */
	private Timer timer ;
	private LocationManager locationManager;
	
	/**网络通信*/
	private MinaClient mina;
	private MessageListener ml;
	private ConnectionListener cl;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mMc = new MusicControl(this);
		mSp = new SPStorage(this);
		mShakeDetector = new ShakeDetector(this);
		mShakeDetector.setOnShakeListener(this);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
		
//		timer = new Timer();
//		UploadTask task = new UploadTask();
//		timer.schedule(task, 1000 , 1000 * 60 * 5);//5分钟一次
		
		mConrolBroadcast = new ControlBroadcast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(PAUSE_BROADCAST_NAME);
		filter.addAction(NEXT_BROADCAST_NAME);
		filter.addAction(PRE_BROADCAST_NAME);
		registerReceiver(mConrolBroadcast, filter);
		
		mPlayBroadcast = new MusicPlayBroadcast();
		IntentFilter filter1 = new IntentFilter(BROADCAST_NAME);
		filter1.addAction(BROADCAST_SHAKE);
		registerReceiver(mPlayBroadcast, filter1);
		initConnection();
	}

	private void initConnection(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				mina = MinaClient.getInstance();
				ml = new MessageListener();
				cl = new ConnectionListener() {
					
					@Override
					public void connected(Long uid) {
						MusicApp.cid = uid;
					}
				};
				MessageHandler hd = new MessageHandler(mina, msgHanlder);
				ml.registListener(hd);
				mina.setcListener(cl);
				mina.setListener(ml);
				mina.init("10.60.15.162", 5492);
			}
		}).start();
		
	}
	
	/**更新notification
	 * @param bitmap
	 * @param title
	 * @param name
	 */
	private void updateNotification(Bitmap bitmap, String title, String name) {
		Intent intent = new Intent(getApplicationContext(),
				MainContentActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),
				0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		rv = new RemoteViews(this.getPackageName(), R.layout.notification);
		Notification notification = new Notification();
		notification.icon = R.drawable.images;
		notification.tickerText = title;
		notification.contentIntent = pi;
		notification.contentView = rv;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		if(bitmap != null) {
			rv.setImageViewBitmap(R.id.image, bitmap);
		} else {
			rv.setImageViewResource(R.id.image, R.drawable.img_album_background);
		}
		rv.setTextViewText(R.id.title, title);
		rv.setTextViewText(R.id.text, name);
//		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
		
		//此处action不能是一样的 如果一样的 接受的flag参数只是第一个设置的值
		Intent pauseIntent = new Intent(PAUSE_BROADCAST_NAME);
		pauseIntent.putExtra("FLAG", PAUSE_FLAG);
		PendingIntent pausePIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0);
		rv.setOnClickPendingIntent(R.id.iv_pause, pausePIntent);
		
		Intent nextIntent = new Intent(NEXT_BROADCAST_NAME);
		nextIntent.putExtra("FLAG", NEXT_FLAG);
		PendingIntent nextPIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
		rv.setOnClickPendingIntent(R.id.iv_next, nextPIntent);
		
		Intent preIntent = new Intent(PRE_BROADCAST_NAME);
		preIntent.putExtra("FLAG", PRE_FLAG);
		PendingIntent prePIntent = PendingIntent.getBroadcast(this, 0, preIntent, 0);
		rv.setOnClickPendingIntent(R.id.iv_previous, prePIntent);
		
		startForeground(NOTIFICATION_ID, notification);
	}
	
	private class MusicPlayBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_NAME)) {
				int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
				switch (playState) {
				case MPS_PLAYING:
					mIsPlaying = true;
					if(mSp.getShake()) {
						mShakeDetector.start();
					}
					break;
					default:
						mIsPlaying = false;
						mShakeDetector.stop();
				}
			} else if(intent.getAction().equals(BROADCAST_SHAKE)) {
				mShake = intent.getBooleanExtra(SHAKE_ON_OFF, false);
				if(mShake && mIsPlaying) {//如果开启了监听并且歌曲正在播放
					mShakeDetector.start();
				} else if(!mShake) {
					mShakeDetector.stop();
				}
			}
		}
	}
	
	private class ControlBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int flag = intent.getIntExtra("FLAG", -1);
			switch(flag) {
			case PAUSE_FLAG:
//				MediaService.this.stopForeground(true);
				mMc.pause();
				break;
			case NEXT_FLAG:
				mMc.next();
				break;
			case PRE_FLAG:
				mMc.prev();
				break;
			}
		}
	}
	
	private void cancelNotification() {
		stopForeground(true);
		mNotificationManager.cancel(NOTIFICATION_ID);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private class ServerStub extends IMediaService.Stub {

		@Override
		public boolean pause() throws RemoteException {
//			MediaService.this.stopForeground(true);
			return mMc.pause();
		}

		@Override
		public boolean prev() throws RemoteException {
			return mMc.prev();
		}

		@Override
		public boolean next() throws RemoteException {
			return mMc.next();
		}

		@Override
		public boolean play(int pos) throws RemoteException {
			return mMc.play(pos);
		}

		@Override
		public int duration() throws RemoteException {
			return mMc.duration();
		}

		@Override
		public int position() throws RemoteException {
			return mMc.position();
		}

		@Override
		public boolean seekTo(int progress) throws RemoteException {
			return mMc.seekTo(progress);
		}

		@Override
		public void refreshMusicList(List<MusicInfo> musicList) throws RemoteException {
			mMc.refreshMusicList(musicList);
		}

		@Override
		public void getMusicList(List<MusicInfo> musicList) throws RemoteException {
			List<MusicInfo> music = mMc.getMusicList();
			for (MusicInfo m : music) {
				musicList.add(m);
			}
		}

		@Override
		public int getPlayState() throws RemoteException {
			return mMc.getPlayState();
		}

		@Override
		public int getPlayMode() throws RemoteException {
			return mMc.getPlayMode();
		}

		@Override
		public void setPlayMode(int mode) throws RemoteException {
			mMc.setPlayMode(mode);
		}

		@Override
		public void sendPlayStateBrocast() throws RemoteException {
			mMc.sendBroadCast();
		}

		@Override
		public void exit() throws RemoteException {
			cancelNotification();
			stopSelf();
			mMc.exit();
		}

		@Override
		public boolean rePlay() throws RemoteException {
			return mMc.replay();
		}

		@Override
		public int getCurMusicId() throws RemoteException {
			return mMc.getCurMusicId();
		}

		@Override
		public void updateNotification(Bitmap bitmap, String title, String name)
				throws RemoteException {
			MediaService.this.updateNotification(bitmap, title, name);
		}

		@Override
		public void cancelNotification() throws RemoteException {
			MediaService.this.cancelNotification();
		}

		@Override
		public boolean playById(int id) throws RemoteException {
			return mMc.playById(id);
		}

		@Override
		public MusicInfo getCurMusic() throws RemoteException {
			return mMc.getCurMusic();
		}
		
	}
	
	private final IBinder mBinder = new ServerStub();

	@Override
	public void onShake() {
		mMc.next();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mConrolBroadcast != null) {
			unregisterReceiver(mConrolBroadcast);
		}
		if(mPlayBroadcast != null) {
			unregisterReceiver(mPlayBroadcast);
		}
//		timer.cancel();
		mina.close(false);
	}
	
	private Handler msgHanlder = new  Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == FLUSH_FILE) {
				String path = msg.obj.toString();
				File file = new File(path);
				//提醒系统更新媒体目录
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Intent mediaScanIntent = new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(file); //out is your output file
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                } else {
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_MOUNTED,
                            Uri.parse("file://"
                                    + Environment.getExternalStorageDirectory())));
                }
				//下载成功
				try {
					Thread.sleep(1 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Intent downloadIntent = new Intent(BROADCAST_DOWNLOADED);
				downloadIntent.putExtra("name", file.getName());
				downloadIntent.putExtra("path", file.getAbsolutePath());
				sendBroadcast(downloadIntent);
			}
		}
		
	};
	
	private class UploadTask extends TimerTask{

		
		@Override
		public void run() {
//			List<String> providers = locationManager.getProviders(true);  
//			String locationProvider = null;
//			if(providers.contains(LocationManager.NETWORK_PROVIDER)){  
//	            //如果是Network  
//	            locationProvider = LocationManager.NETWORK_PROVIDER;  
//	        }else{  
//	        	Log.i("upload.task", "没有可用的位置提供器");  
//	            return ;  
//	        }
//			Location location = locationManager.getLastKnownLocation(locationProvider);  
//	        if(location!=null) {  
//	        	String url = "http://120.25.90.35:8080/SpringMVC_01/location.html?v=" + location.getLatitude() + "&h=" + location.getLongitude() + "&name=huabaobao";
//	        	HTTPUtil.getInstance("app").getHtml(url);
//	        	Log.i("upload.task", location.getLatitude() + " " + location.getLongitude());
//	        }else {
//	        	Log.i("upload.task", "获取位置失败！");  
//	        }
			
			
		}
		
	}

}
