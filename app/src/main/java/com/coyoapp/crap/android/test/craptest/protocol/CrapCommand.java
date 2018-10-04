package com.coyoapp.crap.android.test.craptest.protocol;

import android.bluetooth.BluetoothGatt;

public interface CrapCommand {

    void exec(BluetoothGatt gatt);

    boolean done(BluetoothGatt gatt, int status);
}
