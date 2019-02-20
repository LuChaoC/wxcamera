package com.chao.utils;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

public class Player {

    private static volatile Player singleton;
    private MediaPlayer mPlayer;

    private Player() {
    }

    public static Player getInstance() {
        if (singleton == null) {
            synchronized (Player.class) {
                if (singleton == null) {
                    singleton = new Player();
                }
            }
        }
        return singleton;
    }

    public void start(String path, Surface textureView) {
        Log.d("Player", "path:" + path);
        mPlayer = new MediaPlayer();
        try {

            mPlayer.setDataSource(path);
            mPlayer.setSurface(textureView);
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d("Player", "what:" + what);
                    Log.d("Player", "extra:" + extra);
                    return false;
                }
            });
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer.start();
                    mPlayer.setLooping(true);
                }
            });
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.setSurface(null);
        mPlayer.release();
        mPlayer = null;
    }
}
