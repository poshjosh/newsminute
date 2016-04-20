package com.looseboxes.idisc.common.listeners;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Util;

public abstract class AbstractContentOptionsButtonListener implements ContentOptionsButtonListener {
    private Context context;

    public AbstractContentOptionsButtonListener(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) {
Logx.log(Log.VERBOSE, this.getClass(), "onClick(View)");
        int id = v.getId();
        if (id == R.id.contentoptions_share) {
            share(v);
        } else if (id == R.id.contentoptions_browse) {
            browseToSource(v);
        } else if (id == R.id.contentoptions_bookmark) {
            bookmark(v);
        } else if (id == R.id.contentoptions_favorite) {
            favorite(v);
        } else if (id == R.id.contentoptions_copy) {
            copy(v);
        } else if (id == R.id.contentoptions_delete) {
            delete(v);
        } else {
            throw new IllegalArgumentException("Uexpected view id: " + id);
        }
    }

    @Override
    public void bookmark(View v) {
    }

    @Override
    public void favorite(View v) {
    }

    @Override
    public void delete(View v) {
    }

    @Override
    public void share(View v) {
        v.getContext().startActivity(Intent.createChooser(Util.createShareIntent(this.context, getSubject(), "android.intent.extra.TEXT"), "Share via"));
    }

    @Override
    public void copy(View v) {
        Context context = v.getContext();
        String text = getText();
        if (App.isAcceptableVersion(context, 11)) {
            ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Copied Text", text));
        } else {
            ((android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setText(text);
        }
        Popup.show(this.context, R.string.msg_copied_to_clipboard, 0);
    }

    @Override
    public void browseToSource(View v) {
        String url = getUrl();
        try {
            if (url == null) {
                Popup.show(this.context, R.string.msg_nolink, 1);
            } else {
                v.getContext().startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
            }
        } catch (Exception e) {
            Popup.show(this.context, url == null ? "Error accessing URL of feed" : "Error browsing: " + url, 1);
            Logx.debug(getClass(), e);
        }
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
