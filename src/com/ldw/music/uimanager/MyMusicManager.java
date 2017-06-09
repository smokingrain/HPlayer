package com.ldw.music.uimanager;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ldw.music.MusicApp;
import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.adapter.MusicAdapter;
import com.ldw.music.lib.SwipeMenu;
import com.ldw.music.lib.SwipeMenuCreator;
import com.ldw.music.lib.SwipeMenuItem;
import com.ldw.music.lib.SwipeMenuListView;
import com.ldw.music.lib.SwipeMenuListView.OnMenuItemClickListener;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.ServiceManager;
import com.ldw.music.storage.SPStorage;
import com.ldw.music.utils.MediaScanner;
import com.ldw.music.utils.MusicTimer;
import com.ldw.music.utils.MusicUtils;

/**
 * 我的音乐
 * @author xiaokui
 *
 */
public class MyMusicManager extends MainUIManager implements IConstants,
		OnTouchListener {

	private LayoutInflater mInflater;
	private Activity mActivity;

	private String TAG = MyMusicManager.class.getSimpleName();
	private MusicAdapter mAdapter;
	private SwipeMenuListView mListView;
	private ServiceManager mServiceManager = null;
	private SlidingDrawerManager mSdm;
	private MyMusicUIManager mUIm;
	private MusicTimer mMusicTimer;
	private MusicPlayBroadcast mPlayBroadcast;

	private Object mObj;
	private int from;

	private RelativeLayout mBottomLayout, mMainLayout;
	private Bitmap defaultArtwork;

	private UIManager mUIManager;
	
	private int mScreenWidth;

	public MyMusicManager(Activity activity, UIManager manager) {
		this.mActivity = activity;
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScreenWidth = metric.widthPixels;
		mInflater = LayoutInflater.from(activity);
		this.mUIManager = manager;
	}

	public View getView(int from) {
		return getView(from, null);
	}

	public View getView(int from, Object object) {
		View contentView = mInflater.inflate(R.layout.mymusic, null);
		mObj = object;
		this.from = from;
		initBg(contentView);
		initView(contentView);

		return contentView;
	}

	private void initView(View view) {
		defaultArtwork = BitmapFactory.decodeResource(mActivity.getResources(),
				R.drawable.img_album_background);
		mServiceManager = MusicApp.mServiceManager;

		mBottomLayout = (RelativeLayout) view.findViewById(R.id.bottomLayout);

		mListView = (SwipeMenuListView) view.findViewById(R.id.music_listview);

		mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mPlayBroadcast = new MusicPlayBroadcast();
		IntentFilter filter = new IntentFilter(BROADCAST_NAME);
		filter.addAction(BROADCAST_NAME);
		filter.addAction(BROADCAST_QUERY_COMPLETE_NAME);
		mActivity.registerReceiver(mPlayBroadcast, filter);

		mUIm = new MyMusicUIManager(mActivity, mServiceManager, view,
				mUIManager);
		mSdm = new SlidingDrawerManager(mActivity, mServiceManager, view);
		mMusicTimer = new MusicTimer(mSdm.mHandler, mUIm.mHandler);
		mSdm.setMusicTimer(mMusicTimer);

		initListView();

		initListViewStatus();
	}

	private void initBg(View view) {
		mMainLayout = (RelativeLayout) view
				.findViewById(R.id.main_mymusic_layout);
		mMainLayout.setOnTouchListener(this);
		SPStorage mSp = new SPStorage(mActivity);
		String mDefaultBgPath = mSp.getPath();
		Bitmap bitmap = mUIManager.getBitmapByPath(mDefaultBgPath);
		if (bitmap != null) {
			mMainLayout.setBackgroundDrawable(new BitmapDrawable(mActivity
					.getResources(), bitmap));
		} else {
			mMainLayout.setBackgroundResource(R.drawable.bg);
		}
	}

	private void initListViewStatus() {
		try {
			mSdm.setListViewAdapter(mAdapter);
			int playState = mServiceManager.getPlayState();
			if (playState == MPS_NOFILE || playState == MPS_INVALID) {
				return;
			}
			if (playState == MPS_PLAYING) {
				mMusicTimer.startTimer();
			}
			List<MusicInfo> musicList = mAdapter.getData();
			int playingSongPosition = MusicUtils.seekPosInListById(musicList,
					mServiceManager.getCurMusicId());
			mAdapter.setPlayState(playState, playingSongPosition);
			MusicInfo music = mServiceManager.getCurMusic();
			mSdm.refreshUI(mServiceManager.position(), music.duration, music);
			mSdm.showPlay(false);
			mUIm.refreshUI(mServiceManager.position(), music.duration, music);
			mUIm.showPlay(false);

		} catch (Exception e) {
			Log.d(TAG, "", e);
		}
	}

	private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
        		mActivity.getResources().getDisplayMetrics());
    }
	
	private void initListView() {
		mAdapter = new MusicAdapter(mActivity, mServiceManager, mSdm);
		mListView.setAdapter(mAdapter);

		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				MusicInfo info = mAdapter.getData().get(position);
				showDeleteDialog(info);
				return true;
			}
		});
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mAdapter.refreshPlayingList();
				mServiceManager
						.playById(mAdapter.getData().get(position).songId);
			}
		});
		Log.i("com.xk.hplayer", "init music view");
		mAdapter.setData(MusicUtils.queryMusic(mActivity, from));
	}

	private void showDeleteDialog(final MusicInfo minfo) {
		
		View view = View.inflate(this.mActivity, R.layout.delete_music_comfirm, null);
		final Dialog dialog = new Dialog(this.mActivity, R.style.lrc_dialog);
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);

		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER);
		// lp.x = 100; // 新位置X坐标
		// lp.y = 100; // 新位置Y坐标
		lp.width = (int) (mScreenWidth * 0.7); // 宽度
		// lp.height = 400; // 高度

		// 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
		// dialog.onWindowAttributesChanged(lp);
		dialogWindow.setAttributes(lp);

		dialog.show();

		final Button cancleBtn = (Button) view.findViewById(R.id.cancle_btn);
		final Button okBtn = (Button) view.findViewById(R.id.ok_btn);
		final CheckBox chk = (CheckBox) view.findViewById(R.id.del_check);
		chk.setChecked(false);
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == cancleBtn) {
					dialog.dismiss();
				} else if (v == okBtn) {
					MusicInfo cur = mServiceManager.getCurMusic();
					if(null != cur && cur.songId == minfo.songId) {
						if(mServiceManager.getPlayState() >= MPS_PREPARE) {
							mServiceManager.pause();
						}
					}
					
					MusicUtils.deleteMusic(minfo);
					String path = minfo.data;
					File file = new File(path);
					String name = file.getName();
					if(chk.isChecked()) {
						file.delete();
						MediaScanner scanner = MediaScanner.getInstanc(mActivity);
						scanner.scanFile(path, null);
					}
					Intent downloadIntent = new Intent(BROADCAST_MUSIC_DELETE);
					downloadIntent.putExtra("name", name);
					downloadIntent.putExtra("path", file.getAbsolutePath());
					mActivity.sendBroadcast(downloadIntent);
					dialog.dismiss();
				}
			}
		};
		cancleBtn.setOnClickListener(listener);
		okBtn.setOnClickListener(listener);
		
	}
	
	private class MusicPlayBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(BROADCAST_QUERY_COMPLETE_NAME.equals(intent.getAction())) {
				mAdapter.setData(MusicUtils.queryMusic(mActivity, from));
				mAdapter.refreshPlayingList();
				return ;
			}
			
			if (intent.getAction().equals(BROADCAST_NAME)) {
				MusicInfo music = new MusicInfo();
				int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
				int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);
				Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
				if (bundle != null) {
					music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
				}
				mAdapter.setPlayState(playState, curPlayIndex);
				switch (playState) {
				case MPS_INVALID:// 考虑后面加上如果文件不可播放直接跳到下一首
					mMusicTimer.stopTimer();
					mSdm.refreshUI(0, music.duration, music);
					mSdm.showPlay(true);

					mUIm.refreshUI(0, music.duration, music);
					mUIm.showPlay(true);
					mServiceManager.next();
					break;
				case MPS_PAUSE:
					mMusicTimer.stopTimer();
					mSdm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mSdm.showPlay(true);

					mUIm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mUIm.showPlay(true);

					mServiceManager.cancelNotification();
					break;
				case MPS_PLAYING:
					mMusicTimer.startTimer();
					mSdm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mSdm.showPlay(false);

					mUIm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mUIm.showPlay(false);

					Bitmap bitmap = MusicUtils.getCachedArtwork(mActivity,
							music.albumId, defaultArtwork);
					// Bitmap bitmap = MusicUtils.getArtwork(getActivity(),
					// music._id, music.albumId);
					// 更新顶部notification
					mServiceManager.updateNotification(bitmap, music.musicName,
							music.artist);

					break;
				case MPS_PREPARE:
					mMusicTimer.stopTimer();
					mSdm.refreshUI(0, music.duration, music);
					mSdm.showPlay(true);

					mUIm.refreshUI(0, music.duration, music);
					mUIm.showPlay(true);

					// 读取歌词文件
					mSdm.loadLyric(music);
					break;
				}
			}
		}
	}

	int oldY = 0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int bottomTop = mBottomLayout.getTop();
		System.out.println(bottomTop);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			oldY = (int) event.getY();
			if (oldY > bottomTop) {
				mSdm.open();
			}
		}
		return true;
	}

	@Override
	protected void setBgByPath(String path) {
		Bitmap bitmap = mUIManager.getBitmapByPath(path);
		if (bitmap != null) {
			mMainLayout.setBackgroundDrawable(new BitmapDrawable(mActivity
					.getResources(), bitmap));
		}
	}

	@Override
	public View getView() {
		return null;
	}
	
	@Override
	public void reflushView(int from) {
		// TODO Auto-generated method stub
		
	}

}
