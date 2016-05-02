package com.looseboxes.idisc.common.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Button;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.ui.DefaultWebViewClient;
import com.looseboxes.idisc.common.activities.WebContentActivity;
import com.looseboxes.idisc.common.listeners.AbstractContentOptionsButtonListener;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.Util;
import org.apache.harmony.awt.datatransfer.DataProvider;

public abstract class WebContentFragment extends BaseFragment {

    private class ContentOptionsButtonListener extends AbstractContentOptionsButtonListener {

        private ContentOptionsButtonListener() {

            super(WebContentFragment.this.getContext());
        }

        public String getUrl() {
            return WebContentFragment.this.getLinkToDisplay();
        }

        public String getSubject() {
            return Util.getDefaultSubjectForMessages(getContext());
        }

        public String getText() {
            return WebContentFragment.this.getShareText();
        }
    }

    public abstract int getContentView();

    public abstract int getWebViewId();

    private WebView _wv;
    public WebView getWebView() {
        if(_wv == null) {
            _wv = (WebView)this.findViewById(this.getWebViewId());
        }
        return _wv;
    }

    public abstract boolean isSupported(int i);

    public boolean isContentEquals(WebContentFragment other) {
        String link_a = getLinkToDisplay();
        String link_b = other.getLinkToDisplay();
        if (link_a != null && link_b != null) {
            return link_a.equals(link_b);
        }
        String content_a = getContentToDisplay();
        String content_b = other.getContentToDisplay();
        if (content_a == null && content_b == null) {
            return true;
        }
        if (content_a == null || content_b == null) {
            return false;
        }
        return content_a.equals(content_b);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @TargetApi(19)
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        initButtons();

        WebView webView = this.getWebView();
        if (webView != null) {
            DefaultWebViewClient webViewClient = getWebViewClient();
            if (webViewClient != null) {
                webView.setWebViewClient(webViewClient);
            }

            // This helps us extract the WebView's html content
            //
            webView.addJavascriptInterface(webViewClient, webViewClient.getName());

            WebSettings webSettings = webView.getSettings();
            if (App.isAcceptableVersion(getContext(), 19)) {
                webSettings.setLayoutAlgorithm(LayoutAlgorithm.TEXT_AUTOSIZING);
            } else {
                webSettings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
            }
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
            if (isDualPaneMode(false)) {
                multiplyFontSizes(webSettings, 2);
            }
            webSettings.setLoadsImagesAutomatically(true);
            webSettings.setGeolocationEnabled(false);
            webSettings.setNeedInitialFocus(false);
            webSettings.setSaveFormData(false);
        }

        loadData();
    }

    public String getShareText() {
        String s = getContentToDisplay();
        if (s == null || s.isEmpty()) {
            s = getLinkToDisplay();
        }
        int maxLen = App.getPropertiesManager(getContext()).getInt(PropertyName.textLengthMedium);
        if (s != null && s.length() > 3 && s.length() > maxLen) {
            s = s.substring(0, maxLen - 3) + "...";
        }
        if (!(s == null || s.isEmpty())) {
            s = s + "\n\n" + Util.getMessageSuffix(getContext());
        }
        Logx.debug(getClass(), "Share content: {0}", s);
        return s;
    }

