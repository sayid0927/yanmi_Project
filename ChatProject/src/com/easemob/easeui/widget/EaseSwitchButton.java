package com.easemob.easeui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easemob.chatuidemo.R;

public class EaseSwitchButton extends FrameLayout{

    private ImageView openImage;
    private ImageView closeImage;

    public EaseSwitchButton(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EaseSwitchButton(Context context) {
        this(context, null);
    }
    
    public EaseSwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseSwitchButton);
        Drawable openDrawable = ta.getDrawable(R.styleable.EaseSwitchButton_switchOpenImage);
        Drawable closeDrawable = ta.getDrawable(R.styleable.EaseSwitchButton_switchCloseImage);
        int switchStatus = ta.getInt(R.styleable.EaseSwitchButton_switchStatus, 0);
        ta.recycle();
        
        LayoutInflater.from(context).inflate(R.layout.ease_widget_switch_button, this);
        openImage = (ImageView) findViewById(R.id.iv_switch_open);
        closeImage = (ImageView) findViewById(R.id.iv_switch_close);
        if(openDrawable != null){
            openImage.setImageDrawable(openDrawable);
        }
        if(closeDrawable != null){
            closeImage.setImageDrawable(closeDrawable);
        }
        if(switchStatus == 1){
            closeSwitch();
        }
        
    }
    
    /**
     * 开关是否为打开状态
     */
    public boolean isSwitchOpen(){
        return openImage.getVisibility() == View.VISIBLE;
    }
    
    /**
     * 打开开关
     */
    public void openSwitch(){
    	openImage.setVisibility(View.VISIBLE);
    	closeImage.setVisibility(View.INVISIBLE);
    }
    
    /**
     * 关闭开关
     */
    public void closeSwitch(){
    	openImage.setVisibility(View.INVISIBLE);
    	closeImage.setVisibility(View.VISIBLE);
    }

    
}
