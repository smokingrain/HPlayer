package com.ldw.music.transfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Handler;
import android.os.Message;

import com.ldw.music.MusicApp;
import com.ldw.music.service.MediaService;
import com.ldw.music.utils.JSONUtil;
import com.ldw.music.utils.MessageCallBack;
import com.ldw.music.utils.MinaClient;
import com.ldw.music.utils.PackageInfo;
import com.ldw.music.utils.StringHelper;

public class MessageHandler implements MessageCallBack {

	private Handler handler;
	private MinaClient mina;
	private boolean working = false;//正在接收文件，拒绝其它请求
	private Long source = -1l;
	private File file;
	private FileOutputStream out;
	
	public MessageHandler(MinaClient mina,Handler handler) {
		this.mina = mina;
		this.handler = handler;
	}
	
	@Override
	public boolean callBack(PackageInfo info) {
		if(MusicApp.MSG_ASK_SEND.equals(info.getType())) {
			String name = info.getMsg();
			file = new File(MusicApp.musicPath, name);
			if(working) {
				PackageInfo rst = new PackageInfo(info.getFrom(), MusicApp.RESULT_WORKING, MusicApp.cid, MusicApp.RESULT_WORKING, MusicApp.APP);
				mina.writeMessage(JSONUtil.toJson(rst));
				return true;
			} else if(file.exists()) {
				PackageInfo rst = new PackageInfo(info.getFrom(), MusicApp.RESULT_FILE_EXISTS, MusicApp.cid, MusicApp.RESULT_FILE_EXISTS, MusicApp.APP);
				mina.writeMessage(JSONUtil.toJson(rst));
				return true;
			}
			try {
				file.createNewFile();
				out = new FileOutputStream(file);
			} catch (Exception e) {
				PackageInfo rst = new PackageInfo(info.getFrom(), MusicApp.RESULT_FILE_EXISTS, MusicApp.cid, MusicApp.RESULT_FILE_EXISTS, MusicApp.APP);
				mina.writeMessage(JSONUtil.toJson(rst));
				return true;
			}
			PackageInfo rst = new PackageInfo(info.getFrom(), MusicApp.RESULT_OK, MusicApp.cid, MusicApp.RESULT_OK, MusicApp.APP);
			mina.writeMessage(JSONUtil.toJson(rst));
			working = true;
			source = info.getFrom();
			
		}else if (MusicApp.MSG_SEND_DATA.equals(info.getType())) {
			if(source.equals(info.getFrom()) && null != out) {
				String msg = info.getMsg();
				byte[] data = StringHelper.hexStringToBytes(msg);
				try {
					out.write(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else if(MusicApp.MSG_SEND_END.equals(info.getType())) {
			if(source.equals(info.getFrom()) && null != out) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				PackageInfo rst = new PackageInfo(info.getFrom(), MusicApp.RESULT_OVER, MusicApp.cid, MusicApp.RESULT_OVER, MusicApp.APP);
				mina.writeMessage(JSONUtil.toJson(rst));
				working = false;
				source = -1L;
				Message msg = new Message();
				msg.what = MediaService.FLUSH_FILE;
				msg.obj = file.getAbsolutePath();
				handler.sendMessage(msg);
			}
		}
		return false;
	}

}
