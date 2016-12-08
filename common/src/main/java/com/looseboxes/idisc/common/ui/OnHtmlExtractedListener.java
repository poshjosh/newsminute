package com.looseboxes.idisc.common.ui;

import com.bc.android.core.ui.WebViewClientImpl;

/**
 * Created by Josh on 9/13/2016.
 */
public interface OnHtmlExtractedListener {

    void onHtmlExtracted(WebViewClientImpl webViewClient, String url, String extractedHtml);
}
