package com.coyoapp.crap.relay.craprelay.feature.protocol;

import android.bluetooth.BluetoothGatt;

public interface CrapCommand {

    void exec(BluetoothGatt gatt);

    boolean done(BluetoothGatt gatt, int status);
}
