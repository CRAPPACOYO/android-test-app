package com.coyoapp.crap.android.test.craptest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import static java.lang.String.format;

public class ColorConversionSettingsDialog extends DialogFragment {

    private static final String ARG_IMAGE = "image";
    private static final String ARG_CONVERSION_PARAMETERS = "conversion";

    private Bitmap image;
    private ImageView reducedColorsImage;
    private ImageConverter.BwrConversionParameters conversionParameters;
    private ColorConversionSettingsChangedListener changedListener;

    public interface ColorConversionSettingsChangedListener {
        void onColorConversionSettingsChanged(ImageConverter.BwrConversionParameters parameters);
    }

    public static ColorConversionSettingsDialog newInstance(Bitmap image, ImageConverter.BwrConversionParameters parameters) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE, image);
        args.putSerializable(ARG_CONVERSION_PARAMETERS, parameters);

        ColorConversionSettingsDialog dialog = new ColorConversionSettingsDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        image = getArguments().getParcelable(ARG_IMAGE);
        conversionParameters = (ImageConverter.BwrConversionParameters) getArguments().getSerializable(ARG_CONVERSION_PARAMETERS);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.color_conversion_settings, null);
        initLayout(layout);
        triggerImageConversion();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setView(layout)
                .setTitle(R.string.color_conversion_settings_title)
                .setPositiveButton(R.string.color_conversion_settings_apply, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (changedListener != null) {
                            changedListener.onColorConversionSettingsChanged(conversionParameters);
                        }
                    }
                })
                .setNegativeButton(R.string.color_conversion_settings_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create();
    }

    public ColorConversionSettingsDialog withChangedListener(ColorConversionSettingsChangedListener changedListener) {
        this.changedListener = changedListener;
        return this;
    }

    private void initLayout(View layout) {
        ImageView img = layout.findViewById(R.id.imgCcsDialogImage);
        reducedColorsImage = layout.findViewById(R.id.imgCcsDialogImageReducedColors);
        img.setImageBitmap(image);
        SeekBar sldThresholdBlack = layout.findViewById(R.id.sldThresholdBlack);
        sldThresholdBlack.setMax(100);
        sldThresholdBlack.setProgress((int) (conversionParameters.thresholdBlack * 100));
        sldThresholdBlack.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                conversionParameters.thresholdBlack = (float) seekBar.getProgress() / 100;
                triggerImageConversion();
            }
        });
        SeekBar sldThresholdRed = layout.findViewById(R.id.sldThresholdRed);
        sldThresholdRed.setMax(100);
        sldThresholdRed.setProgress((int) (conversionParameters.thresholdRed * 100));
        sldThresholdRed.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                conversionParameters.thresholdRed = (float) seekBar.getProgress() / 100;
                triggerImageConversion();
            }
        });
        SeekBar sldHueAngleRed = layout.findViewById(R.id.sldHueAngleRed);
        sldHueAngleRed.setMax(360);
        sldHueAngleRed.setProgress(conversionParameters.hueAngleRed);
        sldHueAngleRed.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                conversionParameters.hueAngleRed = seekBar.getProgress();
                triggerImageConversion();
            }
        });
    }

    private void triggerImageConversion() {
        Log.w("foo", format("tb %f, tr %f, har %d", conversionParameters.thresholdBlack, conversionParameters.thresholdRed, conversionParameters.hueAngleRed));
        Bitmap imageBwr = ImageConverter.toBwr(image, conversionParameters);
        reducedColorsImage.setImageBitmap(imageBwr);
    }

    private abstract static class SeekBarChangeAdapter implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
    }
}
