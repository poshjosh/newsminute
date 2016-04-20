package com.looseboxes.idisc.common.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.ui.DefaultWebViewClient;
import com.looseboxes.idisc.common.util.Logx;

public abstract class BrowserFragment extends WebContentFragment implements OnClickListener {
    private Button urlButton;
    private EditText urlInput;

    public int getUrlInput() {
        return R.id.urlbar_urlinput;
    }

    public int getUrlButton() {
        return R.id.urlbar_button1;
    }

    public boolean isSupported(int buttonId) {
        return R.id.contentoptions_browse == buttonId || R.id.contentoptions_copy == buttonId || R.id.contentoptions_share == buttonId;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int id = getUrlInput();
        if (id > 0) {
            this.urlInput = (EditText) findViewById(id);
        }
        id = getUrlButton();
        if (id > 0) {
            this.urlButton = (Button) findViewById(id);
            if (this.urlButton != null) {
                this.urlButton.setOnClickListener(this);
            }
        }
    }

    public DefaultWebViewClient getWebViewClient() {
        return new DefaultWebViewClient((ProgressBar) findViewById(R.id.urlbar_progressbar));
    }

    public void onResume() {
        super.onResume();
        if (this.urlInput != null) {
            this.urlInput.setText(getLinkToDisplay());
        }
    }

    public void onClick(View v) {
        if (v.getId() == getUrlButton()) {
            getContentOptionsButtonListener().browseToSource(v);
            return;
        }
        Logx.log(5, getClass(), "{0} of {1} encountered unexpected view: {2}", OnClickListener.class.getName(), getClass().getName(), v);
    }
}
