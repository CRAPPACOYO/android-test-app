package com.coyoapp.crap.android.test.craptest.protocol;

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
}
