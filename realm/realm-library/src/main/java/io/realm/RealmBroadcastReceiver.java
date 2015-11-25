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
 */
package io.realm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RealmBroadcastReceiver extends BroadcastReceiver{
    final static String PARAM_PID = "pid";
    final static String PARAM_REALM_PATH = "path";

    private static int processId;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (processId == 0) {
            processId = android.os.Process.myPid();
        }

        int senderPid = intent.getIntExtra(PARAM_PID, processId);
        String realmPath = intent.getStringExtra(PARAM_REALM_PATH);
        // Same process with the sender
        if (senderPid == processId) {
            return;
        }
        BaseRealm.notifyHandlers(realmPath, null);
    }
}
