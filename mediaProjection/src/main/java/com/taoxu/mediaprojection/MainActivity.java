package com.taoxu.mediaprojection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.taoxu.permission.FloatWindowManager;

/**
 * Created by tao.xu on 2016/01/28.
 */
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


    private void initData() {
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        intent = ((MediaProjectionApplication) getApplication()).getIntent();
        result = ((MediaProjectionApplication) getApplication()).getResult();
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
                if (FloatWindowManager.getInstance().checkPermission(this)) {
                    start();
                } else {
                    FloatWindowManager.getInstance().applyPermission(this);
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
            Utils.showToast(this, "您的录屏功能正在运行，请停止后再启动");
//            ((MediaProjectionApplication) getApplication()).setResult(result);
//            ((MediaProjectionApplication) getApplication()).setIntent(intent);
//            startScreenService();

        } else {
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
            ((MediaProjectionApplication) getApplication()).setMediaProjectionManager(mMediaProjectionManager);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Utils.showToast(this, "同意录屏后才能使用");
            } else if (data != null && resultCode != 0) {
                result = resultCode;
                intent = data;
                ((MediaProjectionApplication) getApplication()).setResult(resultCode);
                ((MediaProjectionApplication) getApplication()).setIntent(data);
                startScreenService();
            }
        }
    }

    private void startScreenService() {

        Intent intent = new Intent(getApplicationContext(), ScreenService.class);
        startService(intent);
    }

    /**
     * 设置返回键不关闭应用,回到桌面
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("onKeyDown","MainActivity----onKeyDown");
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            //启动1像素的Activity来保活
            Intent intent2 = new Intent(getApplicationContext(), ProtectActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent2);

            //启动一个意图,回到桌面
            Intent backHome = new Intent(Intent.ACTION_MAIN);
            backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            backHome.addCategory(Intent.CATEGORY_HOME);
            startActivity(backHome);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void stop() {
        if (intent != null && result != 0) {
            Intent intent = new Intent(getApplicationContext(), ScreenService.class);
            stopService(intent);
            ((MediaProjectionApplication) getApplication()).freeMediaProjectionManager();
            ((MediaProjectionApplication) getApplication()).freeIntentAndResult();
            this.intent = null;
            this.result = 0;
        } else {
            Utils.showToast(this, "录屏功能未开启");
        }

    }
}
