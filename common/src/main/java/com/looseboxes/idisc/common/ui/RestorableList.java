package com.looseboxes.idisc.common.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class RestorableList extends ListView {
    private int listIndex;
    private int listTop;

    public RestorableList(Context context) {
        super(context);
    }

    public void onRestoreInstanceState(Bundle inState) {
        this.listIndex = inState.getInt("listIndex", -1);
        this.listTop = inState.getInt("listTop", -1);
    }

    public void onSaveInstanceState(Bundle outState) {
        int i = 0;
        this.listIndex = getFirstVisiblePosition();
        View v = getChildAt(0);
        if (v != null) {
            i = v.getTop() - getPaddingTop();
        }
        this.listTop = i;
        outState.putInt("listIndex", this.listIndex);
        outState.putInt("listTop", this.listTop);
    }

    public void scrollToRestoredListPosition() {
        if (this.listIndex != -1 && this.listTop != -1) {
            setSelectionFromTop(this.listIndex, this.listTop);
        }
    }

    public int getListIndex() {
        return this.listIndex;
    }

    public int getListTop() {
        return this.listTop;
    }
}
