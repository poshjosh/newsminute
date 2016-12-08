package com.looseboxes.idisc.common.listeners;

import android.annotation.TargetApi;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.NewsminuteUtil;

import org.json.simple.JSONObject;

/**
 * Created by Josh on 9/12/2016.
 */
public class ContentOptionsButtonListenerImpl extends AbstractFeedContentOptionsButtonListener {

    public interface ListenerDataHandler {
        String getShareText();
        JSONObject getSelectedData();
        void loadData(WebView webView, String url);
    }

    private final WebView webView;

    private final ListenerDataHandler listenerDataHandler;

    public ContentOptionsButtonListenerImpl(WebView webView, ListenerDataHandler listenerDataHandler) {
        super(webView.getContext());
        this.webView = webView;
        this.listenerDataHandler = listenerDataHandler;
    }

    @Override
    public String getSubject() {
        return NewsminuteUtil.getDefaultSubjectForMessages(this.getContext());
    }
    @Override
    public String getText() {
        return listenerDataHandler.getShareText();
    }
    @Override
    public void onClick(View v) {
//            Logx.getInstance().log(Log.VERBOSE, this.getClass(), "onClick(View)");
        this.updateButtonState(v, true);
        super.onClick(v);
    }

    private void updateButtonState(View v, boolean selected) {
        Logx.getInstance().log(Log.VERBOSE, this.getClass(), "updateButtonState(View, boolean)");
        v.setActivated(selected);
        if (App.isAcceptableVersion(this.getContext(), 14)) {
            this.setHovered_v14(v, selected);
        }
        v.setPressed(selected);
        v.setSelected(selected);
    }

    @TargetApi(14)
    private void setHovered_v14(View v, boolean selected) {
        v.setHovered(selected);
    }

    public void browseToSource(View v) {
        String url = getUrl();
        try {
            if (url == null) {
                Popup.getInstance().show(this.getContext(), R.string.msg_nolink, Toast.LENGTH_SHORT);
            } else {
                // First display loading message before the actual loading begins
                Util.loadData(this.webView, this.getContext().getString(R.string.msg_loading));
                listenerDataHandler.loadData(this.webView, url);
            }
        } catch (Exception e) {
            Popup.getInstance().show(this.getContext(), url == null ? "Error accessing URL of feed" : "Error browsing: " + url, Toast.LENGTH_SHORT);
            Logx.getInstance().debug(getClass(), e);
        }
    }
    public JSONObject getJsonData() {
        return listenerDataHandler.getSelectedData();
    }
}
