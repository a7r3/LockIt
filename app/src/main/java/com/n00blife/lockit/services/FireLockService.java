package com.n00blife.lockit.services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FireLockService extends FirebaseMessagingService {
    final String TAG = getClass().getSimpleName();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String unlock = remoteMessage.getData().get("unlock").toString();
        Log.d(TAG, "onMessageReceived: Called");
        if (unlock.equals("true")) {
            Log.d(TAG, "onMessageReceived: Unlocked");
            stopService(new Intent(FireLockService.this, LockService.class));
        }
    }
}
