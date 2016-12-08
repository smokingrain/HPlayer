package com.ldw.music.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.ldw.music.MusicApp;
import com.ldw.music.activity.IConstants;
import com.ldw.music.db.DatabaseHelper;
import com.ldw.music.lrc.LrcInfo;
import com.ldw.music.utils.FileUtils;
import com.ldw.music.utils.HTTPUtil;
import com.ldw.music.utils.MusicUtils;
import com.ldw.music.utils.SongLocation;
import com.ldw.music.utils.SongSeacher;
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
import android.widget.Toast;

public class DownloadService extends Service implements IConstants{

	public static final String DOWNLOAD_MUSIC = "DOWNLOAD_MUSIC";
	private LinkedBlockingQueue<SearchInfo> queue = new LinkedBlockingQueue<SearchInfo>();
	
	private boolean running = true;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SearchInfo info = (SearchInfo) intent.getSerializableExtra("info");
		if(null != info) {
			try {
				queue.put(info);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
						SearchInfo info = queue.take();
						if(null != info) {
							String parent=MusicApp.musicPath;
							String lrcParent=MusicApp.lrcPath;
							File file=new File(parent,info.singer+" - "+info.name +"."+ info.type);
							File lrcFile=new File(lrcParent,info.singer+" - "+info.name + ".lrc");
							if(!file.exists()){
								String url=info.url;
								String lrcUrl = info.lrcURL;
								String realUrl=HTTPUtil.getInstance("player").getHtml(url);
								SongLocation loc=HTTPUtil.getInstance("player").getInputStream(realUrl);
								File temp = new File(parent,"temp_"+System.currentTimeMillis() + info.type);
								String html = HTTPUtil.getInstance("player").getHtml(lrcUrl);
								LrcInfo lrcs = SongSeacher.perseFromHTML(html);
								try {
									saveToFile(temp, file, loc.input);
									saveLrc(lrcFile, lrcs);
									//提醒系统更新媒体目录
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
									//下载成功
									Thread.sleep(1 * 1000);
									Intent downloadIntent = new Intent(BROADCAST_DOWNLOADED);
									downloadIntent.putExtra("name", info.name);
									downloadIntent.putExtra("path", file.getAbsolutePath());
									sendBroadcast(downloadIntent);
								} catch (Exception e) {
									//下载失败
									System.out.println("download failed!"+e.getMessage());
									Intent downloadIntent = new Intent(BROADCAST_DOWNLOAD_FAILED);
									downloadIntent.putExtra("name", info.name);
									sendBroadcast(downloadIntent);
								}finally{
									temp.delete();
									try {
										loc.input.close();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}else{
								Intent downloadIntent = new Intent(BROADCAST_DOWNLOADED_FILEEXISTS);
								downloadIntent.putExtra("name", info.name);
								downloadIntent.putExtra("path", file.getAbsolutePath());
								sendBroadcast(downloadIntent);
							}
							Log.i("com.xk.hplayer", info.name + " 下载完成");
						}
						Log.i("com.xk.hplayer", "looping!!!!");
					} catch (Exception e) {
						e.printStackTrace();
						Log.e("com.xk.hplayer", "下载队列出错！！" + e.getMessage());
					}
				}
				
			}
		});
    	t.start();
    }
    
    /**
     * 保存歌词
     * @param lrc
     * @param lrcs
     */
    public static void saveLrc(File lrc, LrcInfo lrcs) {
    	Map<Long ,String>ls=lrcs.getInfos();
		StringBuffer sb=new StringBuffer();
		for(Long time:ls.keySet()){
			String text=ls.get(time);
			long mil=(time%1000);
			long sec=time/1000;
			long mun=sec/60;
			long second=sec%60;
			sb.append("[").append(format(mun))
			.append(":").append(format(second)).append(".")
			.append(format(mil)).append("]").append(text).append("\r\n");
		}
		FileUtils.writeString(sb.toString(), lrc);
    }
    
    /**
     * 格式化时间
     * @param time
     * @return
     */
    private static String format(long time){
		if(time<10){
			return "0"+time;
		}
		if(time>99){
			return time/10+"";
		}
		return time+"";
	}
    
    /**
     * 先将歌曲用临时文件存起来，然后重命名。防止下载一半就失败了，留下垃圾文件
     * @param temp
     * @param file
     * @param loc
     * @throws IOException
     */
    private void saveToFile(File temp,File file, InputStream loc) throws IOException{
    	FileOutputStream out = null;
    	try {
			temp.createNewFile();
			out = new FileOutputStream(temp);
			byte[]buf = new byte[20480];
			int len = 0;
			while((len=loc.read(buf, 0, buf.length))>=0){
				out.write(buf, 0, len);
				out.flush();
			}
			temp.renameTo(file);
		} catch (IOException e) {
			throw e;
		}finally {
			if(null != out) {
				out.close();
			}
		}
		
    }
    
    
    /**
     * 添加到下载队列
     * @param context
     * @param info
     */
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
