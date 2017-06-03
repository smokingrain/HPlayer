package com.ldw.music.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ldw.music.MusicApp;
import com.ldw.music.lrc.XRCLine;
import com.ldw.music.lrc.XRCNode;
import com.ldw.music.utils.MusicTimer;

import android.content.Context;  
import android.graphics.Canvas;  
import android.graphics.Color;  
import android.graphics.LinearGradient;  
import android.graphics.Paint;  
import android.graphics.Shader;  
import android.graphics.Shader.TileMode;  
import android.graphics.Typeface;  
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;  
import android.view.View;
import android.view.WindowManager;  
import android.widget.TextView;

public class DesktopLyricView extends TextView {
	private final String TAG = DesktopLyricView.class.getSimpleName();  
    public static int TOOL_BAR_HIGH = 0;  
    public static WindowManager.LayoutParams params = new WindowManager.LayoutParams();  
    private float startX;  
    private float startY;  
    private float x;  
    private float y;  
    private String text = "hello 花宝宝";
    private float float1 = 0.0f;  
    private float float2 = 0.01f; 
    private Paint p = new Paint();
    private List<XRCLine> sentences = new ArrayList<XRCLine>();
    private List<Long> times = new ArrayList<>();
    private Handler handler;
    private MusicTimer timer;
     
    WindowManager wm = (WindowManager) getContext().getApplicationContext()  
            .getSystemService(Context.WINDOW_SERVICE);  
  
    public DesktopLyricView(Context context,List<XRCLine> sentences) {  
        super(context);  
        initSentences(sentences);
        this.setText(sentences.get(0).getWord());
        this.setBackgroundColor(Color.argb(100, 140, 160, 150));  
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(getTextSize());
        handler = new DrawHandler(this);
        timer = new MusicTimer(handler);
        timer.setIntervalTime(100);
        timer.startTimer();
    }  
    
    public void die() {
    	wm.removeView(this);
    	timer.stopTimer();
    }
  
    private int findCur(long time){
		for(int i=times.size()-1;i>=0;i--){
			if(time>times.get(i)){
				return i;
			}
		}
		return 0;
	}
    
    private void initSentences(List<XRCLine> sentences) {
    	this.sentences = sentences;
    	times.clear();
		if(null != sentences){
			for(XRCLine line:sentences){
				times.add(line.start);
			}
			Collections.sort(times);
		}
    }
    
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        // 触摸点相对于屏幕左上角坐标  
        x = event.getRawX();  
        y = event.getRawY() - TOOL_BAR_HIGH;  
        System.out.println("event = " + event.getAction());
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
            startX = event.getX();  
            startY = event.getY();  
            break;  
        case MotionEvent.ACTION_MOVE:  
            updatePosition();  
            break;  
        case MotionEvent.ACTION_UP:  
            updatePosition();  
            startX = startY = 0;  
            break;  
        }  
        return false;  
    }  
  
    @Override  
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        this.setText("");  
        float len = this.getTextSize() * text.length();  
        LinearGradient shader = new LinearGradient(0, 0, len, 0, new int[] {  
                Color.YELLOW, Color.RED }, new float[] { float1, float2 },  
                TileMode.CLAMP); 
        p.setShader(shader);  
        canvas.drawText(text, 0, getTextSize(), p);  
    }  
  
    /**
     * 更新歌词进度
     */
    private void updateLocation() {
    	List<XRCLine> temp = MusicApp.mLyricLoadHelper.getLyricSentences();
    	if(temp.isEmpty()) {
    		this.die();
    		return;
    	}
    	if(!sentences.equals(temp)) {
    		initSentences(temp);
    	}
    	long time = MusicApp.mServiceManager.position();
    	int cur = findCur(time);
    	XRCLine currentLine = sentences.get(cur);
    	if(!text.equals(currentLine.getWord())) {
    		text = currentLine.getWord();
    	}
    	int now = 0;
		for (int i = currentLine.nodes.size() - 1; i >= 0; i--) {// 获取当前字
			XRCNode node = currentLine.nodes.get(i);
			if (time > currentLine.start + node.start) {
				now = i;
				break;
			}
		}
		float off = 0;
		float allLength = 0;
		float[] widths = new float[currentLine.nodes.size()];
		p.getTextWidths(currentLine.getWord(), widths);
		for(int i = 0; i < widths.length; i ++) {
			if(i < now) {
				off += widths[i] + 1;
			}
			allLength += widths[i] + 1;
		}
		XRCNode node=currentLine.nodes.get(now);
        float percent=(float)(time-(currentLine.start+node.start))/node.length;
        if(percent>1){
        	percent=1;
        }
        off += widths[now] * percent + 1;
        
        float1 = off / (allLength);
        float2 = float1 + 0.01f; 
        if (float2 > 1.0) {  
            float1 = 0.0f;  
            float2 = 0.01f;  
        }  
    }
    
    /** 
     * 更新浮动窗口位置参数 
     */  
    private void updatePosition() {  
        // View的当前位置  
        params.x = (int) (x - startX);  
        params.y = (int) (y - startY);  
        wm.updateViewLayout(this, params);  
    }
    
    private static class DrawHandler extends Handler {

    	private DesktopLyricView view;
    	
    	DrawHandler(DesktopLyricView view) {
    		this.view = view;
    	}
    	
		@Override
		public void handleMessage(Message msg) {
			view.updateLocation();
		}
    	
    }
    
    
}
