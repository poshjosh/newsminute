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
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.asynctasks.MultiRequest;
import com.looseboxes.idisc.common.handlers.ButtonGroupHandler;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedSearcher.FeedSearchResult;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.service.DownloadService;
import com.looseboxes.idisc.common.util.AliasesManager;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Logx.LogSettings;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager.PreferenceType;
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
            LogSettings ls = Logx.getLogSettings();
            int n = ls.getPopupRepeats();
            ls.setPopupRepeats(n + 1);
            String msg = "Popup repeats increased to: " + (n + 1);
            this.val$textView.setText(msg);
            Popup.show(DeveloperActivity.this, msg, 0);
        }
    }

    class DecreasePopupRepeatsOnClickListener implements OnClickListener {

        final /* synthetic */ TextView val$textView;
        DecreasePopupRepeatsOnClickListener(TextView textView) {
            this.val$textView = textView;
        }

        public void onClick(View v) {
            LogSettings ls = Logx.getLogSettings();
            int n = ls.getPopupRepeats();
            ls.setPopupRepeats(n - 1);
            String msg = "Popup repeats decreased to: " + (n - 1);
            this.val$textView.setText(msg);
            Popup.show(DeveloperActivity.this, msg, 0);
        }
    }

    /* renamed from: com.looseboxes.idisc.common.activities.DeveloperActivity.2 */
    class AnonymousClass2 implements OnClickListener {
        final /* synthetic */ TextView val$textView;

        /* renamed from: com.looseboxes.idisc.common.activities.DeveloperActivity.2.1 */
        class AnonymousClass1 extends FeedDownloadManager {
            AnonymousClass1(Context x0) {
                super(x0);
            }

            protected InputStream openRemoteStream(String urlString) throws IOException {
                InputStream in = super.openRemoteStream(urlString);
                Logx.debug(getClass(), "Input stream: " + in);
                return in;
            }

            public void onCancelled(String result) {
                Logx.debug(getClass(), "Cancelled, result: " + result);
            }

            public void onPostSuccess(List download) {
                String msg = "Done downloading " + download.size() + " feeds";
                Logx.debug(getClass(), msg);
                AnonymousClass2.this.val$textView.setText(msg);
            }

            protected void onPostExecute(String contents) {
                Class cls = getClass();
                if (contents == null) {
                    contents = "null";
                } else if (contents.length() > 200) {
                    contents = contents.substring(0, 200);
                }
                Logx.debug(cls, contents);
            }

            public void displayMessage(Object msg, int length) {
                Popup.show(DeveloperActivity.this, msg, length);
            }
        }

        AnonymousClass2(TextView textView) {
            this.val$textView = textView;
        }

        public void onClick(View v) {
            FeedDownloadManager downloadMgr = new AnonymousClass1(DeveloperActivity.this);
            downloadMgr.setNoUI(false);
            Logx.debug(getClass(), "Downloading feeds");
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

        private DeveloperButtonGroupHandler() {
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

        public Class<Button> getButtonClass() {
            return Button.class;
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
        return new AnonymousClass2(textView);
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

            /* renamed from: com.looseboxes.idisc.common.activities.DeveloperActivity.6.1 */
            class AnonymousClass1 extends FeedNotificationHandler {
                AnonymousClass1(Context x0) {
                    super(x0);
                }

                protected FeedSearchResult getResultForDisplay(Feed feed, List download, int displayLen) {
                    FeedSearchResult result = super.getResultForDisplay(feed, download, displayLen);
                    if (result == null) {
                        return getMostRecent(feed, download, displayLen);
                    }
                    return result;
                }
            }

            public void onClick(View v) {
                FeedNotificationHandler notice = new AnonymousClass1(DeveloperActivity.this);
                notice.showFeedNotice();
                notice.showCommentNotice(FeedDownloadManager.getCommentNotifications(DeveloperActivity.this, false));
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
                        PreferenceFeedsManager pfm = App.getPreferenceFeedsManager(DeveloperActivity.this, prefValues[i]);
                        pfm.setNoUI(false);
                        Popup.show(DeveloperActivity.this, "Downloading: " + prefValues[i], 0);
                        pfm.update(true);
                    }
                } catch (Exception e) {
                    Logx.log(getClass(), e);
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
                    Popup.show((Activity) this, "Downloading: " + mgr.getClass().getName(), 0);
                    mgr.update(true);
                }
            }
        } catch (Exception e) {
            Logx.log(getClass(), e);
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
