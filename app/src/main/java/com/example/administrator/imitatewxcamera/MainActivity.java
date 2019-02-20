package com.example.administrator.imitatewxcamera;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import com.chao.iwxcamera.ResultCallback;
import com.chao.iwxcamera.WXCamera;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class MainActivity extends AppCompatActivity implements ResultCallback, TextureView.SurfaceTextureListener {

    private TextureView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imge);
        RxPermissions rxPermissions=new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe();
        mImageView.setSurfaceTextureListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void takePicture(View view) {
        WXCamera.getInstance()
                .setType(WXCamera.PICTURE)
                .setResultCallback(this)
                .start(this);
    }

    public void recordVideo(View view) {
        WXCamera.getInstance()
                .setType(WXCamera.VIDEO)
                .setResultCallback(this)
                .start(this);
    }

    @Override
    public void onResult(final String path) {
        Log.d("MainActivity", "path:"+path);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });

    }

    @Override
    public void onError() {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        Player.getInstance().start(Environment.getExternalStorageDirectory()+"/cache/00dbe752-2942-421d-b4df-e712a1ce1f88.mp4",new Surface(surface));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
