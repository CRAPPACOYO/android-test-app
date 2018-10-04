package com.coyoapp.crap.relay.craprelay.feature.protocol;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import static java.lang.String.format;

public class CrapProtocol extends BluetoothGattCallback {

    public static final UUID CRAP_SERVICE_UUID = UUID.fromString("90d8fdc0-0cae-41df-ac77-b74d280fb9c4");
    public static final UUID CTRL_CHAR_UUID = UUID.fromString("5679484f-fd09-4b13-8776-aa2405777b42");
    public static final UUID IMAGE_CHAR_UUID = UUID.fromString("d10d2fe9-e52b-467d-9b0f-7269e2451a91");

    private static final String TAG = "CrapProtocol";

    private int state = BluetoothProfile.STATE_DISCONNECTED;
    private Queue<CrapCommand> commandQueue = new LinkedList<>();

    public void queue(CrapCommand command, BluetoothDevice device, Context context) {
        commandQueue.offer(command);
        if (isDisconnected()) {
            state = BluetoothProfile.STATE_CONNECTING;
            device.connectGatt(context, true, this);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "Characteristic written");
        CrapCommand curCmd = commandQueue.peek();
        boolean done = curCmd.done(gatt, status);
        if (done) {
            commandQueue.poll();
            execNextCmd(gatt, status);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, format("Services discovered: %d", status));
        execNextCmd(gatt, status);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, format("Connection state: %d", newState));
        state = newState;
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
        }
    }

    private void execNextCmd(BluetoothGatt gatt, int status) {
        if (commandQueue.isEmpty() || (status != BluetoothGatt.GATT_SUCCESS)) {
            Log.d(TAG, "execNextCmd: disconnecting");
            gatt.disconnect();
        } else {
            CrapCommand nextCmd = commandQueue.peek();
            Log.d(TAG, "execNextCmd: exec next cmd " + nextCmd.toString());
            nextCmd.exec(gatt);
        }
    }

    private boolean isDisconnected() {
        return state == BluetoothProfile.STATE_DISCONNECTED;
    }

}
