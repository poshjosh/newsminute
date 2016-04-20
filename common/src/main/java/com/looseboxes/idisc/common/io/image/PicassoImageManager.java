package com.looseboxes.idisc.common.io.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.os.AsyncTask;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.IOException;

public class PicassoImageManager implements ImageManager {
    private Context context;
    private int transformationType;

    public PicassoImageManager(Context context) {
        this.context = context;
        this.transformationType = 0;
    }

    class AsyncBitmapLoader extends AsyncTask<Void, Void, Bitmap> {
        final int val$height;
        final String val$imageUrl;
        final OnPostBitmapLoadListener val$onPostBitmapLoadListener;
        final int val$width;

        AsyncBitmapLoader(String str, int i, int i2, OnPostBitmapLoadListener onPostBitmapLoadListener) {
            this.val$imageUrl = str;
            this.val$width = i;
            this.val$height = i2;
            this.val$onPostBitmapLoadListener = onPostBitmapLoadListener;
        }

        protected Bitmap doInBackground(Void... params) {
            try {
                return PicassoImageManager.this.loadBitmap(this.val$imageUrl, this.val$width, this.val$height);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Bitmap bitmap) {
            this.val$onPostBitmapLoadListener.onPostLoad(bitmap);
        }
    }

    public Bitmap loadBitmap(String imageUrl, int width, int height) throws IOException {
        RequestCreator rc = from(imageUrl, width, height);
        return rc == null ? null : rc.get();
    }

    @Override
    public void loadBitmapAsync(String imageUrl, int width, int height, OnPostBitmapLoadListener listener) {
        new AsyncBitmapLoader(imageUrl, width, height, listener).execute(new Void[0]);
    }

    public RequestCreator from(String imageUrl, int width, int height) {
        Picasso picasso = Picasso.with(this.context);
        RequestCreator rc = null;
        if (!(imageUrl == null || imageUrl.isEmpty())) {
            rc = picasso.load(imageUrl);
        }
        if (rc == null) {
            return rc;
        }
        if (width > 0 && height > 0) {
            rc = rc.resize(width, height);
        }
        Transformation t = getTransformation();
        if (t != null) {
            return rc.transform(t);
        }
        return rc;
    }

    public Transformation getTransformation() {
        return this;
    }

    public Bitmap transform(Bitmap source) {
        switch (this.transformationType) {
            case TRANSFORMATION_CIRCULAR:
                return getCircularBitmapImage(source);
            default:
                return source;
        }
    }

    public String key() {
        switch (this.transformationType) {
            case TRANSFORMATION_CIRCULAR:
                return "image-circular-transformation";
            default:
                return "image-no-transformation";
        }
    }

    public Bitmap getCircularBitmapImage(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        Bitmap squaredBitmap = Bitmap.createBitmap(source, (source.getWidth() - size) / 2, (source.getHeight() - size) / 2, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }
        Bitmap bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(squaredBitmap, TileMode.CLAMP, TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = ((float) size) / 2.0f;
        canvas.drawCircle(r, r, r, paint);
        squaredBitmap.recycle();
        return bitmap;
    }

    public int getTransformationType() {
        return this.transformationType;
    }

    public void setTransformationType(int transformationType) {
        this.transformationType = transformationType;
    }
}
