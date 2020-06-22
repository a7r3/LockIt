package com.n00blife.lockit.services;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.n00blife.lockit.util.Utils;

public class FireLockService extends FirebaseMessagingService {
    final String TAG = getClass().getSimpleName();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String unlock = remoteMessage.getData().get("unlock");
        String lock = remoteMessage.getData().get("lock");
        Log.d(TAG, "onMessageReceived: Called");
        if (unlock != null) {
            if (unlock.equals("true")) {
                Log.d(TAG, "onMessageReceived: Unlocked: Stopping LockService");
                stopService(new Intent(FireLockService.this, LockService.class));
            } else if (unlock.equals("false")) {
                Log.d(TAG, "onMessageReceived: Locking Device: Calling LockService");
                Utils.startLockService(FireLockService.this);
            }
        }
    }
}
