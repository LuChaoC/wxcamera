package com.chao.iwxcamera;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class WXCamera {
    private static volatile WXCamera singleton;
    public static final int PICTURE = 570;
    public static final int VIDEO = 838;
    @IntDef({
            PICTURE,
            VIDEO
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraType {
    }
    private int type=PICTURE;
    private ResultCallback mCallback;
    private WXCamera() {
    }

    public static WXCamera getInstance() {
        if (singleton == null) {
            synchronized (WXCamera.class) {
                if (singleton == null) {
                    singleton = new WXCamera();
                }
            }
        }
        return singleton;
    }

    public WXCamera setType(@CameraType int type){
        this.type=type;
        return singleton;
    }

    public WXCamera setResultCallback(ResultCallback callback){
        this.mCallback=callback;
        return singleton;
    }

    public void start(Context context){
        if (context==null){
            throw new NullPointerException("Context is NUll!");
        }
        if (context instanceof Activity){
            CameraActivity.StartCamera(context,type,mCallback);
        }else {
            throw new IllegalArgumentException("Context is illegal!");
        }
    }
}