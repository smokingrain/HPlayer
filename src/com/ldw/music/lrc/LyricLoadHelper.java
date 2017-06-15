/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.lrc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ldw.music.utils.FileUtils;
import com.ldw.music.utils.JSONUtil;

import android.annotation.SuppressLint;
import android.util.Log;


/**
 * 歌词的显示控制
 * @author xiaokui
 *
 */
public class LyricLoadHelper {
	/** 用于向外通知歌词载入、变化的监听器 */
	public interface LyricListener {

		/**
		 * 歌词载入时调用
		 * 
		 * @param lyricSentences
		 *            歌词文本处理后的所有歌词句子
		 * @param indexOfCurSentence
		 *            正在播放的句子在句子集合中的索引号
		 */
		public abstract void onLyricLoaded(List<XRCLine> lyricSentences,
				int indexOfCurSentence);

		/**
		 * 歌词变化时调用
		 * 
		 * @param indexOfCurSentence
		 *            正在播放的句子在句子集合中的索引号
		 * @param currentTime
		 *            已经播放的毫秒数
		 * */
		public abstract void onLyricSentenceChanged(int indexOfCurSentence);
	}

	private static final String TAG = LyricLoadHelper.class.getSimpleName();

	/** 句子集合 */
	private List<XRCLine> mLyricSentences = new ArrayList<XRCLine>();

	private List<LyricListener> mLyricListeners = new ArrayList<LyricListener>();
	
	private boolean mHasLyric = false;

	/** 当前正在播放的歌词句子的在句子集合中的索引号 */
	private int mIndexOfCurrentSentence = -1;

	/** 用于缓存的一个正则表达式对象,识别[]中的内容，不包括中括号 */
	private final Pattern mBracketPattern = Pattern
			.compile("(?<=\\[).*?(?=\\])");
	private final Pattern mTimePattern = Pattern
			.compile("(?<=\\[)(\\d{2}:\\d{2}\\.?\\d{0,3})(?=\\])");

	private final String mEncoding = "utf-8";

	public List<XRCLine> getLyricSentences() {
		return mLyricSentences;
	}

	public void setLyricListener(LyricListener listener) {
		if(null != listener) {
			mLyricListeners.add(listener);
			Log.i(TAG, "listeners size = " + mLyricListeners.size());
		}
	}

	public void setIndexOfCurrentSentence(int index) {
		mIndexOfCurrentSentence = index;
	}

	public int getIndexOfCurrentSentence() {
		return mIndexOfCurrentSentence;
	}

