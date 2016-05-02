package com.looseboxes.idisc.common.listeners;

import android.view.View;

/**
 * Created by poshjosh on 4/18/2016.
 */
public interface ContentOptionsButtonListener extends View.OnClickListener {

//    @Override
//    void onClick(View v);

    String getSubject();

    String getText();

    String getUrl();

    void bookmark(View v);

    void favorite(View v);

    void delete(View v);

    void share(View v);

    void copy(View v);

    void browseToSource(View v);
}
