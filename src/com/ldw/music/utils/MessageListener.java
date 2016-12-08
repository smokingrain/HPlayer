package com.ldw.music.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class MessageListener {
	private List<MessageCallBack> callBacks=new CopyOnWriteArrayList<MessageCallBack>();
	public void registListener(MessageCallBack callBack){
		if(null!=callBack){
			callBacks.add(callBack);
		}
	}
	public void unregistListener(MessageCallBack callBack){
		if(null!=callBack){
			callBacks.remove(callBack);
		}
	}
	/**
	 * @param message
	 * @return 是否所有人都收到消息
	 */
	public boolean getMessage(final PackageInfo message){
		for(MessageCallBack callback:callBacks){
			if(callback.callBack(message)){
				return false;
			}
		}
		return true;
	}
}
