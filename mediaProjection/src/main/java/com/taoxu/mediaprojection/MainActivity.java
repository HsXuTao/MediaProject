package com.taoxu.mediaprojection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private Button start;
    private Button stop;
    private MediaProjectionManager mMediaProjectionManager;
    private int result = 0;
    private Intent intent = null;
    private int REQUEST_MEDIA_PROJECTION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initParams();
        initData();
        initListener();

    }

    private void initListener() {
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    /**
     * 判断 悬浮窗口权限是否打开
     *
     * @param context
     * @return true 允许  false禁止
     */
    public static boolean getAppOps(Context context) {
        try {
            Object object = context.getSystemService("appops");
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = Integer.valueOf(24);
            arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid());
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1)).intValue();
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }

    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
    }


    private void initData() {
        // TODO Auto-generated method stub
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void initParams() {
        start = getView(R.id.start);
        stop = getView(R.id.stop);
    }

    @SuppressWarnings("unchecked")
    private final <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:

                if (Build.VERSION.SDK_INT >= 23 && !getAppOps(this)) {

                    AlertDialog dialog = new AlertDialog.Builder(this).setTitle("提示").setMessage("应用可能未获得悬浮窗权限，点击确定进入设置页面")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getAppDetailSettingIntent(MainActivity.this);
                                }
                            }).setNegativeButton("取消", null).create();
                    dialog.show();
                } else {
                    start();
                }
                break;
            case R.id.stop:
                boolean flag = ((MediaProjectionApplication) getApplication())
                        .isInRecord();
                if (!flag) {
                    stop();
                } else {
                    Utils.showToast(this, "请先停止录屏操作");
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void start() {
        if (intent != null && result != 0) {
            // Service1.mResultCode = resultCode;
            // Service1.mResultData = data;
            ((MediaProjectionApplication) getApplication()).setResult(result);
            ((MediaProjectionApplication) getApplication()).setIntent(intent);
            startScreenService();

        } else {
            startActivityForResult(
                    mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
            // Service1.mMediaProjectionManager1 = mMediaProjectionManager;
            ((MediaProjectionApplication) getApplication())
                    .setMediaProjectionManager(mMediaProjectionManager);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "同意录屏后才能使用", Toast.LENGTH_SHORT).show();
                return;
            } else if (data != null && resultCode != 0) {
                result = resultCode;
                intent = data;
                ((MediaProjectionApplication) getApplication())
                        .setResult(resultCode);
                ((MediaProjectionApplication) getApplication()).setIntent(data);
                startScreenService();
            }
        }
    }

    private void startScreenService() {
        Intent intent = new Intent(getApplicationContext(), ScreenService.class);
        startService(intent);
    }

    private void stop() {
        Intent intent = new Intent(getApplicationContext(), ScreenService.class);
        stopService(intent);
        ((MediaProjectionApplication) getApplication())
                .freeMediaProjectionManager();
    }
}
