package com.ldw.music.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class MediaScanner {

    private MediaScannerConnection mediaScanConn = null;
    private MediaSannerClient client = null;
    private String filePath = null;
    private String fileType = null;
    private static MediaScanner mediaScanner= null;

    /**
     * 然后调用MediaScanner.scanFile("/sdcard/2.mp3");
     * */

    public MediaScanner(Context context) {
        // 创建MusicSannerClient
        if (client == null) {
            client = new MediaSannerClient();
        }
        if (mediaScanConn == null) {
            mediaScanConn = new MediaScannerConnection(context, client);
        }
    }
    
    public static MediaScanner getInstanc(Context context){
        if (mediaScanner==null){
            mediaScanner = new MediaScanner(context);
        }
        return mediaScanner;
    }

    private class MediaSannerClient implements
        MediaScannerConnection.MediaScannerConnectionClient {

        public void onMediaScannerConnected() {

            if (filePath != null) {
                mediaScanConn.scanFile(filePath, fileType);
            }

            filePath = null;
            fileType = null;
        }

        public void onScanCompleted(String path, Uri uri) {
            // TODO Auto-generated method stub
            mediaScanConn.disconnect();
        }

    }

    /**
     * 扫描文件标签信息
     * 
     * @param filePath
     *            文件路径 eg:/sdcard/MediaPlayer/dahai.mp3
     * @param fileType
     *            文件类型 eg: audio/mp3 media/* application/ogg
     * */

    public void scanFile(String filepath, String fileType) {
        this.filePath = filepath;
        this.fileType = fileType;
        // 连接之后调用MusicSannerClient的onMediaScannerConnected()方法
        mediaScanConn.connect();
    }

}