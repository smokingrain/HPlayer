package com.ldw.music.view;

import android.content.Context;  
import android.graphics.Canvas;  
import android.graphics.Color;  
import android.graphics.LinearGradient;  
import android.graphics.Paint;  
import android.graphics.Shader;  
import android.graphics.Shader.TileMode;  
import android.graphics.Typeface;  
import android.view.MotionEvent;  
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
    private String text = "我是不是你最疼爱的人，你为什么不说话，握住是你冰冷的手动也不动让我好难过";;  
    private float float1 = 0.0f;  
    private float float2 = 0.01f; 
    private Paint p = new Paint();  
     
    WindowManager wm = (WindowManager) getContext().getApplicationContext()  
            .getSystemService(Context.WINDOW_SERVICE);  
  
    public DesktopLyricView(Context context) {  
        super(context);  
        this.setText(text);
        this.setBackgroundColor(Color.argb(100, 140, 160, 150));  
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(getTextSize());
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        // 触摸点相对于屏幕左上角坐标  
        x = event.getRawX();  
        y = event.getRawY() - TOOL_BAR_HIGH;  
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
        return true;  
    }  
  
    @Override  
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        float1 += 0.001f;  
        float2 += 0.001f;  
        if (float2 > 1.0) {  
            float1 = 0.0f;  
            float2 = 0.01f;  
        }  
        this.setText("");  
        float len = this.getTextSize() * text.length();  
        LinearGradient shader = new LinearGradient(0, 0, len, 0, new int[] {  
                Color.YELLOW, Color.RED }, new float[] { float1, float2 },  
                TileMode.CLAMP); 
        p.setShader(shader);  
        canvas.drawText(text, 0, getTextSize(), p);  
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
}