    public DefaultWebViewClient getWebViewClient() {
        return new DefaultWebViewClient(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logx.log(Log.VERBOSE, getClass(), "#onResume");
        updateButtonStates();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(this.getArguments() != null && outState != null) {
            Logx.log(Log.DEBUG, this.getClass(), "SAVING:: {0}", this.getArguments().keySet());
            outState.putAll(this.getArguments());
        }
    }

    @Override
    public void onViewStateRestored(Bundle inState) {
        super.onViewStateRestored(inState);
        if(this.getArguments() != null && inState != null) {
            Logx.log(Log.DEBUG, this.getClass(), "RESTORING:: {0}", this.getArguments().keySet());
            this.getArguments().putAll(inState);
        }
    }

    public void load(Bundle bundle, boolean clearPrevious) {
        if (bundle != null) {
            update(bundle, clearPrevious);
        }
        updateButtonStates();
        loadData();
    }

    protected boolean loadData() {
        return loadData(this.getWebView());
    }

    private void initButtons() {

        final Class cls = this.getClass();

        int[] optionsButtonIds = getContentOptionButtonIds();

        final int logLevel = Log.VERBOSE;

        Logx.log(logLevel, cls, "{0} options buttons", optionsButtonIds == null ? null : optionsButtonIds.length);

        if (optionsButtonIds != null && optionsButtonIds.length != 0) {

            View.OnClickListener listener = null;

            for (int buttonId : optionsButtonIds) {

                Button contentOptionButton = (Button) findViewById(buttonId);

                if (contentOptionButton != null) {

                    final CharSequence buttonText = contentOptionButton.getText();

                    final boolean isSupported = isSupported(buttonId);

                    if(Logx.isLoggable(logLevel))
                    Logx.log(logLevel, cls, "Button({0}) is supported: {1}", buttonText, isSupported);


                    if (isSupported) {
                        if (listener == null) {
                            listener = getContentOptionsButtonListener();
                        }
                        Logx.log(logLevel, cls, "Setting listener for Button: ({0})", buttonText);
                        contentOptionButton.setOnClickListener(listener);
                    } else {
                        contentOptionButton.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void updateButtonStates() {
        int[] optionsButtonIds = getContentOptionButtonIds();
        if (optionsButtonIds != null && optionsButtonIds.length != 0) {
            for (int buttonId : optionsButtonIds) {
                if (isSupported(buttonId)) {
                    updateButtonState(buttonId);
                }
            }
        }
    }

    protected void updateButtonState(int buttonId) {
    }

    public final int[] getContentOptionButtonIds() {
        return new int[]{R.id.contentoptions_bookmark, R.id.contentoptions_browse, R.id.contentoptions_copy, R.id.contentoptions_delete, R.id.contentoptions_favorite, R.id.contentoptions_share};
    }

    protected boolean loadData(WebView webView) {
        // First display loading message before the actual loading begins
        webView.loadDataWithBaseURL(null, getString(R.string.msg_loading), DataProvider.TYPE_HTML, "utf-8", null);
        String content = getContentToDisplay();
        if (content != null && !content.isEmpty()) {
            return loadData(webView, content, getContentType(), getContentCharset());
        }
        String urlString = getLinkToDisplay();
        if (urlString == null || urlString.isEmpty()) {
            return false;
        }
        this.loadData(webView, urlString);
        return true;
    }

    protected void loadData(WebView webView, String urlString) {
        if (urlString == null || urlString.isEmpty()) {
            throw new NullPointerException();
        }
        webView.loadUrl(urlString);
        DefaultWebViewClient webViewClient = this.getWebViewClient();
        if(webViewClient != null) {

            String extractedHtml = webViewClient.getExtractedHtmlContent();

            if(extractedHtml != null && !extractedHtml.isEmpty()) {

                this.getArguments().putString(WebContentActivity.EXTRA_STRING_CONTENT_TO_DISPLAY, extractedHtml);

                Logx.log(Log.DEBUG, this.getClass(), "Updated contents to display with HTML from URL: {0}", urlString);

                webViewClient.clearExtractedHtmlContent();
            }
        }
    }

    protected boolean loadData(WebView webView, String content, String contentType, String charset) {
        if (content == null) {
            throw new NullPointerException();
        }
        try {
            content = format(content);
            if (contentType == null) {
                contentType = DataProvider.TYPE_HTML;
            }
            if (charset == null) {
                charset = "utf-8";
            }
            webView.loadDataWithBaseURL(null, content, contentType, charset, null);
            return true;
        } catch (Exception e) {
            try {
                Logx.log(getClass(), e);
                webView.loadData(content, contentType, charset);
                return true;
            } catch (Exception e1) {
                Logx.log(getClass(), e1);
                return false;
            }
        }
    }

    public String format(String bodyHTML) {
        if (bodyHTML.contains("<html") || bodyHTML.contains("<HTML")) {
            return bodyHTML.replaceFirst("(<head>|<HEAD>)", "<head><style>img{max-width: 100%; width:auto; height: auto;}</style>");
        }
        return "<html>" + "<head><style>img{max-width: 100%; width:auto; height: auto;}</style><title>Auto Generated</title></head>" + "<body>" + bodyHTML + "</body></html>";
    }

    public String getLinkToDisplay() {
        return getArguments().getString(WebContentActivity.EXTRA_STRING_LINK_TO_DISPLAY);
    }

    public String getContentToDisplay() {
        return getArguments().getString(WebContentActivity.EXTRA_STRING_CONTENT_TO_DISPLAY);
    }

    public String getContentType() {
        return getArguments().getString(WebContentActivity.EXTRA_STRING_CONTENT_TYPE);
    }

    public String getContentCharset() {
        return getArguments().getString(WebContentActivity.EXTRA_STRING_CONTENT_CHARSET);
    }

    public com.looseboxes.idisc.common.listeners.ContentOptionsButtonListener getContentOptionsButtonListener() {
        return new ContentOptionsButtonListener();
    }

    private boolean isDualPaneMode(boolean defaultValue) {
        Activity a = getActivity();
        if (a != null) {
            return ((DefaultApplication) a.getApplication()).isDualPaneMode();
        }
        return defaultValue;
    }

    private void multiplyFontSizes(WebSettings webSettings, int factor) {
        webSettings.setDefaultFontSize(webSettings.getDefaultFontSize() * factor);
        webSettings.setDefaultFixedFontSize(webSettings.getDefaultFixedFontSize() * factor);
        webSettings.setMinimumFontSize(webSettings.getMinimumFontSize() * factor);
        webSettings.setMinimumLogicalFontSize(webSettings.getMinimumLogicalFontSize() * factor);
    }
}
