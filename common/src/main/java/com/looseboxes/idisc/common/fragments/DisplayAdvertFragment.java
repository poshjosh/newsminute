package com.looseboxes.idisc.common.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.AdvertDisplayFinishedListener;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.RepeatingTask;

public class DisplayAdvertFragment extends WebContentFragment implements OnClickListener {
    private AdvertDisplayFinishedListener advertDisplayFinishedListener;

    private class AdvertCountdownTask extends RepeatingTask {
        private TextView _cdv;
        int period;

        private AdvertCountdownTask(int interval) {
            super(interval);
            init(interval);
        }

        private AdvertCountdownTask(Handler handler, int interval) {
            super(handler, interval);
            init(interval);
        }

        private void init(int interval) {
            this.period = App.getPropertiesManager(DisplayAdvertFragment.this.getContext()).getInt(PropertyName.advertDisplayPeriod);
            if (this.period < interval) {
                throw new UnsupportedOperationException();
            }
        }

        public boolean isExpired() {
            return this.period <= 0;
        }

        protected void runTask() {
            try {
                updateCountdown();
            } finally {
                this.period -= getInterval();
            }
        }

        protected void onExpired() {
            try {
                updateCountdown();
                TextView label = (TextView) DisplayAdvertFragment.this.findViewById(R.id.advertview_label);
                if (label != null) {
                    label.setOnClickListener(DisplayAdvertFragment.this);
                    label.setTextColor(Color.BLUE);
                    label.setText(R.string.msg_continue);
                }
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
        }

        private void updateCountdown() {
            getCountdownView().setText(Integer.toString(this.period <= 0 ? 0 : this.period / getInterval()));
        }

        private TextView getCountdownView() {
            if (this._cdv == null) {
                this._cdv = (TextView) DisplayAdvertFragment.this.findViewById(R.id.advertview_countdown);
            }
            return this._cdv;
        }
    }

    public int getContentView() {
        return R.layout.advertview;
    }

    public int getWebViewId() {
        return R.id.advertview_display;
    }

    public boolean isSupported(int buttonId) {
        return false;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.advertDisplayFinishedListener = (AdvertDisplayFinishedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity + " must be instanceof " + AdvertDisplayFinishedListener.class.getName());
        }
    }

    public void onDetach() {
        super.onDetach();
        this.advertDisplayFinishedListener = null;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button nomoreadverts = (Button) findViewById(R.id.advertview_nomoreadverts_button);
        nomoreadverts.setOnClickListener(((DefaultApplication) getActivity().getApplication()).createOnClickListener(nomoreadverts));
    }

    protected boolean loadData(WebView webView) {
        ((TextView) findViewById(R.id.advertview_label)).setText(R.string.msg_pleasewait);
        boolean loading = super.loadData(webView);
// Using View#getHandler() didn't work
//        new AdvertCountdownTask(webView.getHandler(), 1000).start(); // 1 second interval
        new AdvertCountdownTask(new Handler(), 1000).start();
        return loading;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.advertview_label) {
            this.advertDisplayFinishedListener.onAdvertDisplayFinished(getArguments());
        }
    }
}
