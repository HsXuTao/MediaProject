package com.taoxu.mediaprojection;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tao.xu on 2016/01/28.
 */
public class ScreenService extends Service implements OnClickListener,
        OnLongClickListener {
    private static final String TAG = ScreenService.class.getSimpleName();
    private MediaRecorder mMediaRecorder;
    private MediaProjection mMediaProjection;
    private Intent mResultData;
    private int mResultCode;
    private MediaProjectionManager mMediaProjectionManager;
    private VirtualDisplay mVirtualDisplay;
    private boolean mScreenSharing;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private ImageReader mImageReader;
    private int mScreenDensity;
    private Notification notification;
    private PendingIntent service;
    private LayoutParams mSideFloatLayoutParams;
    private WindowManager mSideWindowManager;
    private View mSideFloatLayout;
    private Button mScreenshot;
    private Button mScreenrecord;
    private Button mStoprecord;
    private long mCurrentTime;
    private boolean inOneTouch;
    private float x;
    private float y;
    private float mTouchSlop;
    private SoundPool mSoundPool;
    private float mCurrent;
    private AlertDialog mAlertDialog;

    @Override
    public void onCreate() {
        super.onCreate();
        initHeightAndWidth();
        LoadScreenSounds();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        initSideFloatLayout();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (notification == null) {
            // 此处是为了让Service作为前台Service,减少被系统kill掉的概率
            notification = new Notification(R.drawable.ic_launcher,
                    "MediaProjection service is running",
                    System.currentTimeMillis());
            service = PendingIntent.getService(this, 0, intent, 0);
            notification.setLatestEventInfo(this, "MediaProjection",
                    "MediaProjection service is running", service);
            startForeground(startId, notification);
        }

        Intent intent2 = new Intent(getApplicationContext(),ProtectActivity.class);
        startActivity(intent2);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mSideWindowManager != null) {
            if (mSideFloatLayout != null) {
                mSideWindowManager.removeView(mSideFloatLayout);
            }
        }
        stopMediaProjection();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory  level:" + level);
        super.onTrimMemory(level);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved  rootIntent:" + rootIntent);
        super.onTaskRemoved(rootIntent);
    }

    private void hideAllButton() {
        // TODO Auto-generated method stub
        mScreenrecord.setVisibility(View.INVISIBLE);
        mScreenshot.setVisibility(View.INVISIBLE);
        mStoprecord.setVisibility(View.INVISIBLE);
    }

    private void normalButton() {
        // TODO Auto-generated method stub
        mScreenrecord.setVisibility(View.VISIBLE);
        mScreenshot.setVisibility(View.VISIBLE);
        mStoprecord.setVisibility(View.INVISIBLE);
    }

    private void inRecordButton() {
        // TODO Auto-generated method stub
        mScreenrecord.setVisibility(View.GONE);
        mScreenshot.setVisibility(View.GONE);
        mStoprecord.setVisibility(View.VISIBLE);
    }

    private void initSideFloatLayout() {
        mSideFloatLayoutParams = new WindowManager.LayoutParams();
        mSideWindowManager = (WindowManager) getApplication().getSystemService(
                Context.WINDOW_SERVICE);
        mSideFloatLayoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        mSideFloatLayoutParams.format = PixelFormat.RGBA_8888;
        mSideFloatLayoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        mSideFloatLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mSideFloatLayoutParams.x = 0;
        mSideFloatLayoutParams.y = 0;
        mSideFloatLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mSideFloatLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        mSideFloatLayout = inflater.inflate(R.layout.side_float_layout, null);
        mSideWindowManager.addView(mSideFloatLayout, mSideFloatLayoutParams);
        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        initSideParams();
        initSideListener();
    }

    private void initSideListener() {
        mScreenshot.setOnLongClickListener(this);
        mScreenrecord.setOnLongClickListener(this);
        mScreenshot.setOnClickListener(this);
        mScreenrecord.setOnClickListener(this);
        mStoprecord.setOnClickListener(this);
        OnTouchListener onTouchListener = new TouchEventListener();
        mScreenshot.setOnTouchListener(onTouchListener);
        mScreenrecord.setOnTouchListener(onTouchListener);
        mStoprecord.setOnTouchListener(onTouchListener);
    }

    private void initSideParams() {
        mScreenshot = (Button) mSideFloatLayout.findViewById(R.id.screenshot);
        mScreenrecord = (Button) mSideFloatLayout
                .findViewById(R.id.start_record);
        mStoprecord = (Button) mSideFloatLayout.findViewById(R.id.stop_record);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScreenShot() {
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setUpMediaProjection() {
        mResultData = ((MediaProjectionApplication) getApplication()).getIntent();
        mResultCode = ((MediaProjectionApplication) getApplication()).getResult();
        mMediaProjectionManager = ((MediaProjectionApplication) getApplication()).getMediaProjectionManager();
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
        mMediaProjection.registerCallback(new MediaProjectionCallback(), new Handler(getApplication().getMainLooper()));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay() {
        mVirtualDisplay = createVirtualDisplay();
    }

    private void stopScreenSharing() {
        mScreenSharing = false;
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }

    private void initHeightAndWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager WindowManager = (WindowManager) getApplication()
                .getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = WindowManager.getDefaultDisplay();
        defaultDisplay.getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mDisplayWidth = defaultDisplay.getWidth();
        mDisplayHeight = defaultDisplay.getHeight();
    }

    private VirtualDisplay createVirtualDisplay() {
        // return mMediaProjection.createVirtualDisplay("ScreenSharingDemo",
        // mDisplayWidth, mDisplayHeight, mScreenDensity,
        // DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface,
        // null /* Callbacks */, null /* Handler */);
        return mMediaProjection
                .createVirtualDisplay("ScreenSharing", mDisplayWidth,
                        mDisplayHeight, mScreenDensity,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mImageReader.getSurface(), null /* Callbacks */, null /* Handler */);
    }

    private void resizeVirtualDisplay() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.resize(mDisplayWidth, mDisplayHeight, mScreenDensity);
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startCapture(String path) {
        try {
            Image image = mImageReader.acquireLatestImage();
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            int padding = rowPadding / pixelStride;
            Bitmap bitmap = Bitmap.createBitmap(width + padding, height,
                    Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();

            if (bitmap != null) {
                try {
                    File fileImage = new File(path);
                    if (!fileImage.exists()) {
                        fileImage.createNewFile();
                    }
                    FileOutputStream out = new FileOutputStream(fileImage);
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                        Intent media = new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(fileImage);
                        media.setData(contentUri);
                        this.sendBroadcast(media);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.deleteFile(screenshotPath);
        }
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        mImageReader.close();
    }

    private void shareScreen() {
        if (mMediaProjection == null) {
            setUpMediaProjection();
        }
        mVirtualDisplay = createRecorderVirtualDisplay();
        mMediaRecorder.start();
    }

    private VirtualDisplay createRecorderVirtualDisplay() {
        return mMediaProjection
                .createVirtualDisplay("MainActivity", mDisplayWidth,
                        mDisplayHeight, mScreenDensity,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mMediaRecorder.getSurface(), null /* Callbacks */, null /* Handler */);
    }

    private void prepareRecorder() throws IOException {
        mMediaRecorder.prepare();
    }

    private void stopRecorder() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
    }

    private void initRecorder(String name) {
        String path = Utils.VIDEO_FOLDER_PATH + name + ".mp4";
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        } else {
            mMediaRecorder.reset();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // 此处调整视屏的捕捉率,数字越大,越清晰,当然视屏文件也越大
        mMediaRecorder.setVideoEncodingBitRate(1024 * 1024);
        // 视屏帧率
        mMediaRecorder.setVideoFrameRate(24);
        mMediaRecorder.setVideoSize(mDisplayWidth, mDisplayHeight);
        mMediaRecorder.setOutputFile(path);
    }

    private void initImageReader() {
        mImageReader = ImageReader.newInstance(mDisplayWidth, mDisplayHeight, 0x1, 2); // ImageFormat.RGB_565
    }

    private void stopMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private class TouchEventListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    inOneTouch = false;
                    x = event.getRawX();
                    y = event.getRawY();
                    mCurrentTime = System.currentTimeMillis();

                    break;
                case MotionEvent.ACTION_MOVE:

                    if (Math.abs(event.getRawX() - x) > mTouchSlop
                            || Math.abs(event.getRawY() - y) > mTouchSlop) {
                        mSideFloatLayoutParams.x = (int) event.getRawX() - mScreenshot.getMeasuredWidth() / 2;
                        mSideFloatLayoutParams.y = (int) event.getRawY() - mScreenshot.getMeasuredHeight() / 2 - 25;
                        mSideWindowManager.updateViewLayout(mSideFloatLayout, mSideFloatLayoutParams);
                        mCurrentTime = System.currentTimeMillis();
                        inOneTouch = true;
                        return true;
                    }
                    // 设置长按事件
                    else if (System.currentTimeMillis() - mCurrentTime > 1000
                            && Math.abs(event.getRawX() - x) < mTouchSlop
                            && Math.abs(event.getRawY() - y) < mTouchSlop) {
                        switch (view.getId()) {
                            case R.id.start:
                            case R.id.stop:
                                Intent intent = new Intent();
                                intent.setClass(ScreenService.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                inOneTouch = true;
                                return true;

                            default:
                                break;
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (inOneTouch) {
                        settingFloatWindowPosition();
                        return true;
                    }

                    break;

                default:
                    break;
            }
            return false; // 设置为false是保证OnClickListener能够被正常触发
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        settingFloatWindowPosition();
    }

    /**
     * 让悬浮窗位置始终在两侧
     */
    private void settingFloatWindowPosition() {
        // Animation translateAnimation;
        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        if (mSideFloatLayoutParams.x < screenWidth / 2) {
            mSideFloatLayoutParams.x = 0;
        } else {
            mSideFloatLayoutParams.x = screenWidth;
        }

        mSideWindowManager.updateViewLayout(mSideFloatLayout,
                mSideFloatLayoutParams);
    }

    /**
     * 初始化音频池,主要是用来播放截屏时候"咔嚓"这个声音
     */
    private void LoadScreenSounds() {
        mSoundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        mSoundPool.load(this, R.raw.camera_click, 1);
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float streamVolumeCurrent = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurrent = streamVolumeCurrent / streamVolumeMax;

    }

    public String screenshotPath;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.screenshot:
                initImageReader();
                if (!mScreenSharing) {
                    /*
                     * 为何要这样执行: createVirtualDisplay方法将屏幕数据加载到mImageReader,需要执行时间
                     * 若是不延迟的话,有可能会发生开始读取的时候还没有执行完毕,返回为空，造成问题
                     *
                     * 而且在录屏过程中，将悬浮窗的按钮隐藏了，所以也需要延迟，防止截取出来的图片可能会带有悬浮窗按钮
                     */
                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        public void run() {
                            // start virtual
                            startScreenShot();
                            hideAllButton();
                        }

                    }, 0);

                    handler1.postDelayed(new Runnable() {
                        public void run() {
                            // capture the screen
                            screenshotPath = Utils.getScreenshotPath();
                            startCapture(screenshotPath);
                        }
                    }, 500);

                    handler1.postDelayed(new Runnable() {
                        public void run() {
                            stopVirtual();
                            normalButton();
                            if (Utils.isExistsFile(screenshotPath)) {
                                mSoundPool.play(1, mCurrent, mCurrent, 1, 0, 1f);
                                Utils.showToast(ScreenService.this, "截屏成功");
                            } else {
                                Utils.showToast(getApplicationContext(), "截屏失败，您可能没有截屏权限");
                            }
                        }
                    }, 1000);
                }
                break;

            case R.id.start_record:
                hideAllButton();
                final EditText editText = new EditText(this);
                editText.setBackgroundColor(Color.TRANSPARENT);
                editText.setTextColor(Color.BLACK);
                editText.setSingleLine(true);
                // 防止某些特殊机型会在选择输入内容的时候发生FC
                editText.setCustomSelectionActionModeCallback(new Callback() {
                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {

                    }

                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode,
                                                       MenuItem item) {
                        return false;
                    }
                });
                mAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme))
                        .setTitle("请输入视屏名字")
                        .setView(editText)
                        .setNegativeButton("确定",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Utils.setAlertDialogAutoClose(mAlertDialog, false);

                                        String name = editText.getText().toString();
                                        Pattern p = Pattern.compile("[ *^/\\\\?:<>|\"]");
                                        Matcher m = p.matcher(name);

                                        // 匹配上面的字符
                                        if (TextUtils.isEmpty(name.trim())) {
                                            Utils.showToast(ScreenService.this, "文件名不能为空");
                                            return;
                                        } else if (m.find()) {
                                            Utils.showToast(ScreenService.this, "文件名不能包含特殊字符");
                                            return;
                                        } else if (name.trim().length() > 100) {
                                            Utils.showToast(ScreenService.this, "文件名长度不能超过100");
                                            return;
                                        } else if (Utils.checkRecordFileExist(name)) {
                                            Utils.showToast(ScreenService.this, "文件名已经存在,请更改");
                                            return;
                                        } else {
                                            Utils.checkRecordFolderFile();
                                            initRecorder(name);
                                            try {
                                                prepareRecorder();
                                                shareScreen();
                                                Utils.showToast(ScreenService.this,
                                                        "录屏开始");
                                                ((MediaProjectionApplication) getApplication())
                                                        .setInRecord(true);
                                                inRecordButton();
                                                Utils.setAlertDialogAutoClose(mAlertDialog, true);
                                                dialog.dismiss();
                                            } catch (IOException e) {
                                                e.printStackTrace();

                                                Utils.deleteFile(Utils.VIDEO_FOLDER_PATH + name + ".mp4");
                                                AlertDialog dialog2 = new AlertDialog.Builder(new ContextThemeWrapper(ScreenService.this, R.style.AppTheme)).setTitle("提示").setMessage("应用可能未获得录像权限，点击确定进入设置页面")
                                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                Utils.setAlertDialogAutoClose(mAlertDialog, true);
                                                                normalButton();
                                                                mAlertDialog.dismiss();
                                                                Utils.getAppDetailSettingIntent(ScreenService.this);
                                                            }
                                                        }).setNegativeButton("取消", null).create();
                                                dialog2.getWindow().setType(
                                                        WindowManager.LayoutParams.TYPE_PHONE);
                                                dialog2.show();
                                            }
                                        }
                                    }
                                })
                        .setPositiveButton("取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Utils.setAlertDialogAutoClose(mAlertDialog, true);
                                        dialog.dismiss();
                                        normalButton();
                                    }
                                }).setCancelable(false).create();
                mAlertDialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_PHONE);
                mAlertDialog.show();

                break;
            case R.id.stop_record:
                stopRecorder();
                stopScreenSharing();
                normalButton();
                Utils.showToast(this, "录屏结束");
                ((MediaProjectionApplication) getApplication()).setInRecord(false);
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
            case R.id.screenshot:
                if (!inOneTouch) {
                    Intent intent = new Intent();
                    intent.setClass(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;

            default:
                break;
        }
        return true;
    }
}
