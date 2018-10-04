package com.coyoapp.crap.android.test.craptest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BleScanCallback extends ScanCallback {

    private static final String TAG = "ScanCallback";

    private Context context;
    private List<CrapDeviceScannedListener> onDeviceScannedListeners = new ArrayList<>();

    public BleScanCallback(Context context) {
        this.context = context;
    }

    public void addListener(CrapDeviceScannedListener listener) {
        onDeviceScannedListeners.add(listener);
    }

    public List<ScanFilter> getScanFilter() {
        return Collections.singletonList(
                new ScanFilter.Builder()
                        .setDeviceName("CRAP")
                        .build());
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        BluetoothDevice device = result.getDevice();
        Log.w(TAG, String.format("Device scanned: %s %s", device.getName(), device.getAddress()));
        CrapDevice crapDevice = new CrapDevice(device, context);
        for (CrapDeviceScannedListener listener : onDeviceScannedListeners) {
            listener.onDeviceScanned(crapDevice);
        }
    }
}
