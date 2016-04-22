package com.wassup789.android.musicsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationDismissedReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getExtras().getInt("com.wassup789.android.musicsync.notificationId");

        if(notificationId == BackgroundService.notificationIDComplete)
            BackgroundService.totalFilesDownloaded = 0;
    }
}
