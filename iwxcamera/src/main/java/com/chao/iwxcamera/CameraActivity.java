package com.chao.iwxcamera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.chao.utils.Player;
import com.chao.utils.Recorder;
import com.chao.utils.ThreadManager;
import com.chao.widget.CaptureButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.chao.common.Constant.ROOT_DIR;

public class CameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, Camera.PictureCallback, CaptureButton.CaptureListener {
    private Context mContext;
    private TextureView mPreviewTv;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Camera mCamera;
    private int mType;
    private CaptureButton mActionIv;
    private ImageView mOkIv;
    private ImageView mReviewIv;
    private Bitmap mBitmap;
    private static ResultCallback mCallback;
    public static void StartCamera(Context context, int type,ResultCallback callback) {
        mCallback=callback;
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        this.mContext = this;
        Intent intent = getIntent();
        mType = intent.getIntExtra("type", 0);
        mPreviewTv = findViewById(R.id.preview_tv);
        mActionIv = findViewById(R.id.action_iv);
        mActionIv.setCaptureType(mType);
        mOkIv = findViewById(R.id.ok_iv);
        mReviewIv = findViewById(R.id.review_iv);
        mPreviewTv.setSurfaceTextureListener(this);
        mActionIv.setCaptureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (openCamera(cameraId)) return;
        startPreview(surface);
        if (mType==WXCamera.VIDEO){
            Recorder.getInstance().initRecorder(mCamera,setCameraDisplayOrientation(cameraId,mCamera));
        }
    }


    private boolean openCamera(int cameraId) {
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras <= 0) {
            showToast("该设备没有摄像头");
            return true;
        }
        mCamera = Camera.open(cameraId);
        if (mCamera == null) {
            showToast("没有可用相机");
            return true;
        }
        return false;
    }

    private Camera.Size getOptimalSize(@NonNull List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    private void startPreview(SurfaceTexture surface) {
        setParameters();
        int i = setCameraDisplayOrientation(cameraId, mCamera);
        mCamera.setDisplayOrientation(i);
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setParameters() {
        Camera.Parameters parameters = mCamera.getParameters();
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        Camera.Size optimalSize = getOptimalSize(parameters.getSupportedPreviewSizes(), mPreviewTv.getWidth(), mPreviewTv.getHeight());
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        if (mType == WXCamera.PICTURE) {
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.set("jpeg-quality", 85);
        } else if (mType == WXCamera.VIDEO) {

        }
        mCamera.setParameters(parameters);
    }

    public int setCameraDisplayOrientation(int cameraId, Camera camera) {
        //通过相机ID获得相机信息
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        //获得当前屏幕方向
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            //若屏幕方向与水平轴负方向的夹角为0度
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            //若屏幕方向与水平轴负方向的夹角为90度
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            //若屏幕方向与水平轴负方向的夹角为180度
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            //若屏幕方向与水平轴负方向的夹角为270度
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            //前置摄像头作镜像翻转
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }


    private void stopPreview() {
        if (mCamera != null) {
            mCamera.lock();
            mCamera.stopPreview();
            try {
                mCamera.setPreviewTexture(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.release();
            mCamera=null;
        }
    }

    private void changeCamera() {
        stopPreview();
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        openCamera(cameraId);
        startPreview(mPreviewTv.getSurfaceTexture());
        if (mType==WXCamera.VIDEO){
            Recorder.getInstance().initRecorder(mCamera,setCameraDisplayOrientation(cameraId,mCamera));
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        stopPreview();
        startPreview(surface);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopPreview();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPreview();
    }


    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    public void switchCamera(View view) {
        changeCamera();
    }


    public void review(View view) {
        if (mType==WXCamera.PICTURE){
            stopPreview();
            openCamera(cameraId);
            startPreview(mPreviewTv.getSurfaceTexture());
        }

        if (mType==WXCamera.VIDEO){
            Player.getInstance().stop();
            openCamera(cameraId);
            startPreview(mPreviewTv.getSurfaceTexture());
            Recorder.getInstance().resetRecorder(mCamera,setCameraDisplayOrientation(cameraId,mCamera));
            mActionIv.setState(CaptureButton.NORMAL_STATE);
        }
        mOkIv.setVisibility(View.INVISIBLE);
        mReviewIv.setVisibility(View.INVISIBLE);
    }


    public void ok(View view) {
        if (mType==WXCamera.PICTURE){
            ThreadManager.getThreadPollProxy().execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (CameraActivity.this){
                        if (mBitmap!=null&&!mBitmap.isRecycled()){
                            FileOutputStream fileOutputStream= null;
                            try {
                                File dir=new File(ROOT_DIR,"cache");
                                if (!dir.exists()){
                                    dir.mkdir();
                                }
                                File file=new File(dir,UUID.randomUUID().toString()+".jpg");
                                fileOutputStream = new FileOutputStream(file);
                                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                                fileOutputStream.flush();
                                fileOutputStream.close();
                                if (mCallback!=null){
                                    mCallback.onResult(file.getAbsolutePath());
                                }
                            } catch (FileNotFoundException e) {
                                if (mCallback!=null){
                                    mCallback.onError();
                                }
                            } catch (IOException e) {
                                if (mCallback!=null){
                                    mCallback.onError();
                                }
                            }finally {
                                mBitmap.recycle();
                            }
                        }
                    }
                }
            });
        }

        if (mType==WXCamera.VIDEO){
            if (mCallback!=null){
                mCallback.onResult(Recorder.getInstance().getSaveFile().toString());
            }
        }
        finish();
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        ThreadManager.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                synchronized (CameraActivity.this){
                    Bitmap temp=null;
                    try {
                        int i = setCameraDisplayOrientation(cameraId, mCamera);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(i);
                        temp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        mBitmap = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), matrix, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (temp!=null&&!temp.isRecycled()){
                            temp.recycle();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onPointClick() {
        if (mOkIv.getVisibility()==View.VISIBLE){
            return;
        }
        mCamera.takePicture(null, null, this);
        mOkIv.setVisibility(View.VISIBLE);
        mReviewIv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLongClick() {
        if (mOkIv.getVisibility()==View.VISIBLE){
            return;
        }
        mOkIv.setVisibility(View.VISIBLE);
        mReviewIv.setVisibility(View.VISIBLE);
        stopPreview();
        Player.getInstance().start(Recorder.getInstance().getSaveFile().getAbsolutePath().toString(),new Surface(mPreviewTv.getSurfaceTexture()));
    }
}
