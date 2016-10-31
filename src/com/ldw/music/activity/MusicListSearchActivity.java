package com.ldw.music.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.helper.StringUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ldw.music.R;
import com.ldw.music.aidl.IMediaService;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.DownloadService;
import com.ldw.music.utils.MusicUtils;
import com.ldw.music.utils.SongSeacher;
import com.ldw.music.utils.SongSeacher.SearchInfo;
import com.ldw.music.utils.StringHelper;
import com.ldw.music.view.KeyBoardKeyView;

/**
 * 歌曲搜索界面
 * @author xiaokui
 *
 */
public class MusicListSearchActivity extends Activity implements
		OnClickListener, IConstants, OnItemClickListener {

	private ImageView mKeyboardSwitcherIv;
	private EditText mSearchInputEt;
	private InputMethodManager mInputMethodManager;

	private ListView mListView;
	protected int mPlayingSongPosition;
	private SearchAdapter mAdapter;

	private MusicPlayBroadcast mPlayBroadcast;
	private SearchHandler handler ;
	private boolean searching = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.music_search);

		mPlayBroadcast = new MusicPlayBroadcast();
		IntentFilter filter = new IntentFilter(BROADCAST_NAME);
		registerReceiver(mPlayBroadcast, filter);

		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		handler = new SearchHandler(this);
		initView();
		initListView();
	}


	protected void initListView() {
		mAdapter = new SearchAdapter(new ArrayList<SearchInfo>());
		mListView.setAdapter(mAdapter);
	}

	private void initView() {

		mListView = (ListView) findViewById(R.id.listview);
		mKeyboardSwitcherIv = (ImageView) findViewById(R.id.keyboard_switcher);
		mSearchInputEt = (EditText) findViewById(R.id.search_input);
		mKeyboardSwitcherIv.setOnClickListener(this);
		initInput();
		mListView.setOnItemClickListener(this);

	}

	private class MusicPlayBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_NAME)) {
				int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
				int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);
			}
		}
	}

	private class SearchAdapter extends BaseAdapter {

		private List<SearchInfo> musicList = new ArrayList<SearchInfo>();

		public SearchAdapter(List<SearchInfo> mMusicList) {
			musicList.addAll(mMusicList);
		}

		@Override
		public int getCount() {
			return musicList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}


		public void setData(List<SearchInfo> list) {
			musicList.clear();
			if (list != null) {
				musicList.addAll(list);
				notifyDataSetChanged();
			}
		}

		public List<SearchInfo> getData() {
			return musicList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			SearchInfo music = musicList.get(position);

			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.searchlist_item, null);
				viewHolder.titleTv = (TextView) convertView
						.findViewById(R.id.item_musicname_tv);
				viewHolder.artistTv = (TextView) convertView
						.findViewById(R.id.item_artist_tv);
				viewHolder.playStateIv = (ImageView) convertView
						.findViewById(R.id.item_playstate_iv);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.playStateIv.setVisibility(View.GONE);
			viewHolder.titleTv.setText((position + 1) + "." + music.name);
			viewHolder.artistTv.setText(music.singer);
			return convertView;
		}

		class ViewHolder {
			TextView titleTv, artistTv;
			ImageView playStateIv;
		}

	}

	private void initInput() {
		mKeyboardSwitcherIv
				.setImageResource(R.drawable.icon_search);
		mSearchInputEt.setHint("请输入简拼或全拼");
		// 显示输入法
		mInputMethodManager.showSoftInput(mSearchInputEt, 0);
		// 搜索输入框允许输入法输入
		mSearchInputEt.setInputType(InputType.TYPE_CLASS_TEXT);
	}
	
	private void searchSong() {
		final String toSearch = mSearchInputEt.getText().toString();
		if(!StringUtil.isBlank(toSearch) && !searching) {
			Toast.makeText(this, "start search!!!", Toast.LENGTH_SHORT).show();
			Thread searchThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					Log.i("com.xk.hplayer", "try search!");
					searching = true;
					List<SearchInfo> result = SongSeacher.getSongFromKuwo(toSearch);
					Log.i("com.xk.hplayer", "search result!!");
					Message msg = new Message();
					msg.obj = result;
					handler.sendMessage(msg);
					Log.i("com.xk.hplayer", "sending!!");
					searching = false;
				}
			});
			searchThread.start();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.keyboard_switcher:
			searchSong();
			break;
		default:break;
		}
	}




	
	/**
	 * 下载歌曲。
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		SearchInfo info = mAdapter.getData().get(position);
		DownloadService.addDownloadTask(getApplicationContext(), info);
		Toast.makeText(getApplicationContext(), "已加入下载列表", Toast.LENGTH_SHORT).show();
		finish();
	}
	
	private static class SearchHandler extends Handler {

		private MusicListSearchActivity thiz ;
		
		public SearchHandler(MusicListSearchActivity thiz) {
			 this.thiz = thiz;
		}
		
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(thiz, "start reflush!!", Toast.LENGTH_SHORT).show();
			List<SearchInfo> results = (List<SearchInfo>) msg.obj;
			thiz.mAdapter.setData(results);
		}
		
	}
	
}
