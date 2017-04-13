package com.taoxu.mediaprojection;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tao.xu on 2016/01/28.
 */
public class Utils {
    public final static String ROOT_FOLDER_PATH = Environment.getExternalStorageDirectory().getPath() + "/MediaProjection/";
    public final static String PICTURE_FOLDER_PATH = ROOT_FOLDER_PATH
            + "Picture/";
    public final static String VIDEO_FOLDER_PATH = ROOT_FOLDER_PATH + "Video/";

    public static String getScreenshotPath() {
        String path = null;

        File rootFolder = new File(ROOT_FOLDER_PATH);
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        File tmpFolder = new File(PICTURE_FOLDER_PATH);
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy_MM_dd");
        String date = sDateFormat.format(new Date());
        File picFolder = new File(PICTURE_FOLDER_PATH + date.toString());
        if (!picFolder.exists()) {
            picFolder.mkdirs();
        }

        SimpleDateFormat sDateFormat2 = new SimpleDateFormat(
                "yyyy_MM_dd_HH_mm_ss");
        String date2 = sDateFormat2.format(new Date());
        path = PICTURE_FOLDER_PATH + date.toString() + "/" + "Halo_"
                + date2.toString() + ".png";

        return path;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean isExistsFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return true;
        }
        return false;
    }


    public static void checkRecordFolderFile() {
        File rootFolder = new File(ROOT_FOLDER_PATH);
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        File tmpFolder = new File(VIDEO_FOLDER_PATH);
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
    }

    public static boolean checkRecordFileExist(String name) {
        boolean flag = false;
        File rootFolder = new File(ROOT_FOLDER_PATH);
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        File tmpFolder = new File(VIDEO_FOLDER_PATH);
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }

        File file = new File(VIDEO_FOLDER_PATH + name + ".mp4");
        if (file.exists()) {
            flag = true;
        }
        return flag;
    }

    private static Toast mToast;
    private static Handler mhandler = new Handler();
    private static Runnable r = new Runnable() {
        public void run() {
            mToast.cancel();
        }

        ;
    };

    public static void showToast(Context context, String text) {
        mhandler.removeCallbacks(r);
        if (null != mToast) {
            mToast.setText(text);
        } else {
            mToast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        mhandler.postDelayed(r, 5000);
        mToast.show();
    }


    public static void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }

    public static void setAlertDialogAutoClose(android.app.AlertDialog dialog, boolean canClose) {
        Field field = null;
        try {
            // 利用反射,来控制dialog若是不符合条件时候不被关闭
            field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, canClose);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAlertDialogAutoClose(android.support.v7.app.AlertDialog dialog, boolean canClose) {
        Field field = null;
        try {
            // 利用反射,来控制dialog若是不符合条件时候不被关闭
            field = dialog.getClass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, canClose);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MediaProjectionApplication getApplication(Activity activity){
        return (MediaProjectionApplication) activity.getApplication();
    }
    public static MediaProjectionApplication getApplication(Service service){
        return (MediaProjectionApplication) service.getApplication();
    }
}
