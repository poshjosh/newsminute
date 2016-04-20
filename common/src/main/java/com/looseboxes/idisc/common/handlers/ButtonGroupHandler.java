package com.looseboxes.idisc.common.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import com.looseboxes.idisc.common.R;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class ButtonGroupHandler<T extends Button> {
    private List<T> _btns_accessViaGetter;

    public abstract Activity getActivity();

    public abstract Class<T> getButtonClass();

    public abstract List<String> getButtonTexts();

    public abstract int getViewGroupId();

    public int getPreInitializationChildCount() {
        return 0;
    }

    public OnClickListener getOnClickListener(T t) {
        return null;
    }

    public T createNew() throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        return (T) getButtonClass().getConstructor(new Class[]{Context.class}).newInstance(new Object[]{getActivity()});
    }

    public T initButton(String buttonText) {
        return getButton(buttonText, true);
    }

    public T getButton(String buttonText) {
        return getButton(buttonText, false);
    }

    public T getButton(String buttonText, boolean create) {
        List<T> buttons = getButtons(create);
        if (buttons == null || buttons.isEmpty()) {
            return null;
        }
        for (T cb : buttons) {
            if (cb.getText().equals(buttonText)) {
                return cb;
            }
        }
        return null;
    }

    public List<T> initButtons() {
        return getButtons(true);
    }

    public List<T> getButtons() {
        return getButtons(false);
    }

    public List<T> getButtons(boolean create) {
        if (this._btns_accessViaGetter == null) {
            Activity context = getActivity();
            ViewGroup layout = (ViewGroup) context.findViewById(getViewGroupId());
            int childCount = layout.getChildCount();
            int i;
            if (childCount > getPreInitializationChildCount() || !create) {
                this._btns_accessViaGetter = new ArrayList(childCount - getPreInitializationChildCount());
                for (i = 0; i < childCount; i++) {
                    this._btns_accessViaGetter.add((T) layout.getChildAt(i));
                }
            } else {
                List<String> categories = getButtonTexts();
                Resources res = context.getResources();
                int size = categories.size();
                this._btns_accessViaGetter = new ArrayList(size);
                LayoutParams params = new LayoutParams(-1, -2);
                int margin = getButtonMargin(res);
                params.setMargins(margin, margin, margin, margin);
                i = 0;
                while (i < size) {
                    try {
                        T cb = createNew();
                        cb.setLayoutParams(params);
                        cb.setText((String) categories.get(i));
                        formatButton(cb, res);
                        OnClickListener listener = getOnClickListener(cb);
                        if (listener != null) {
                            cb.setOnClickListener(listener);
                        }
                        this._btns_accessViaGetter.add(cb);
                        layout.addView(cb);
                        i++;
                    } catch (Exception e) {
                        throw new UnsupportedOperationException(e);
                    }
                }
            }
        }
        return this._btns_accessViaGetter;
    }

    public void formatButton(Button btn, Resources res) {
        int textColorRes = getButtonTextColor(res);
        if (textColorRes != -1) {
            btn.setTextColor(textColorRes);
        }
        int backgroundRes = getButtonBackgroundResource();
        if (backgroundRes != -1) {
            btn.setBackgroundResource(backgroundRes);
        }
        int padding = getButtonPadding(res);
        btn.setPadding(padding, padding, padding, padding);
        btn.setTextSize(getButtonTextSizeUnit(), getButtonTextSize(res));
        btn.setTextSize(getButtonTextSize(res));
    }

    public int getButtonPadding(Resources res) {
        return (int) res.getDimension(R.dimen.space_small);
    }

    public int getButtonMargin(Resources res) {
        return (int) res.getDimension(R.dimen.space_small);
    }

    public int getButtonTextSizeUnit() {
        return 2;
    }

    public float getButtonTextSize(Resources res) {
        return 20.0f;
    }

    public int getButtonBackgroundResource() {
        return -1;
    }

    public int getButtonTextColor(Resources res) {
        return -1;
    }
}