	/**
	 * 根据歌词文件的路径，读取出歌词文本并解析
	 * 
	 * @param lyricPath
	 *            歌词文件路径
	 * @return true表示存在歌词，false表示不存在歌词
	 */
	public boolean loadLyric(String lyricPath, long allLength) {
		Log.i(TAG, "LoadLyric begin,path is:" + lyricPath);
		mHasLyric = false;
		mLyricSentences.clear();
		if (lyricPath != null) {
			File file = new File(lyricPath);
			if (file.exists()) {
				Log.i(TAG, "歌词文件存在");
				mHasLyric = true;
				try {
					if(lyricPath.toLowerCase().endsWith(".lrc")) {
						LrcParser parser = new LrcParser(allLength);
						mLyricSentences = parser.parser(lyricPath);
					}else if (lyricPath.toLowerCase().endsWith(".krc")) {
						mLyricSentences = KrcText.fromKRC(lyricPath);
					}else if (lyricPath.toLowerCase().endsWith(".zlrc")) {
						String data=FileUtils.readString(lyricPath);
						mLyricSentences = JSONUtil.toBean(data, JSONUtil.getCollectionType(List.class, XRCLine.class));
					}
					

					// 按时间排序句子集合
					Collections.sort(mLyricSentences,
							new Comparator<XRCLine>() {
								// 内嵌，匿名的compare类
								public int compare(XRCLine object1,
										XRCLine object2) {
									if (object1.start > object2
											.start) {
										return 1;
									} else if (object1.start < object2
											.start) {
										return -1;
									} else {
										return 0;
									}
								}
							});
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
			} else {
				Log.i(TAG, "歌词文件不存在");
			}
		}
		// 如果有谁在监听，通知它歌词载入完啦，并把载入的句子集合也传递过去
		if (!mLyricListeners.isEmpty()) {
			for(LyricListener mLyricListener : mLyricListeners) {
				mLyricListener.onLyricLoaded(mLyricSentences,
						mIndexOfCurrentSentence);
			}
		}
		if (mHasLyric) {
			Log.i(TAG, "Lyric file existed.Lyric has " + mLyricSentences.size()
					+ " Sentences");
		} else {
			Log.i(TAG, "Lyric file does not existed");
		}
		return mHasLyric;
	}

	/**
	 * 根据传递过来的已播放的毫秒数，计算应当对应到句子集合中的哪一句，再通知监听者播放到的位置。
	 * 
	 * @param millisecond
	 *            已播放的毫秒数
	 */
	public void notifyTime(long millisecond) {
		// Log.i(TAG, "notifyTime");
		if (mHasLyric && mLyricSentences != null && mLyricSentences.size() != 0) {
			int newLyricIndex = seekSentenceIndex(millisecond);
			if (newLyricIndex != -1 && newLyricIndex != mIndexOfCurrentSentence) {// 如果找到的歌词和现在的不是一句。
				if (!mLyricListeners.isEmpty()) {
					for(LyricListener mLyricListener : mLyricListeners) {
						// 告诉一声，歌词已经变成另外一句啦！
						mLyricListener.onLyricSentenceChanged(newLyricIndex);
					}
				}
				mIndexOfCurrentSentence = newLyricIndex;
			}
		}
	}

	private int seekSentenceIndex(long millisecond) {
		int findStart = 0;
		if (mIndexOfCurrentSentence >= 0) {
			// 如果已经指定了歌词，则现在位置开始
			findStart = mIndexOfCurrentSentence;
		}

		try {
			long lyricTime = mLyricSentences.get(findStart).start;

			if (millisecond > lyricTime) { // 如果想要查找的时间在现在字幕的时间之后
				// 如果开始位置经是最后一句了，直接返回最后一句。
				if (findStart == (mLyricSentences.size() - 1)) {
					return findStart;
				}
				int new_index = findStart + 1;
				// 找到第一句开始时间大于输入时间的歌词
				while (new_index < mLyricSentences.size()
						&& mLyricSentences.get(new_index).start <= millisecond) {
					++new_index;
				}
				// 这句歌词的前一句就是我们要找的了。
				return new_index - 1;
			} else if (millisecond < lyricTime) { // 如果想要查找的时间在现在字幕的时间之前
				// 如果开始位置经是第一句了，直接返回第一句。
				if (findStart == 0)
					return 0;

				int new_index = findStart - 1;
				// 找到开始时间小于输入时间的歌词
				while (new_index > 0
						&& mLyricSentences.get(new_index).start > millisecond) {
					--new_index;
				}
				// 就是它了。
				return new_index;
			} else {
				// 不用找了
				return findStart;
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			Log.i(TAG, "新的歌词载入了，所以产生了越界错误，不用理会，返回0");
			return 0;
		}
	}


	/** 去除指定字符串中包含[XXX]形式的字符串 */
	private String trimBracket(String content) {
		String s = null;
		String result = content;
		Matcher matcher = mBracketPattern.matcher(content);
		while (matcher.find()) {
			s = matcher.group();
			result = result.replace("[" + s + "]", "");
		}
		return result;
	}

	/** 将歌词的时间字符串转化成毫秒数，如果参数是00:01:23.45 */
	@SuppressLint("DefaultLocale")
	private long parseTime(String strTime) {
		String beforeDot = new String("00:00:00");
		String afterDot = new String("0");

		// 将字符串按小数点拆分成整秒部分和小数部分。
		int dotIndex = strTime.indexOf(".");
		if (dotIndex < 0) {
			beforeDot = strTime;
		} else if (dotIndex == 0) {
			afterDot = strTime.substring(1);
		} else {
			beforeDot = strTime.substring(0, dotIndex);// 00:01:23
			afterDot = strTime.substring(dotIndex + 1); // 45
		}

		long intSeconds = 0;
		int counter = 0;
		while (beforeDot.length() > 0) {
			int colonPos = beforeDot.indexOf(":");
			try {
				if (colonPos > 0) {// 找到冒号了。
					intSeconds *= 60;
					intSeconds += Integer.valueOf(beforeDot.substring(0,
							colonPos));
					beforeDot = beforeDot.substring(colonPos + 1);
				} else if (colonPos < 0) {// 没找到，剩下都当一个数处理了。
					intSeconds *= 60;
					intSeconds += Integer.valueOf(beforeDot);
					beforeDot = "";
				} else {// 第一个就是冒号，不可能！
					return -1;
				}
			} catch (NumberFormatException e) {
				return -1;
			}
			++counter;
			if (counter > 3) {// 不会超过小时，分，秒吧。
				return -1;
			}
		}
		// intSeconds=83

		String totalTime = String.format("%d.%s", intSeconds, afterDot);// totaoTimer
		// =
		// "83.45"
		Double doubleSeconds = Double.valueOf(totalTime); // 转成小数83.45
		return (long) (doubleSeconds * 1000);// 转成毫秒8345
	}

}
