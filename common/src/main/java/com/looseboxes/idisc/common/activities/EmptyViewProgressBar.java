package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class EmptyViewProgressBar extends ProgressBar {
    private Paint paint;
    private String text;

    public EmptyViewProgressBar(Context context) {
        super(context);
        init(null);
    }

    public EmptyViewProgressBar(Context context, String text) {
        super(context);
        init(text);
    }

    public EmptyViewProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }

    public EmptyViewProgressBar(Context context, AttributeSet attrs, String text) {
        super(context, attrs);
        init(text);
    }

    public EmptyViewProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(null);
    }

    public EmptyViewProgressBar(Context context, AttributeSet attrs, int defStyleAttr, String text) {
        super(context, attrs, defStyleAttr);
        init(text);
    }

    private void init(String text) {
        this.text = text;
        if (text != null) {
            if (this.paint == null) {
                this.paint = new Paint();
            }
            this.paint.setColor(Color.BLUE);
            this.paint.setTextSize(80.0f);
        }
    }

    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!(this.text == null || this.paint == null)) {
            canvas.drawText(this.text, 20.0f, 240.0f, this.paint);
        }
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
