package com.taoxu.mediaprojection;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class Utils {
    public final static String ROOT_FOLDER_PATH = "/sdcard/MediaProjection/";
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
        };
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
}