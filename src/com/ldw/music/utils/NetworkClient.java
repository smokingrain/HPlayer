package com.ldw.music.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;



public class NetworkClient {
	private Socket sk;
	private DataInputStream din;
	private DataOutputStream dou;
	private String id;
	private MessageListener listener;
	private boolean isListening=false;
	private static NetworkClient instance=new NetworkClient();
	
	public static NetworkClient getInstance(String id){
		if(null==instance){
			instance=new NetworkClient(id);
		}
		if(null==instance.id){
			instance.id=id;
		}
		return instance;
	}
	public static NetworkClient getInstance(){
		if(null==instance){
			instance=new NetworkClient();
		}
		return instance;
	}
	
	private NetworkClient(){
		
	}
	
	private NetworkClient(String id){
		this.id=id;
	}
	
	public String connect(String addr,int port) {
		if(null==sk){
			try {
				sk=new Socket(addr,port);
				din=new DataInputStream(sk.getInputStream());
				dou=new DataOutputStream(sk.getOutputStream());
				if(null==id){
					dou.writeUTF("+");
					String inf=din.readUTF();
					PackageInfo info=JSONUtil.toBean(inf, PackageInfo.class);
					id=info.getMsg();
				}else{
					dou.writeUTF(id);
				}
				readMessage();
			} catch (UnknownHostException e) {
				return id;
			} catch (IOException e) {
				return id;
			}
		}
		return id;
		
	}
	
	public void writeMessage(String info){
		try {
			if(null!=dou){
				dou.writeUTF(info);
				dou.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readMessage(){
		if(isListening){
			return;
		}
		isListening=true;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while(isListening){
						if(din!=null){
							String info=din.readUTF();
							if(!isListening){
								break;
							}
							if(null!=listener){
								listener.getMessage(JSONUtil.toBean(info,PackageInfo.class));
							}
						}
					}
					isListening=false;
					if(null!=din){
						din.close();
					}
				} catch (IOException e) {
					NetworkClient.this.destory();
					System.out.println("������Ϣ����");
				}
			}
		}).start();
	}
	
	public void destory(){
		id=null;
		isListening=false;
		instance=null;
		try {
			if(null!=din){
				din.close();
				din=null;
			}
			if(null!=dou){
				dou.close();
				dou=null;
			}
			if(null!=sk){
				sk.close();
				sk=null;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MessageListener getListener() {
		return listener;
	}

	public void setListener(MessageListener listener) {
		this.listener = listener;
	}
}	
