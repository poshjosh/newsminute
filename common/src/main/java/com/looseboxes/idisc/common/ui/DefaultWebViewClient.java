package com.looseboxes.idisc.common.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.activities.LinkHandlingActivity;
import com.looseboxes.idisc.common.activities.WebContentActivity;
import com.looseboxes.idisc.common.util.Logx;

public class DefaultWebViewClient extends WebViewClient implements HtmlExtractingJavascriptInterface {

    private String extractedHtmlContent;

    private ProgressBar progressBar;

    public DefaultWebViewClient(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void onProgressChanged(WebView view, String url) {
        setProgress(view.getProgress());
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (!url.toLowerCase().startsWith("app:")) {
            return false;
        }
        Intent intent = new Intent(view.getContext(), LinkHandlingActivity.class);
        intent.putExtra(LinkHandlingActivity.EXTRA_STRING_LINK_TO_DISPLAY, url);
        view.getContext().startActivity(intent);
        return true;
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        onProgressChanged(view, url);
        super.onPageStarted(view, url, favicon);
        onProgressChanged(view, url);
    }

    public void onLoadResource(WebView view, String url) {
        onProgressChanged(view, url);
        super.onLoadResource(view, url);
        onProgressChanged(view, url);
    }

    public void onPageCommitVisible(WebView view, String url) {
        onProgressChanged(view, url);
        super.onPageCommitVisible(view, url);
        onProgressChanged(view, url);
    }

    public void onPageFinished(WebView view, String url) {
        try {
            onProgressChanged(view, url);
            super.onPageFinished(view, url);
            onProgressChanged(view, url);
            if (this.progressBar != null) {
                setProgress(this.progressBar.getMax());
            }
        } catch (Throwable th) {
            if (this.progressBar != null) {
                setProgress(this.progressBar.getMax());
            }
        }finally{
            this.loadHtml(view);
        }
    }

    private void setProgress(int value) {
        if (this.progressBar != null) {
            this.progressBar.setProgress(value);
        }
    }

    private void loadHtml(WebView webView) {
        try {
            String prefix;
            if (App.isAcceptableVersion(webView.getContext(), Build.VERSION_CODES.JELLY_BEAN)) {
                prefix = "javascript:";
            } else {
                prefix = "javascript:window.";
            }
            webView.loadUrl(prefix + this.getName() + ".loadHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        }catch(Throwable t) {
            Logx.log(this.getClass(), t);
        }
    }

    // BEGIN methods of interface HtmlExtractingJavascriptInterface
    //
    @JavascriptInterface
    @SuppressWarnings("unused")
    @Override
    public void loadHTML(String html) {
        extractedHtmlContent = html;
        Logx.log(Log.DEBUG, this.getClass(), "Loaded HTML. {0} chars", html == null ? null : html.length());

    }

    @Override
    public String getName() {
        return "HTMLOut";
    }

    @Override
    public void clearExtractedHtmlContent() {
        extractedHtmlContent = null;
    }

    @Override
    public String getExtractedHtmlContent() {
        return extractedHtmlContent;
    }
    //
    // END methods of interface HtmlExtractingJavascriptInterface

    public ProgressBar getProgressBar() {
        return this.progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
}
