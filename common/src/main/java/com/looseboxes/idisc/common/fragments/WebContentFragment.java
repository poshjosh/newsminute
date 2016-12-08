package com.looseboxes.idisc.common.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.bc.android.core.ui.WebViewClientImpl;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.WebContentActivity;
import com.looseboxes.idisc.common.listeners.AbstractContentOptionsButtonListener;
import com.looseboxes.idisc.common.ui.DefaultWebViewClient;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.ui.OnHtmlExtractedListener;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.NewsminuteUtil;

public abstract class WebContentFragment extends BaseFragment implements OnHtmlExtractedListener {

    private class ContentOptionsButtonListener extends AbstractContentOptionsButtonListener {

        private ContentOptionsButtonListener() {

            super(WebContentFragment.this.getContext());
        }

        public String getUrl() {
            return WebContentFragment.this.getLinkToDisplay();
        }

        public String getSubject() {
            return NewsminuteUtil.getDefaultSubjectForMessages(getContext());
        }

        public String getText() {
            return WebContentFragment.this.getShareText();
        }
    }

    public abstract int getContentViewId();

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

    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        initButtons();

        WebView webView = this.getWebView();

        if (webView != null) {

            DefaultWebViewClient webViewClient = getWebViewClient();

            Util.setupInBcMode(webView, webViewClient);
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
            s = s + "\n\n" + NewsminuteUtil.getMessageSuffix(getContext());
        }
        Logx.getInstance().debug(getClass(), "Share content: {0}", s);
        return s;
    }

    public DefaultWebViewClient getWebViewClient() {
        return new DefaultWebViewClient(null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logx.getInstance().log(Log.VERBOSE, getClass(), "#onResume");
        updateButtonStates();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(this.getArguments() != null && outState != null) {
            Logx.getInstance().log(Log.DEBUG, this.getClass(), "SAVING:: {0}", this.getArguments().keySet());
            outState.putAll(this.getArguments());
        }
    }

    @Override
    public void onViewStateRestored(Bundle inState) {
        super.onViewStateRestored(inState);
        if(this.getArguments() != null && inState != null) {
            Logx.getInstance().log(Log.DEBUG, this.getClass(), "RESTORING:: {0}", this.getArguments().keySet());
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

    public boolean loadData() {
        return loadData(this.getWebView());
    }

    private void initButtons() {

        final Class cls = this.getClass();

        int[] optionsButtonIds = getContentOptionButtonIds();

        final int logLevel = Log.VERBOSE;

        Logx.getInstance().log(logLevel, cls, "{0} options buttons", optionsButtonIds == null ? null : optionsButtonIds.length);

        if (optionsButtonIds != null && optionsButtonIds.length != 0) {

            View.OnClickListener listener = null;

            for (int buttonId : optionsButtonIds) {

                Button contentOptionButton = (Button) findViewById(buttonId);

                if (contentOptionButton != null) {

                    final CharSequence buttonText = contentOptionButton.getText();

                    final boolean isSupported = isSupported(buttonId);

                    if(Logx.getInstance().isLoggable(logLevel))
                    Logx.getInstance().log(logLevel, cls, "Button({0}) is supported: {1}", buttonText, isSupported);


                    if (isSupported) {
                        if (listener == null) {
                            listener = getContentOptionsButtonListener();
                        }
                        Logx.getInstance().log(logLevel, cls, "Setting listener for Button: ({0})", buttonText);
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

    protected void updateButtonState(int buttonId) { }

    public final int[] getContentOptionButtonIds() {
        return new int[]{R.id.contentoptions_bookmark, R.id.contentoptions_browse, R.id.contentoptions_copy, R.id.contentoptions_delete, R.id.contentoptions_favorite, R.id.contentoptions_share};
    }

    public boolean loadData(WebView webView) {

        // First display loading message before the actual loading begins
        Util.loadData(webView, getString(R.string.msg_loading));

        final String content = getContentToDisplay();

        if (this.acceptContentForDisplay(content)) {
            return loadData(webView, content, getContentType(), getContentCharset());
        }

        final String urlString = getLinkToDisplay();

        if (this.acceptLinkForDisplay(urlString)) {
            this.loadData(webView, urlString);
            return true;
        }else {
            return false;
        }
    }

    public boolean acceptContentForDisplay(String content) {
        return content != null && !content.isEmpty();
    }

    public boolean acceptLinkForDisplay(String urlString) {
        return urlString != null && !urlString.isEmpty();
    }

    public void loadData(WebView webView, String urlString) {

        if (urlString == null || urlString.isEmpty()) {
            throw new NullPointerException();
        }

        webView.loadUrl(urlString);
    }

    protected boolean loadData(WebView webView, String content, String contentType, String charset) {
        if (content == null) {
            throw new NullPointerException();
        }
        try {
            content = format(content);
            return Util.loadData(webView, content, contentType, charset);
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
            return false;
        }
    }

    @Override
    public void onHtmlExtracted(WebViewClientImpl webViewClient, String url, String extractedHtml) {

        if(extractedHtml != null && !extractedHtml.isEmpty()) {

            Bundle bundle = this.getArguments();
            if(bundle == null) {
                bundle = new Bundle();
                this.setArguments(bundle);
            }

            bundle.putString(WebContentActivity.EXTRA_STRING_CONTENT_TO_DISPLAY, extractedHtml);

            Logx.getInstance().log(Log.DEBUG, this.getClass(), "Updated contents to display with HTML from URL: {0}", url);
        }
    }

    public String format(String bodyHTML) {
        bodyHTML = this.addStyle(bodyHTML);
        bodyHTML = this.addGoToSourceLink(bodyHTML);
        return bodyHTML;
    }

    private String addStyle(String bodyHTML) {
        final String style = this.getStyle();
        if(style == null) {
            return bodyHTML;
        }else{
            String updated = this.insert(bodyHTML, null, "<head>", style);
            if(updated != null){
                Logx.getInstance().log(Log.VERBOSE, this.getClass(), "Added style to existing head");
                return updated;
            }
            Logx.getInstance().log(Log.VERBOSE, this.getClass(), "Created head and added style");
            return "<html>" + "<head>"+style+"<title>"+this.getContext().getString(R.string.app_label)+"</title></head>" + "<body>" + bodyHTML + "</body></html>";
        }
    }

    private String addGoToSourceLink(String bodyHTML) {
        final String goToSourceLink = this.getGoToSourceLink();
        if(goToSourceLink == null) {
            return bodyHTML;
        }else{
            String updated = this.insert(bodyHTML, goToSourceLink, "</body>", null);
            if(updated != null){
                return updated;
            }
            return goToSourceLink + bodyHTML;
        }
    }

    public String getGoToSourceLink() {
        final String link = this.getLinkToDisplay();
        if(link == null) {
            return null;
        }else {
//            return "<p style=\"width:100%; text-align:center;\"><a href=\" " + link + " \">"+this.getContext().getString(R.string.msg_gotosource)+"</a></p>";
            // &hellip;    &#8230;
            return "&hellip;&nbsp;[<a href=\" " + link + " \">"+this.getContext().getString(R.string.msg_readfullstory)+"</a>]";
        }
    }

    public String getStyle() {
        if(this.isDualPaneMode(false)) {
            return "<style>body {font-size:1.6em; line-height: 150%;} img{max-width: 100%; width:auto; height: auto;} </style>";
        }else{
            return "<style>body {font-size:1.4em; line-height: 150%;} img{max-width: 100%; width:auto; height: auto;} </style>";
        }
    }

    /**
     * @param text The text containing the <tt>target</tt> before which a <tt>prefix</tt> will be inserted
     * @param prefix The <tt>prefix</tt> to insert before the <tt>target</tt> contained within the text
     * @param target The <tt>target</tt> before which a <tt>prefix</tt> will be inserted
     * @return The updated text or null if the <tt>prefix</tt> was not inserted
     */
    private String insert(String text, String prefix, String target, String suffix) {
        int n = text.indexOf(target);
        if(n == -1) {
            target = target.toUpperCase();
            n = text.indexOf(target);
        }
        if(n != -1) {
            if(suffix != null) {
                text = text.replaceFirst(target, target + suffix);
            }
            if(prefix != null) {
                text = text.replaceFirst(target, prefix + target);
            }
            return text;
        }
        return null;
    }

    public String getLinkToDisplay() {
        return this.getString(WebContentActivity.EXTRA_STRING_LINK_TO_DISPLAY, null);
    }

    public String getContentToDisplay() {
        return this.getString(WebContentActivity.EXTRA_STRING_CONTENT_TO_DISPLAY, null);
    }

    public String getContentType() {
        return this.getString(WebContentActivity.EXTRA_STRING_CONTENT_TYPE, "text/html");
    }

    public String getContentCharset() {
        return this.getString(WebContentActivity.EXTRA_STRING_CONTENT_CHARSET, "utf-8");
    }

    public String getString(String key, String defaultValue) {
        String output;
        Bundle bundle = this.getArguments();
        if(bundle == null || bundle.isEmpty()) {
            output = defaultValue;
        }else{
            output = bundle.getString(key);
            if(output == null) {
                output = defaultValue;
            }
        }
        return output;
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

    private void multiplyFontSizes(WebSettings webSettings, float factor) {
        webSettings.setDefaultFontSize( (int)(webSettings.getDefaultFontSize() * factor) );
//        webSettings.setDefaultFixedFontSize( (int)(webSettings.getDefaultFixedFontSize() * factor) );
//        webSettings.setMinimumFontSize( (int)(webSettings.getMinimumFontSize() * factor) );
//        webSettings.setMinimumLogicalFontSize( (int)(webSettings.getMinimumLogicalFontSize() * factor) );
    }
}
