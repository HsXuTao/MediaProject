package com.taoxu.mediaprojection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by tao.xu on 2017/4/12.
 * 这是一个像素为1的Activity，为了防止进程被轻易杀掉
 */
public class ProtectActivity extends Activity {
    private static String TAG = ProtectActivity.class.getSimpleName();

    @Override
    protected void onNewIntent(Intent intent) {
        moveTaskToBack(false);
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setGravity(Gravity.LEFT|Gravity.TOP);
        WindowManager.LayoutParams params  = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
        moveTaskToBack(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("onKeyDown","ProtectActivity----onKeyDown");
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            //启动一个意图,回到桌面
            Intent backHome = new Intent(Intent.ACTION_MAIN);
            backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            backHome.addCategory(Intent.CATEGORY_HOME);
            startActivity(backHome);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
