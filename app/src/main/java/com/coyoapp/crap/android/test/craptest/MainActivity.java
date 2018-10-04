package com.coyoapp.crap.android.test.craptest;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;

public class MainActivity extends AppCompatActivity implements CrapDeviceScannedListener {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int ACTIVITY_REQUEST_PICK_IMAGE = 1;
    private static final ScanSettings BLE_SCAN_SETTINGS = new ScanSettings.Builder().build();

    private ToggleButton btnToggleScan;
    private ImageView imgImage;
    private ImageView imgImage2;
    private BluetoothAdapter bluetoothAdapter;
    private BleScanCallback scanCallback;
    private CrapDeviceListAdapter deviceListAdapter;
    private byte[] imageData;

    public void onScanClick(View view) {
        if (btnToggleScan.isChecked()) {
            Log.d(TAG, "start scan");
            deviceListAdapter.clear();
            bluetoothAdapter.getBluetoothLeScanner().startScan(
                    scanCallback.getScanFilter(), BLE_SCAN_SETTINGS, scanCallback);
        } else {
            Log.d(TAG, "stop scan");
            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        }
    }

    public void onPickImageClick(View view) {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        startActivityForResult(pickIntent, ACTIVITY_REQUEST_PICK_IMAGE);
    }

    @Override
    public void onDeviceScanned(CrapDevice crapDevice) {
        Log.w(TAG, format("device scanned: %s", crapDevice.getAddress()));
        deviceListAdapter.add(crapDevice);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showMissingPermissionsDialog();
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        scanCallback = new BleScanCallback(this);
        scanCallback.addListener(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnToggleScan = findViewById(R.id.btnToggleScan);
        imgImage = findViewById(R.id.imgImage);
        imgImage2 = findViewById(R.id.imgImage2);
        List<CrapDevice> scannedDevices = new ArrayList<>();
        deviceListAdapter = new CrapDeviceListAdapter(this, scannedDevices);
        ListView lstBleDevices = findViewById(R.id.lstBleDevices);
        lstBleDevices.setAdapter(deviceListAdapter);
    }

    @Override
    protected void onDestroy() {
        deviceListAdapter.clear();
        scanCallback = null;
        bluetoothAdapter = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ACTIVITY_REQUEST_PICK_IMAGE) && (resultCode == Activity.RESULT_OK)) {
            Log.d(TAG, "picker result");
            CropImage.activity(data.getData())
                    .setAspectRatio(1, 1).setFixAspectRatio(true)
                    .setScaleType(CropImageView.ScaleType.FIT_CENTER)
                    .setRequestedSize(200, 200, CropImageView.RequestSizeOptions.RESIZE_FIT)
                    .start(this);
        }
        if ((requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) && (resultCode == Activity.RESULT_OK)) {
            Log.d(TAG, "crop result");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri imageUri = result.getUri();
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imgImage.setImageBitmap(image);
                Bitmap imageBwr = ImageConverter.toBwr(image);
                imageData = ImageConverter.uncompressed(imageBwr);
                imgImage2.setImageBitmap(imageBwr);
                btnToggleScan.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showMissingPermissionsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("This app needs location access");
        builder.setMessage("Please grant location access so this app can detect CRAP devices.");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        });
        builder.show();
    }

    private class CrapDeviceListAdapter extends ArrayAdapter<CrapDevice> {

        private List<CrapDevice> crapDevices;
        private View.OnClickListener itemClickedListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int position = (Integer) view.getTag();
                CrapDevice crapDevice = getItem(position);
                itemClicked(crapDevice);
            }
        };

        CrapDeviceListAdapter(@NonNull Context context, @NonNull List<CrapDevice> crapDevices) {
            super(context, 0, crapDevices);
            this.crapDevices = crapDevices;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            CrapDevice crapDevice = getItem(position);
            View view = (convertView != null)
                    ? convertView
                    : LayoutInflater.from(getContext()).inflate(R.layout.devices_list_item, parent, false);
            View item = view.findViewById(R.id.lytDeviceItem);
            item.setTag(position);
            item.setOnClickListener(itemClickedListener);
            view.<TextView>findViewById(R.id.txtName).setText(crapDevice.getName());
            view.<TextView>findViewById(R.id.txtAddress).setText(crapDevice.getAddress());
            return view;
        }

        @Override
        public void add(@Nullable CrapDevice crapDevice) {
            int position = crapDevices.indexOf(crapDevice);
            if (position < 0) {
                super.add(crapDevice);
            } else {
                crapDevices.remove(position);
                crapDevices.add(position, crapDevice);
                notifyDataSetChanged();
            }
            purgeItemsOlderThan(System.currentTimeMillis() - (1000 * 10));
        }

        private void itemClicked(CrapDevice crapDevice) {
            Log.w(TAG, "item clicked");
            btnToggleScan.setChecked(false);
            onScanClick(btnToggleScan);
            crapDevice.clear();
            crapDevice.image(imageData);
        }

        private void purgeItemsOlderThan(long threshold) {
            Collection<CrapDevice> itemsToRemove = new ArrayList<>();
            for (CrapDevice crapDevice : crapDevices) {
                if (crapDevice.getScanTimestamp() < threshold) {
                    itemsToRemove.add(crapDevice);
                }
            }
            crapDevices.removeAll(itemsToRemove);
        }
    }
}
