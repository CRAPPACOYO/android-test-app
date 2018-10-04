package com.coyoapp.crap.android.test.craptest.protocol;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import static java.lang.String.format;

abstract class ChunkedCommand implements CrapCommand {

    private static final String TAG = "ChunkedCommand";
    private static final int MAX_CHUNK_SIZE = 20;

    private byte[] buffer;
    private int position = 0;

    ChunkedCommand(byte[] buffer) {
        this.buffer = buffer;
    }

    @Override
    public void exec(BluetoothGatt gatt) {
        Log.d(getTag(), format("exec - size %d", buffer.length));
        sendNextChunk(gatt);
    }

    @Override
    public boolean done(BluetoothGatt gatt, int status) {
        return sendNextChunk(gatt);
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() +
                ",size:" + buffer.length +
                ",pos:" + position + "]";
    }

    protected String getTag() {
        return TAG;
    }

    private boolean sendNextChunk(BluetoothGatt gatt) {
        int remaining = buffer.length - position;
        Log.d(getTag(), format("sendNextChunk - remaining %d", remaining));
        if (remaining > 0) {
            int len = Math.min(remaining, MAX_CHUNK_SIZE);
            byte[] sendBuf = new byte[len];
            System.arraycopy(buffer, position, sendBuf, 0, len);
            BluetoothGattCharacteristic imageChar = gatt
                    .getService(CrapProtocol.CRAP_SERVICE_UUID)
                    .getCharacteristic(CrapProtocol.IMAGE_CHAR_UUID);
            imageChar.setValue(sendBuf);
            position += len;
            gatt.writeCharacteristic(imageChar);
            return false;
        } else {
            return true;
        }
    }
}
