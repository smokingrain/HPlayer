package com.ldw.music.activity;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.StringUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ldw.music.R;
import com.ldw.music.service.DownloadService;
import com.ldw.music.storage.SPStorage;
import com.ldw.music.utils.IDownloadSource;
import com.ldw.music.utils.SongSeacher;
import com.ldw.music.utils.SourceFactory;
import com.ldw.music.utils.SongSeacher.SearchInfo;

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

	private SearchHandler handler ;
	private boolean searching = false;
	
	private int mScreenWidth;
	
	private SPStorage mSp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.music_search);


		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScreenWidth = metric.widthPixels;
		
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		handler = new SearchHandler(this);
		mSp = new SPStorage(this);
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
	
	/**
	 * 执行搜索
	 */
	private void searchSong() {
		final String toSearch = mSearchInputEt.getText().toString();
		if(!StringUtil.isBlank(toSearch) && !searching) {
			Thread searchThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					searching = true;
					IDownloadSource source = SourceFactory.getSource(mSp.getDataSource());
					List<SearchInfo> result = source.getSong(toSearch);
					Message msg = new Message();
					msg.obj = result;
					handler.sendMessage(msg);
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
	 * 没连接wifi的时候提示
	 * @param info
	 */
	private void showWifiDialog(final SearchInfo info) {
		View view = View.inflate(this, R.layout.confirm_download, null);
		view.setMinimumWidth(mScreenWidth - 40);
		final Dialog dialog = new Dialog(this, R.style.lrc_dialog);

		final Button okBtn = (Button) view.findViewById(R.id.download_btn);
		final Button cancleBtn = (Button) view.findViewById(R.id.nodown_btn);
		
		OnClickListener btnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == okBtn) {
					DownloadService.addDownloadTask(MusicListSearchActivity.this, info);
					Toast.makeText(getApplicationContext(), "已加入下载列表", Toast.LENGTH_SHORT).show();
				}
				dialog.dismiss();
			}
		};
		okBtn.setOnClickListener(btnListener);
		cancleBtn.setOnClickListener(btnListener);
		dialog.setContentView(view);
		dialog.show();
	}
	
	/**
	 * 检查是否连接了wifi
	 * @param context
	 * @return
	 */
	public boolean isWifiConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo.isConnected()) {
			return true;
		}

		return false;
	}
	
	
	/**
	 * 下载歌曲。
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		SearchInfo info = mAdapter.getData().get(position);
		if(isWifiConnected(this)) {
			DownloadService.addDownloadTask(getApplicationContext(), info);
			Toast.makeText(getApplicationContext(), "已加入下载列表", Toast.LENGTH_SHORT).show();
		}else {
			showWifiDialog(info);
		}
		
	}
	
	/**
	 * 搜索回调
	 * @author o-kui.xiao
	 *
	 */
	private static class SearchHandler extends Handler {

		private MusicListSearchActivity thiz ;
		
		public SearchHandler(MusicListSearchActivity thiz) {
			 this.thiz = thiz;
		}
		
		@Override
		public void handleMessage(Message msg) {
			List<SearchInfo> results = (List<SearchInfo>) msg.obj;
			thiz.mAdapter.setData(results);
		}
		
	}
	
}
