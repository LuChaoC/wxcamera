package com.chao.utils;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;

import com.chao.common.Constant;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Recorder {
    private static volatile Recorder singleton;
    private static MediaRecorder mRecorder;
    private boolean isReocrd = false;
    private File mSaveFile;

    private Recorder() {
    }

    public static Recorder getInstance() {
        if (singleton == null) {
            synchronized (Recorder.class) {
                if (singleton == null) {
                    singleton = new Recorder();
                    mRecorder = new MediaRecorder();
                }
            }
        }
        return singleton;
    }

    public void initRecorder(Camera camera,int degree) {
        stop();
        mRecorder.reset();
        camera.unlock();
        mRecorder.setCamera(camera);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setVideoSize(Constant.VIDEO_WIDTH,Constant.VIDEO_HEIGHT);
        mRecorder.setVideoFrameRate(Constant.FRAME_RATE);
        mRecorder.setOrientationHint(degree);
        mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                Log.d("RecordUtils", "what:" + what);
                Log.d("RecordUtils", "extra:" + extra);
            }
        });
        mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.d("Recorder", "what:" + what);
                Log.d("Recorder", "extra:" + extra);
            }
        });
    }




    public void start() {
        File dir = new File(Constant.ROOT_DIR, "cache");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mSaveFile = new File(dir, UUID.randomUUID() + ".mp4");
        mRecorder.setOutputFile(mSaveFile.getAbsolutePath());
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
        isReocrd = true;
    }


    public File getSaveFile() {
        return mSaveFile;
    }

    public void resetRecorder(Camera camera,int degree) {
        if (mSaveFile.exists()) {
            mSaveFile.delete();
        }
        initRecorder(camera, degree);
    }

    public void stop() {
        if (isReocrd){
            mRecorder.stop();
            isReocrd = false;
        }
//        mRecorder.release();
    }
}