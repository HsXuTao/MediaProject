package com.taoxu.mediaprojection;

import android.app.Application;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;

/**
 * Created by tao.xu on 2016/01/28.
 */
public class MediaProjectionApplication extends Application {
    private boolean inRecord;
    private Intent intent;
    private int result;
    private MediaProjectionManager mediaProjectionManager;

    public boolean isInRecord() {
        return inRecord;
    }

    public void setInRecord(boolean inRecord) {
        this.inRecord = inRecord;
    }

    public void freeMediaProjectionManager() {
        if (mediaProjectionManager != null) {
            mediaProjectionManager = null;
        }
    }

    public MediaProjectionManager getMediaProjectionManager() {
        return mediaProjectionManager;
    }

    public void setMediaProjectionManager(
            MediaProjectionManager mediaProjectionManager) {
        this.mediaProjectionManager = mediaProjectionManager;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
