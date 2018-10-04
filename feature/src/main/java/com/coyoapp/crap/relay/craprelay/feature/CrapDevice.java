package com.coyoapp.crap.relay.craprelay.feature;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;

import com.coyoapp.crap.relay.craprelay.feature.protocol.ControlCommand;
import com.coyoapp.crap.relay.craprelay.feature.protocol.CrapProtocol;
import com.coyoapp.crap.relay.craprelay.feature.protocol.ImageCommand;

import java.util.Objects;

public class CrapDevice {

    private BluetoothDevice device;
    private Context context;
    private long timestamp;
    private CrapProtocol crapProtocol = new CrapProtocol();

    public CrapDevice(@NonNull BluetoothDevice device, @NonNull Context context) {
        this.device = device;
        this.context = context;
        this.timestamp = System.currentTimeMillis();
    }

    @NonNull
    public String getName() {
        return device.getName();
    }

    @NonNull
    public String getAddress() {
        return device.getAddress();
    }

    public long getScanTimestamp() {
        return timestamp;
    }

    public void clear() {
        crapProtocol.queue(ControlCommand.clear(), device, context);
    }

    public void image(byte[] imageData) {
        crapProtocol.queue(ImageCommand.image(imageData), device, context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrapDevice that = (CrapDevice) o;
        return Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress());
    }
}
