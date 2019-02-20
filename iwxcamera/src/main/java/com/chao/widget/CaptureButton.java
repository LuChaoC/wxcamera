package com.chao.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.chao.iwxcamera.R;
import com.chao.iwxcamera.WXCamera;
import com.chao.utils.Recorder;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by LuChao on 2019/2/15.
 * Email:872517247@qq.com
 */
public class CaptureButton extends View implements View.OnTouchListener {
    private static final int REFRESH = 721;
    private static final int MAX_RECORD_TIME = 60;
    private static final int BORDER_WIDTH = 16;
    public static final int NORMAL_STATE = 0;
    private static final int CLICK_STATE = 1;
    private static final int LONG_NORMAL_STATE = 2;
    private static final int LONG_STATE = 3;
    private static final int LONG_FINISH_STATE = 4;
    private CaptureListener mListener;
    private int mWidth;
    private int mHeight;
    private Paint mPaint;
    private float mDownX;
    private float mDownY;
    private long mDownTime;
    private float mUpX;
    private float mUpY;
    private long mUpTime;
    private int mScaledTouchSlop;
    private float sweepDegree = 0.0f;
    private int mState = NORMAL_STATE;//0:普通状态//1:点击状态 2:长按初始化状态 3:长按录制状态
    private WeakHandler mHandler;
    private Timer mTimer;
    private int recordTime = 0;
    private int mCaptureType = WXCamera.PICTURE;
    private Bitmap mStopIcon;

    public CaptureButton(Context context) {
        this(context, null);
    }

    public CaptureButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mHandler = new WeakHandler(this);
        mTimer = new Timer();
        setOnTouchListener(this);
        mPaint = new Paint();
        mStopIcon = BitmapFactory.decodeResource(getResources(), R.drawable.record_stop);
    }

    public void setCaptureType(int captureType) {
        mCaptureType = captureType;
    }

    public void setState(int state) {
        mState = state;
        this.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = this.getWidth();
        mHeight = this.getHeight();
        if (mState == NORMAL_STATE || mState == CLICK_STATE) {
            mPaint.setColor(Color.GRAY);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(BORDER_WIDTH);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2 - BORDER_WIDTH / 2 - 20, mPaint);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.parseColor("#FFFFFF"));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mWidth / 2, mHeight / 2, (mWidth - BORDER_WIDTH * 2) / 2 - 20, paint);
        } else if (mState == LONG_NORMAL_STATE) {
            mPaint.setStrokeWidth(BORDER_WIDTH * 10);
            mPaint.setColor(Color.parseColor("#FFFFFF"));
            canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2 - BORDER_WIDTH * 5, mPaint);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.GRAY);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mWidth / 2, mHeight / 2, (mWidth - BORDER_WIDTH * 10) / 2, paint);
        } else if (mState == LONG_STATE) {
            mPaint.setStrokeWidth(BORDER_WIDTH * 10);
            mPaint.setColor(Color.parseColor("#FFFFFF"));
            canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2 - BORDER_WIDTH * 5, mPaint);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.GRAY);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mWidth / 2, mHeight / 2, (mWidth - BORDER_WIDTH * 10) / 2, paint);
            Paint roundPaint = new Paint();
            roundPaint.setColor(Color.GREEN);
            roundPaint.setAntiAlias(true);
            roundPaint.setStyle(Paint.Style.STROKE);
            roundPaint.setStrokeWidth(BORDER_WIDTH);
            RectF roundF = new RectF(0 + BORDER_WIDTH / 2, BORDER_WIDTH / 2, mWidth - BORDER_WIDTH / 2, mHeight - BORDER_WIDTH / 2);
            canvas.drawArc(roundF, -90, sweepDegree, false, roundPaint);
        } else if (mState == LONG_FINISH_STATE) {
            this.setVisibility(View.INVISIBLE);
//            Paint paint = new Paint();
//            paint.setAlpha(255);
//            canvas.drawRect(0/,0,mWidth,mHeight,paint);
//            canvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);//绘制透明色

//            canvas.drawBitmap(mStopIcon, new Rect(0, 0, mStopIcon.getWidth(), mStopIcon.getHeight()),
//                    new RectF(0 + 30, 0 + 30, mWidth - 30, mHeight - 30), new Paint());
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mDownX = event.getX();
            mDownY = event.getY();
            mDownTime = event.getDownTime();
        } else if (action == MotionEvent.ACTION_UP) {
            mUpX = event.getX();
            mUpY = event.getY();
            mUpTime = event.getEventTime();
            float absX = Math.abs(mUpX - mDownX);
            float absY = Math.abs(mUpY - mDownY);
            long absTime = Math.abs(mUpTime - mDownTime);
            if (absX < mScaledTouchSlop && absY < mScaledTouchSlop && absTime < 100 && mCaptureType == WXCamera.PICTURE) {
                if (mListener != null) {
                    mListener.onPointClick();
                }
            }
            if (mState == LONG_STATE && mCaptureType == WXCamera.VIDEO) {
                Recorder.getInstance().stop();
                stopTimer();
                if (mListener != null) {
                    mListener.onLongClick();
                }
                mState = LONG_FINISH_STATE;
                mHandler.sendEmptyMessage(REFRESH);
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mCaptureType == WXCamera.PICTURE) {
                return false;
            }
            float absX = Math.abs(event.getX() - mDownX);
            float absY = Math.abs(event.getY() - mDownY);
            final long absTime = Math.abs(event.getEventTime() - mDownTime);
            if (absX < mScaledTouchSlop && absY < mScaledTouchSlop && absTime > 100) {
                if (mState == NORMAL_STATE) {
                    mState = LONG_NORMAL_STATE;
                    if (mTimer == null) {
                        mTimer = new Timer();
                    }
                    mTimer.schedule(new Invalidate(), 0, 1000);
                    invalidate();
                }
            }
        }
        return true;
    }

    private void stopTimer() {
        mTimer.cancel();
        mTimer.purge();
        mTimer = null;
    }


    public void setCaptureListener(CaptureListener listener) {
        this.mListener = listener;
    }

    public interface CaptureListener {
        void onPointClick();

        void onLongClick();
    }

    class WeakHandler extends Handler {
        private WeakReference<CaptureButton> mReference;

        public WeakHandler(CaptureButton button) {
            mReference = new WeakReference<CaptureButton>(button);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CaptureButton captureButton = mReference.get();
            if (captureButton == null) {
                return;
            }
            if (msg.what == REFRESH) {
                invalidate();
            }
        }
    }

    class Invalidate extends TimerTask {
        @Override
        public void run() {
            if (mState == LONG_NORMAL_STATE) {
                recordTime = 0;
                sweepDegree = 0.0f;
                Recorder.getInstance().start();
                mState = LONG_STATE;
            }
            recordTime++;
            float l = recordTime * 1.0f / MAX_RECORD_TIME;
            sweepDegree = l * 360;

            if (mHandler != null) {
                mHandler.sendEmptyMessage(REFRESH);
            }

            if (recordTime == MAX_RECORD_TIME) {
                Recorder.getInstance().stop();
                stopTimer();
            }
        }
    }
}
