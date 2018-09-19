package com.daemonize.daemondevapp.images;

import android.graphics.Bitmap;

public class AndroidBitmapImage implements Image<Bitmap> {

    private Bitmap image;

    public AndroidBitmapImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public Bitmap getImageImp() {
        return image;
    }
}
