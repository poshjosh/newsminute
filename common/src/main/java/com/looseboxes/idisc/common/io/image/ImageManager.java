package com.looseboxes.idisc.common.io.image;

import android.graphics.Bitmap;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;
import java.io.IOException;

public interface ImageManager extends Transformation {

    public static final int TRANSFORMATION_CIRCULAR = 1;
    public static final int TRANSFORMATION_NONE = 0;

    public static interface OnPostBitmapLoadListener {
        void onPostLoad(Bitmap bitmap);
    }

    RequestCreator from(String imageUrl, int width, int height);

    int getTransformationType();

    Bitmap loadBitmap(String imageUrl, int width, int height) throws IOException;

    void loadBitmapAsync(String imageUrl, int width, int height, OnPostBitmapLoadListener listener);

    void setTransformationType(int i);
}
