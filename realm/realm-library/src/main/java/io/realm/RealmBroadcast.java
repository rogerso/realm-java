/*
 * Copyright 2015 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.realm;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;

class RealmBroadcast {
    public static class BroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int senderPid = intent.getIntExtra(PARAM_PID, 0);
            String realmPath = intent.getStringExtra(PARAM_REALM_PATH);
            // Same process with the sender and ignore improper pid.
            if (senderPid == processId || senderPid == 0) {
                return;
            }
            BaseRealm.notifyHandlers(realmPath, null);
        }
    }

    private final static String PARAM_PID = "pid";
    private final static String PARAM_REALM_PATH = "path";
    private static final String BROADCAST_ACTION = ".REALM_CHANGED";

    private static Context appContext;
    private static String packageName;
    private static int processId;
    private static RealmBroadcast.BroadcastReceiver broadcastReceiver;
    private static HandlerThread handlerThread;
    private static Handler handler;

    // Not thread safe, and no need to make it thread safe i think?
    static synchronized void sendBroadcast(String path) {
        // Interprocess notification is not enabled.
        if (appContext == null){
            return;
        }

        Intent intent = new Intent();
        intent.setAction(packageName + BROADCAST_ACTION);
        intent.putExtra(PARAM_PID, processId);
        intent.putExtra(PARAM_REALM_PATH, path);
        appContext.sendBroadcast(intent, packageName + ".PERMISSION.REALM_NOTIFICATION");
    }

    static synchronized void enableInterprocessNotification(Context context, String targetPackageName) {
        if (appContext != null) {
            // It is enabled already.
            return;
        }
        appContext = context.getApplicationContext();
        if (targetPackageName == null) {
            packageName = appContext.getPackageName();
        } else {
            packageName = targetPackageName;
        }

        handlerThread = new HandlerThread("RealmBroadcastReceiver");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        IntentFilter filter = new IntentFilter();
        filter.addAction(packageName + BROADCAST_ACTION);

        if (broadcastReceiver == null) {
            broadcastReceiver = new RealmBroadcast.BroadcastReceiver();
        }
        appContext.registerReceiver(broadcastReceiver, filter, packageName + ".PERMISSION.REALM_NOTIFICATION", handler);

        processId = android.os.Process.myPid();
    }

    static synchronized void disableInterprocessNotification() {
        if (appContext != null) {
            processId = 0;
            appContext.unregisterReceiver(broadcastReceiver);
            packageName = null;
            appContext = null;

            handlerThread.quit();
            handlerThread = null;
            handler = null;
        }
    }
}
