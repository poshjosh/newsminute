package com.looseboxes.idisc.common.ui;

import android.webkit.JavascriptInterface;

/**
 * Created by poshjosh on 4/18/2016.
 */
public interface HtmlExtractingJavascriptInterface {

    @JavascriptInterface
    @SuppressWarnings("unused")
    void loadHTML(String html);

    String getName();

    void clearExtractedHtmlContent();

    String getExtractedHtmlContent();
}
