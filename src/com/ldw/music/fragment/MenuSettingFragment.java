package com.ldw.music.fragment;

import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.activity.MenuSettingActivity;
import com.ldw.music.storage.SPStorage;
import com.ldw.music.view.WheelView;

/**
 * 
 * @author xiaokui
 *
 */
public class MenuSettingFragment extends Fragment implements OnClickListener, IConstants {
	
	private LinearLayout mAdviceLayout, mAboutLayout;
	private CheckedTextView mChangeSongTv, mAutoLyricTv, mFilterSizeTv, mFilterTimeTv, mDesktopLrcTv;
	private TextView mDataSource;
	private SPStorage mSp;
	
	private ImageButton mBackBtn;
	
	private List<String> dataSources = Arrays.asList(new String[]{"kugou","kuwo"});
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.menu_setting_fragment, container, false);
		
		mSp = new SPStorage(getActivity());
		
		initView(view);
		
		return view;
	}

	private void initView(View view) {
		mAboutLayout = (LinearLayout) view.findViewById(R.id.setting_about_layout);
		mAdviceLayout = (LinearLayout) view.findViewById(R.id.setting_advice_layout);
		mAboutLayout.setOnClickListener(this);
		mAdviceLayout.setOnClickListener(this);
		
		mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
		mBackBtn.setOnClickListener(this);
		
		mChangeSongTv = (CheckedTextView) view.findViewById(R.id.shake_change_song);
		mAutoLyricTv = (CheckedTextView) view.findViewById(R.id.auto_download_lyric);
		mFilterSizeTv = (CheckedTextView) view.findViewById(R.id.filter_size);
		mFilterTimeTv = (CheckedTextView) view.findViewById(R.id.filter_time);
		mDesktopLrcTv = (CheckedTextView) view.findViewById(R.id.open_desktoplrc);
		
		mDataSource = (TextView) view.findViewById(R.id.data_source_selector);
		
		mChangeSongTv.setChecked(mSp.getShake());
		mAutoLyricTv.setChecked(mSp.getAutoLyric());
		mFilterSizeTv.setChecked(mSp.getFilterSize());
		mFilterTimeTv.setChecked(mSp.getFilterTime());
		mDesktopLrcTv.setChecked(mSp.getOpenDesktopLrc());
		
		mDataSource.setText(mSp.getDataSource());
		
		mChangeSongTv.setOnClickListener(this);
		mAutoLyricTv.setOnClickListener(this);
		mFilterSizeTv.setOnClickListener(this);
		mFilterTimeTv.setOnClickListener(this);
		mDesktopLrcTv.setOnClickListener(this);
		
		mDataSource.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.setting_about_layout:
			break;
		case R.id.setting_advice_layout:
			break;
		case R.id.shake_change_song:
			mChangeSongTv.toggle();
			mSp.saveShake(mChangeSongTv.isChecked());
			Intent intent = new Intent(BROADCAST_SHAKE);
			intent.putExtra(SHAKE_ON_OFF, mChangeSongTv.isChecked());
			getActivity().sendBroadcast(intent);
			break;
		case R.id.auto_download_lyric:
			mAutoLyricTv.toggle();
			mSp.saveAutoLyric(mAutoLyricTv.isChecked());
			break;
		case R.id.filter_size:
			mFilterSizeTv.toggle();
			mSp.saveFilterSize(mFilterSizeTv.isChecked());
			break;
		case R.id.filter_time:
			mFilterTimeTv.toggle();
			mSp.saveFilterTime(mFilterTimeTv.isChecked());
			break;
		case R.id.open_desktoplrc:
			mDesktopLrcTv.toggle();
			mSp.saveDesktopLrc(mDesktopLrcTv.isChecked());
			break;
		case R.id.data_source_selector:
			String data = mSp.getDataSource();
			View outerView = View.inflate(this.getActivity(), R.layout.datasource_selector, null);
			WheelView wv = (WheelView) outerView.findViewById(R.id.wheel_view_wv);
			wv.setOffset(2);
            wv.setItems(dataSources);
            wv.setSeletion(dataSources.indexOf(data));
            wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(int selectedIndex, String item) {
                	mDataSource.setText(item);
                	mSp.setDataSource(item);
                }
            });
            final Dialog dialog = new Dialog(this.getActivity(), R.style.lrc_dialog);
            dialog.setContentView(outerView);
            dialog.setCanceledOnTouchOutside(true);
    		dialog.show();
			break;
		case R.id.backBtn:
			((MenuSettingActivity)getActivity()).mViewPager.setCurrentItem(0, true);
			break;
		}
	}
}
