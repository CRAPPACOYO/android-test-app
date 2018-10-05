package com.coyoapp.crap.android.test.craptest.protocol;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class ImageCommand extends ChunkedCommand {

    private static final String TAG = "ImageCommand";

    private ImageCommand(byte[] buffer) {
        super(buffer);
    }

    public static ImageCommand image(byte[] imageData) {
        return new ImageCommand(imageData);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt) {
        return gatt
                .getService(CrapProtocol.CRAP_SERVICE_UUID)
                .getCharacteristic(CrapProtocol.IMAGE_CHAR_UUID);
    }
}
