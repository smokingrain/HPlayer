package com.ldw.music.utils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.ldw.music.MusicApp;


public class MinaClient {
	private IoSession session;
	private MessageListener listener;
	private ConnectionListener cListener;
	private IoConnector connector;
	private MinaClient(){}
	
	public boolean init(String host,int port){
		if(null==connector){
			connector = new NioSocketConnector();
			//设置链接超时时间
			connector.setConnectTimeoutMillis(5000);
			connector.getFilterChain().addLast("codec",new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
			connector.setHandler(new MessageHandler());
			try{
				ConnectFuture future = connector.connect(new InetSocketAddress(host,port));
				future.awaitUninterruptibly();// 等待连接创建完成
				session = future.getSession();//获得session
				System.out.println("created connection!");
				return true;
			}catch (Exception e){
				close(true);
			}
		}
		return false;
	}
	
	public void setListener(MessageListener listener) {
		this.listener = listener;
	}
	
	public void setcListener(ConnectionListener cListener) {
		this.cListener = cListener;
	}
	
	public boolean writeMessage(final String msg){
		if(null==session){
			return false;
		}
		WriteFuture future=session.write(msg);
		return future.isWritten();
	}
	
	
	public void close(boolean notify){
		if(null!=listener&&notify){
			PackageInfo info=new PackageInfo(MusicApp.cid, "disconnect", MusicApp.SERVER, MusicApp.MSG_DISCONNECT, MusicApp.APP);
			listener.getMessage(info);
			System.out.println("send close msg!");
		}
		if(null!=connector){
			connector.dispose();
		}
		session=null;
		connector=null;
	}
	
	public static MinaClient getInstance(){
		return MinaFactory.INSTANCE;
	}
	
	private class MessageHandler extends IoHandlerAdapter  {

		@Override
		public void exceptionCaught(IoSession session, Throwable cause)
				throws Exception {
			session.closeNow();
		}

		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {
			PackageInfo info=JSONUtil.toBean(message.toString(),PackageInfo.class);
			if("LOGIN".equals(info.getType())){
				cListener.connected(Long.parseLong(info.getMsg()));
				return;
			}
			listener.getMessage(info);
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			close(true);
		}

		@Override
		public void sessionOpened(IoSession session) throws Exception {
			System.out.println("conection conecting!");
		}
		
	}
	
	private static class MinaFactory{
		public static final MinaClient INSTANCE=new MinaClient();
	}
	
	
}
