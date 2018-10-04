package com.coyoapp.crap.android.test.craptest.protocol;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import static java.lang.String.format;

public class ControlCommand implements CrapCommand {

    private static final String TAG = "ControlCommand";
    private static final byte CLEAR = 1;

    private final byte cmdCode;

    private ControlCommand(byte cmdCode) {
        this.cmdCode = cmdCode;
    }

    public static ControlCommand clear() {
        return new ControlCommand(CLEAR);
    }

    @Override
    public void exec(BluetoothGatt gatt) {
        Log.d(TAG, format("exec %d", cmdCode));
        BluetoothGattCharacteristic ctrlChar = gatt
                .getService(CrapProtocol.CRAP_SERVICE_UUID)
                .getCharacteristic(CrapProtocol.CTRL_CHAR_UUID);
        ctrlChar.setValue(new byte[]{cmdCode});
        gatt.writeCharacteristic(ctrlChar);
    }

    @Override
    public boolean done(BluetoothGatt gatt, int status) {
        Log.d(TAG, format("done: %d", status));
        return true;
    }

    @Override
    public String toString() {
        return "[ControlCommand," + cmdCode + "]";
    }
}
