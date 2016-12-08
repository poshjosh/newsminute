package com.looseboxes.idisc.common.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.TextView;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.AdvertDisplayFinishedListener;
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
                final TextView textView = (TextView) DisplayAdvertFragment.this.findViewById(R.id.advertview_label);
                if (textView != null) {
                    textView.setEnabled(true);
                    textView.setText(R.string.msg_continue);
                }
            } catch (Exception e) {
                Logx.getInstance().log(getClass(), e);
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

    public int getContentViewId() {
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

        final View nomoreadverts = findViewById(R.id.advertview_nomoreadverts_button);

        OnClickListener listener = ((DefaultApplication) getActivity().getApplication()).initNoMoreAdvertsOnClickListener(this);

        nomoreadverts.setOnClickListener(listener);

        final View continueTextView = DisplayAdvertFragment.this.findViewById(R.id.advertview_label);

        continueTextView.setOnClickListener(DisplayAdvertFragment.this);
    }

    public boolean loadData(WebView webView) {

        TextView textView = ((TextView) findViewById(R.id.advertview_label));

        textView.setText(R.string.msg_pleasewait);
        textView.setEnabled(false);

        boolean loading = super.loadData(webView);

//        new AdvertCountdownTask(webView.getHandler(), 1000).start(); // didn't work
        new AdvertCountdownTask(new Handler(), 1000).start();

        return loading;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.advertview_label) {
            this.advertDisplayFinishedListener.onAdvertDisplayFinished(getArguments());
        }
    }
}
