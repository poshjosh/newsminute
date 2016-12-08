package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.Logx.LogSettings;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.asynctasks.MultiRequest;
import com.looseboxes.idisc.common.handlers.ButtonGroupHandler;
import com.looseboxes.idisc.common.notice.CommentNotificationHandler;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds.PreferenceType;
import com.looseboxes.idisc.common.preferencefeed.PreferencefeedsManager;
import com.looseboxes.idisc.common.service.DownloadService;
import com.looseboxes.idisc.common.util.AliasesManager;
import com.looseboxes.idisc.common.util.StaticResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class DeveloperActivity extends Activity {

    private ButtonGroupHandler<Button> _bh;

    class IncreasePopupRepeatsOnClickListener implements OnClickListener {

        final /* synthetic */ TextView val$textView;

        IncreasePopupRepeatsOnClickListener(TextView textView) {
            this.val$textView = textView;
        }

        public void onClick(View v) {
            LogSettings ls = Logx.getInstance().getLogSettings();
            int n = ls.getPopupRepeats();
            ls.setPopupRepeats(n + 1);
            String msg = "Popup repeats increased to: " + (n + 1);
            this.val$textView.setText(msg);
            Popup.getInstance().show(DeveloperActivity.this, msg, 0);
        }
    }

    class DecreasePopupRepeatsOnClickListener implements OnClickListener {

        final /* synthetic */ TextView val$textView;
        DecreasePopupRepeatsOnClickListener(TextView textView) {
            this.val$textView = textView;
        }

        public void onClick(View v) {
            LogSettings ls = Logx.getInstance().getLogSettings();
            int n = ls.getPopupRepeats();
            ls.setPopupRepeats(n - 1);
            String msg = "Popup repeats decreased to: " + (n - 1);
            this.val$textView.setText(msg);
            Popup.getInstance().show(DeveloperActivity.this, msg, 0);
        }
    }

    class OnClickListenerImpl implements OnClickListener {

        final TextView val$textView;

        class FeedDownloadManagerImpl extends FeedDownloadManager {

            FeedDownloadManagerImpl(Context x0) {
                super(x0);
                this.setNoUI(false);
            }

            @Override
            protected InputStream openStream(String urlString) throws IOException {
                InputStream in = super.openStream(urlString);
                Logx.getInstance().debug(getClass(), "Input stream: " + in);
                return in;
            }

            public void onCancelled(String result) {
                super.onCancelled(result);
                Logx.getInstance().debug(getClass(), "Cancelled, result: " + result);
            }

            public void onPostSuccess(List newlyDownloadedFeeds) {
                String msg = "Done downloading " + newlyDownloadedFeeds.size() + " feeds";
                Logx.getInstance().debug(getClass(), msg);
                OnClickListenerImpl.this.val$textView.setText(msg);
            }

            protected void onPostExecute(String download) {
                super.onPostExecute(download);
                Class cls = getClass();
                if (download == null) {
                    download = "null";
                } else if (download.length() > 200) {
                    download = download.substring(0, 200);
                }
                Logx.getInstance().debug(cls, download);
            }
        }

        OnClickListenerImpl(TextView textView) {
            this.val$textView = textView;
        }

        public void onClick(View v) {
            FeedDownloadManager downloadMgr = new FeedDownloadManagerImpl(DeveloperActivity.this);
            downloadMgr.setNoUI(false);
            Logx.getInstance().debug(getClass(), "Downloading feeds");
            downloadMgr.execute();
        }
    }

    private class DeveloperButtonGroupHandler extends ButtonGroupHandler<Button> {
        public static final String DECREASE_DEVELOPER_POPUP_REPEATS = "Decrease Developer Popup Repeats";
        public static final String EXECUTE_MULTI_REQUESTS = "Execute Multi Requests";
        public static final String FIRE_FEED_NOTICE = "Fire Feed Notice";
        public static final String INCREASE_DEVELOPER_POPUP_REPEATS = "Increase Developer Popup Repeats";
        public static final String LAUNCH_ACTIVITY = "Launch Activity";
        public static final String LOAD_ALIASES = "Load AliasesManager";
        public static final String LOAD_FEEDS = "Load Feeds";
        public static final String LOAD_PROPERTIES = "Load Properties";
        public static final String LOAD_USER_PREFERENCES = "Load User Preferences";
        public static final String SELECT_LOG_LEVEL = "Select Log Level";
        public static final String START_DOWNLOAD_SERVICE = "Start Download Service";
        public static final String VIEW_LOG = "View Log";

        @Override
        public Button createNew() {
            return new Button(this.getActivity());
        }

        public void formatButton(Button btn, Resources res) {
            super.formatButton(btn, res);
            btn.setTransformationMethod(null);
        }

        public int getPreInitializationChildCount() {
            return 1;
        }

        public int getViewGroupId() {
            return R.id.developer_buttonviewgroup;
        }

        public OnClickListener getOnClickListener(Button button) {
            return DeveloperActivity.this.getOnClickListener(button.getText().toString());
        }

        public Activity getActivity() {
            return DeveloperActivity.this;
        }

        public List<String> getButtonTexts() {
            return Arrays.asList(new String[]{FIRE_FEED_NOTICE, LOAD_ALIASES, LOAD_FEEDS, LOAD_PROPERTIES, EXECUTE_MULTI_REQUESTS, LOAD_USER_PREFERENCES, SELECT_LOG_LEVEL, LAUNCH_ACTIVITY, START_DOWNLOAD_SERVICE, VIEW_LOG, INCREASE_DEVELOPER_POPUP_REPEATS, DECREASE_DEVELOPER_POPUP_REPEATS});
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.developer);
        getButtonGroupHandler().initButtons();
    }

    public ButtonGroupHandler<Button> getButtonGroupHandler() {
        if (this._bh == null) {
            this._bh = new DeveloperButtonGroupHandler();
        }
        return this._bh;
    }

    public View.OnClickListener getOnClickListener(String buttonText) {
        final TextView textView = (TextView)this.findViewById(R.id.developer_textview);
        View.OnClickListener output;
        switch(buttonText) {
            case DeveloperButtonGroupHandler.EXECUTE_MULTI_REQUESTS:
                output = this.getMultiRequestOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.LOAD_USER_PREFERENCES:
                output = this.getLoadUserFeedPreferencesOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.FIRE_FEED_NOTICE:
                output = this.getShowFeedNoticeOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.LOAD_ALIASES:
                output = this.getLoadAliasesOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.LOAD_FEEDS:
                output = this.getDownloadOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.LOAD_PROPERTIES:
                output = this.getLoadPropertiesOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.SELECT_LOG_LEVEL:
                output = this.getSelectLogLevelOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.LAUNCH_ACTIVITY:
                output = this.getLaunchActivityOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.START_DOWNLOAD_SERVICE:
                output = this.getFeedServiceOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.INCREASE_DEVELOPER_POPUP_REPEATS:
                output = this.getIncreaseDeveloperPopupOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.DECREASE_DEVELOPER_POPUP_REPEATS:
                output = this.getDecreaseDeveloperPopupOnclickListener(textView);
                break;
            case DeveloperButtonGroupHandler.VIEW_LOG:
                output = this.getViewLogOnclickListener(textView);
                break;
            default:
                throw new IllegalArgumentException("Expected Button resource ID, any of: "+
                        this.getButtonGroupHandler().getButtonTexts()+", found: "+buttonText);
        }
        return output;
    }

    private OnClickListener getFeedServiceOnclickListener(TextView textView) {
        return new OnClickListener() {
            public void onClick(View v) {
                DeveloperActivity.this.startService(new Intent(DeveloperActivity.this, DownloadService.class));
            }
        };
    }

    private OnClickListener getDownloadOnclickListener(TextView textView) {
        return new OnClickListenerImpl(textView);
    }

    private OnClickListener getSelectLogLevelOnclickListener(TextView textView) {
        return new OnClickListener() {
            public void onClick(View v) {
                DeveloperActivity.this.startActivity(new Intent(DeveloperActivity.this, SelectLogLevelActivity.class));
            }
        };
    }

    private OnClickListener getLaunchActivityOnclickListener(TextView textView) {
        return new OnClickListener() {
            public void onClick(View v) {
                DeveloperActivity.this.startActivity(new Intent(DeveloperActivity.this, LaunchActivityActivity.class));
            }
        };
    }

    private OnClickListener getViewLogOnclickListener(TextView textView) {
        return new OnClickListener() {
            public void onClick(View v) {
                DeveloperActivity.this.startActivity(new Intent(DeveloperActivity.this, ViewLogFilesActivity.class));
            }
        };
    }

    private OnClickListener getShowFeedNoticeOnclickListener(TextView textView) {
        return new OnClickListener() {
            public void onClick(View v) {
                FeedDownloadManager feedDownloadManager = new FeedDownloadManager(DeveloperActivity.this);
                feedDownloadManager.showFeedNotice(true, true);
                CommentNotificationHandler cnotice = new CommentNotificationHandler(DeveloperActivity.this);
                cnotice.showCommentNotice(FeedDownloadManager.getCommentNotifications(DeveloperActivity.this, false));
            }
        };
    }

    private OnClickListener getMultiRequestOnclickListener(TextView textView) {
        return new OnClickListener() {
            public void onClick(View v) {
                new MultiRequest(DeveloperActivity.this).execute();
            }
        };
    }

    private OnClickListener getLoadPropertiesOnclickListener(TextView textView) {
        return new OnClickListener() {
            public void onClick(View v) {
                DeveloperActivity.this.doStaticResOnclick(v);
            }
        };
    }

    private OnClickListener getLoadAliasesOnclickListener(TextView textView) {
        return new OnClickListener() {
            public void onClick(View v) {
                DeveloperActivity.this.doStaticResOnclick(v);
            }
        };
    }

    private OnClickListener getLoadUserFeedPreferencesOnclickListener(TextView textView) {
        return new OnClickListener() {
            public void onClick(View v) {
                try {
                    PreferenceType[] prefValues = PreferenceType.values();
                    for (int i = 0; i < prefValues.length; i++) {
                        PreferencefeedsManager pfm = new PreferencefeedsManager(DeveloperActivity.this, prefValues[i], false, null);
                        Popup.getInstance().show(DeveloperActivity.this, "Downloading: " + prefValues[i], 0);
                        pfm.update(true);
                    }
                } catch (Exception e) {
                    Logx.getInstance().log(getClass(), e);
                }
            }
        };
    }

    private OnClickListener getIncreaseDeveloperPopupOnclickListener(TextView textView) {
        return new IncreasePopupRepeatsOnClickListener(textView);
    }

    private OnClickListener getDecreaseDeveloperPopupOnclickListener(TextView textView) {
        return new DecreasePopupRepeatsOnClickListener(textView);
    }

    private void doStaticResOnclick(View v) {
        try {
            StaticResourceManager[] mgrs = getStaticResourcesManagers(((Button) v).getText().toString());
            if (mgrs != null) {
                for (StaticResourceManager mgr : mgrs) {
                    Popup.getInstance().show((Activity) this, "Downloading: " + mgr.getClass().getName(), 0);
                    mgr.update(true);
                }
            }
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    private StaticResourceManager[] getStaticResourcesManagers(String viewResId) {
        StaticResourceManager[] output;
        switch(viewResId) {
            case DeveloperButtonGroupHandler.LOAD_ALIASES:
                AliasesManager.AliasType[] aliasesValues = AliasesManager.AliasType.values();
                output = new StaticResourceManager[aliasesValues.length];
                for(int i=0; i<aliasesValues.length; i++) {
                    output[i] = App.getAliasesManager(this, aliasesValues[i]);
                }
                break;
            case DeveloperButtonGroupHandler.LOAD_PROPERTIES:
                output = new StaticResourceManager[]{App.getPropertiesManager(this)};
                break;
            default:
                throw new IllegalArgumentException();
        }
        return output;
    }
}
