package com.looseboxes.idisc.common.ui;

import android.content.Intent;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.bc.android.core.ui.WebViewClientImpl;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.activities.LinkHandlingActivity;
import com.bc.android.core.util.Logx;

public class DefaultWebViewClient extends WebViewClientImpl {

    private String url;

    private final OnHtmlExtractedListener onHtmlExtractedListener;

    public DefaultWebViewClient(ProgressBar progressBar, OnHtmlExtractedListener onHtmlExtractedListener) {
        super(progressBar);
        this.onHtmlExtractedListener = onHtmlExtractedListener;

    }

    @Override
    public void loadHTML(String extractedHtml) {
        super.loadHTML(extractedHtml);
        this.onHtmlExtractedListener.onHtmlExtracted(this, url, extractedHtml);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        this.url = url;
        Logx.getInstance().debug(this.getClass(), "URL: {0}", url);
        if (!url.toLowerCase().startsWith(App.getUrlScheme()+":")) {
            return false;
        }
        Intent intent = new Intent(view.getContext(), LinkHandlingActivity.class);
        intent.putExtra(LinkHandlingActivity.EXTRA_STRING_LINK_TO_DISPLAY, url);
        view.getContext().startActivity(intent);
        return true;
    }

    public final OnHtmlExtractedListener getOnHtmlExtractedListener() {
        return onHtmlExtractedListener;
    }
}
