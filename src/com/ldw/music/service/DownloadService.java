package com.ldw.music.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import com.ldw.music.MusicApp;
import com.ldw.music.utils.HTTPUtil;
import com.ldw.music.utils.SongLocation;
import com.ldw.music.utils.SongSeacher.SearchInfo;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class DownloadService extends Service {

	public static final String DOWNLOAD_MUSIC = "DOWNLOAD_MUSIC";
	private LinkedList<SearchInfo> queue = new LinkedList<SearchInfo>();
	
	private boolean running = true;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SearchInfo info = (SearchInfo) intent.getSerializableExtra("info");
		if(null != info) {
			queue.add(info);
		}
		return super.onStartCommand(intent, flags, startId);
	}


	@Override
    public void onCreate()
    {
        super.onCreate();
        doTasks();
        Log.i("com.xk.hplayer","onCreate");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        running = false;
        Log.i("com.xk.hplayer","onDestroy");
    }
    
    private void doTasks() {
    	Thread t = new Thread(new Runnable() {
			

			@Override
			public void run() {
				while(running) {
					try {
						SearchInfo info = queue.poll();
						int persent = 0;
						if(null != info) {
							String url=info.url;
							String realUrl=HTTPUtil.getInstance("player").getHtml(url);
							SongLocation loc=HTTPUtil.getInstance("player").getInputStream(realUrl);
							String parent=MusicApp.musicPath;
							File file=new File(parent,info.singer+" - "+info.name+"."+info.type);
							if(!file.exists()){
								File temp = new File(parent,"temp_"+System.currentTimeMillis()+".ape");
								FileOutputStream out=null;
								try {
									temp.createNewFile();
									out=new FileOutputStream(temp);
									long all=0;
									byte[]buf=new byte[20480];
									int len=0;
									while((len=loc.input.read(buf, 0, buf.length))>=0){
										all+=len;
										double per=(double)all/loc.length*100;
										if(per-persent>1||per>=100){
											persent=(int) (per);
										}
										out.write(buf, 0, len);
										out.flush();
									}
									temp.renameTo(file);
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
								} catch (Exception e) {
									System.out.println("download failed!"+e.getMessage());
								}finally{
									if(null!=out){
										try {
											out.close();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							}
							try {
								loc.input.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Log.i("com.xk.hplayer", info.name + " 下载完成");
						}
					} catch (Exception e) {
						Log.e("com.xk.hplayer", "下载队列出错！！" + e.getMessage());
					}
				}
				
			}
		});
    	t.start();
    }
    
    public static void addDownloadTask(Context context, SearchInfo info) {
    	Intent intent = new Intent(context,DownloadService.class);
    	intent.setAction(DOWNLOAD_MUSIC);
    	intent.putExtra("info", info);
    	context.startService(intent);
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
