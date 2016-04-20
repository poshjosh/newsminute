package com.looseboxes.idisc.common.activities;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.fragments.DefaultListFragment;
import com.looseboxes.idisc.common.fragments.DefaultListFragment.ListItemClickHandler;
import com.looseboxes.idisc.common.fragments.DisplayAdvertFragment;
import com.looseboxes.idisc.common.fragments.WebContentFragment;
import com.looseboxes.idisc.common.handlers.FeedDisplayHandler;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.listeners.TabOnClickListener;
import com.looseboxes.idisc.common.util.AdvertDisplayFinishedListener;
import com.looseboxes.idisc.common.util.AdvertManager;
import com.looseboxes.idisc.common.util.FeedhitManager;
import com.looseboxes.idisc.common.util.LocalTransitionAdvertDisplay;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.ArrayList;
import java.util.Collection;
import org.json.simple.JSONObject;

public abstract class ListFragmentActivity extends AdsActivity implements ListItemClickHandler, LocalTransitionAdvertDisplay, AdvertDisplayFinishedListener {

    private DefaultListFragment _alf;
    private View _df;
    private FeedDisplayHandler _fdh_accessViaGetter;
    private FeedListAdapter _lav_accessViaGetter;
    private WebContentFragment currentDetailsFragment;
    private boolean dualPaneMode;
    private boolean dualPaneVisible;

    /* renamed from: com.looseboxes.idisc.common.activities.ListFragmentActivity.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ HorizontalScrollView val$scrolls;

        AnonymousClass1(HorizontalScrollView horizontalScrollView) {
            this.val$scrolls = horizontalScrollView;
        }

        public void run() {
            try {
                int tabBtnId = ListFragmentActivity.this.getTabId();
                if (tabBtnId >= 0) {
                    this.val$scrolls.smoothScrollTo(ListFragmentActivity.this.findViewById(tabBtnId).getLeft(), 0);
                }
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
        }
    }

    /* renamed from: com.looseboxes.idisc.common.activities.ListFragmentActivity.2 */
    class AnonymousClass2 extends FeedDisplayHandler {
        final /* synthetic */ ListFragmentActivity val$listActivity;

        AnonymousClass2(Context x0, ListView x1, FeedListAdapter x2, ListFragmentActivity listFragmentActivity) {
            super(x0, x1, x2);
            this.val$listActivity = listFragmentActivity;
        }

        public void onExceedBottom() {
            super.onExceedBottom();
            this.val$listActivity.onExceedBottom();
        }

        public void onReachedBottom() {
            super.onReachedBottom();
            this.val$listActivity.onReachedBottom();
        }

        public void onExceedTop() {
            super.onExceedTop();
            this.val$listActivity.onExceedTop();
        }

        public void onReachedTop() {
            super.onReachedTop();
            this.val$listActivity.onReachedTop();
        }

        public void postDisplayFeeds(Collection feeds) {
            super.postDisplayFeeds(feeds);
            this.val$listActivity.postDisplayFeeds(feeds);
        }

        public Collection preDisplayFeeds(Collection feeds) {
            return this.val$listActivity.preDisplayFeeds(super.preDisplayFeeds(feeds));
        }
    }

    protected abstract WebContentFragment createDetailsFragment();

    public abstract int getDetailsFragmentId();

    public abstract int getListFragmentId();

    public abstract int getTabId();

    protected DisplayAdvertFragment createAdvertFragment() {
        return new DisplayAdvertFragment();
    }

    protected void handleIntent(Intent intent) {
        try {
            updateDualPaneVisibility();
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    protected void onResume() {
        try {
            updateDualPaneVisibility();
            super.onResume();
            updateActiveTab(getTabId());
            scrollToRestoredTabPosition();
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    private void updateDualPaneVisibility() {
        boolean z;
        boolean z2 = true;
        View detailsFrame = getDetailsFrame();
        if (detailsFrame != null) {
            z = true;
        } else {
            z = false;
        }
        this.dualPaneMode = z;
        if (detailsFrame == null || detailsFrame.getVisibility() != View.VISIBLE) {
            z2 = false;
        }
        this.dualPaneVisible = z2;
        DefaultApplication app = (DefaultApplication) getApplication();
        app.setDualPaneMode(this.dualPaneMode);
        app.setDualPaneVisible(this.dualPaneVisible);
        if (detailsFrame == null) {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(getDetailsFragmentId());
            if (fragment != null) {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.remove(fragment);
                ft.commit();
            }
        }
    }

    public Collection<JSONObject> preDisplayFeeds(Collection<JSONObject> feeds) {
        return feeds;
    }

    public void postDisplayFeeds(Collection<JSONObject> collection) {
    }

    public void loadNewer() {
    }

    public void onExceedTop() {
        loadNewer();
    }

    public void onReachedTop() {
        loadNewer();
    }

    public void onReachedBottom() {
    }

    public void onExceedBottom() {
    }

    public String getShareText() {
        String appName = getString(R.string.app_label);
        String appPackageName = getApplication().getPackageName();
        int numberOfFeedSources = App.getPropertiesManager(this).getMap(PropertyName.sources).size();
        String downloadUrl = "https://play.google.com/store/apps/details?id=" + appPackageName;
        Logx.debug(getClass(), "Share content: {0}", getString(R.string.msg_trythisapp, new Object[]{appName, Integer.valueOf(numberOfFeedSources), downloadUrl}));
        return getString(R.string.msg_trythisapp, new Object[]{appName, Integer.valueOf(numberOfFeedSources), downloadUrl});
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        try {
            super.onPostCreate(savedInstanceState);
            setUpTabs();
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    public Bundle getAdvertBundle(int position, long id) {
        JSONObject feedData = getItemToDisplay(position, id);
        if (feedData == null || feedData.isEmpty()) {
            throw new NullPointerException();
        }
        Bundle bundle = new Bundle();
        bundle.putString(DisplayFeedActivity.EXTRA_STRING_SELECTED_FEED_JSON, feedData.toJSONString());
        return bundle;
    }

    public JSONObject getItemToDisplay(int position, long id) {
        JSONObject output = null;
        try {
            FeedListAdapter adapter = getFeedListAdapter();
            if (adapter == null || adapter.isEmpty()) {
                return null;
            }
            if (id >= 0) {
                for (int pos = 0; pos < adapter.getCount(); pos++) {
                    if (adapter.getItemId(pos) == id) {
                        output = (JSONObject) adapter.getItem(pos);
                        break;
                    }
                }
            }
            if (output != null || position < 0 || position >= adapter.getCount()) {
                return output;
            }
            return (JSONObject) adapter.getItem(position);
        } catch (Exception e) {
            Logx.log(getClass(), e);
            return null;
        }
    }

    public void onListItemClick(boolean mayDisplayAdverts) {
        DefaultListFragment fragment = getListFragment();
        onListItemClick(fragment.getSelectedPosition(), fragment.getSelectedId(), mayDisplayAdverts);
    }

    @Override
    public void onListItemClick(int position, long id, boolean mayDisplayAdverts) {

        AdvertManager advertManager = mayDisplayAdverts ? getAdvertManager(false) : null;

        displayFeed(position, id);

        if (advertManager == null) {
            return;
        }

        advertManager.displayAdvert(getAdvertBundle(position, id));
    }

    public void onListItemClick_old(int position, long id, boolean mayDisplayAdverts) {
        AdvertManager advertManager = mayDisplayAdverts ? getAdvertManager(false) : null;
        if (advertManager == null) {
            displayFeed(position, id);
            return;
        }
        switch (advertManager.displayAdvert(getAdvertBundle(position, id))) {
            case AdvertManager.ADVERT_NOT_DISPLAYED:
            case AdvertManager.ADVERT_DISPLAYED_NEXT_ACTION_NOT_INITIATED:
                displayFeed(position, id);
            case AdvertManager.ADVERT_DISPLAYED_AND_NEXT_ACTION_INITIATED:
            default:
                break;
        }
    }

    public void onAdvertDisplayFinished(Bundle bundle) {
        Logx.debug(this.getClass(), "onAdvertDisplayFinished(Bundle)");
    }

    public void displayFeed(JSONObject feedJson) {
        displayFeed(feedJson.toJSONString());
    }

    public void displayFeed(String feedJson) {
        Intent intent = new Intent(this, DisplayFeedActivity.class);
        intent.putExtra(DisplayFeedActivity.EXTRA_STRING_SELECTED_FEED_JSON, feedJson);
        displayContent(intent);
    }

    public void displayFeed(Bundle bundle) {
        if (bundle == null) {
            throw new NullPointerException();
        }
        Intent intent = new Intent(this, DisplayFeedActivity.class);
        intent.putExtras(bundle);
        displayContent(intent);
    }

    public boolean displayFeed(int position, long id) {
        JSONObject feedJson = getItemToDisplay(position, id);
        if (feedJson == null) {
            return false;
        }
        Long feedId = (Long) feedJson.get(FeedhitNames.feedid);
        if (!(feedId == null || feedId.longValue() == Feed.DEFAULT_FEED_ID || feedId.longValue() >= 2147482647)) {
            User.getInstance().addReadFeed(this, feedId);
            FeedhitManager.addFeedhit(this, feedId);
        }
        displayFeed(feedJson);
        return true;
    }

    public void scrollToRestoredTabPosition() {
        HorizontalScrollView scrolls = getTabsScrollView();
        if (scrolls != null) {
            scrolls.post(new AnonymousClass1(scrolls));
        }
    }

    public int[] getTabIds() {
        return new int[]{R.id.tabs_all, R.id.tabs_bookmarks, R.id.tabs_categories, R.id.tabs_sources, R.id.tabs_favorites, R.id.tabs_headlines, R.id.tabs_info};
    }

    @TargetApi(14)
    public void updateActiveTab(int activeTabId) {
        if (getTabIds() != null) {
            for (int tabIds : getTabIds()) {
                View tab_mayBeAnyView = findViewById(tabIds);
                boolean flag = tab_mayBeAnyView.getId() == activeTabId;
                tab_mayBeAnyView.setActivated(flag);
                if (App.isAcceptableVersion(this, 14)) {
                    tab_mayBeAnyView.setHovered(flag);
                }
                tab_mayBeAnyView.setPressed(flag);
                tab_mayBeAnyView.setSelected(flag);
            }
        }
    }

    private void setUpTabs() {
        int[] tabIds = getTabIds();
        if (tabIds != null) {
            OnClickListener listener = new TabOnClickListener(getClass());
            for (int tabId : tabIds) {
                View tab = findViewById(tabId);
                tab.setOnClickListener(listener);
                ((TextView) tab.findViewById(android.R.id.title)).setText(getTitleForTabId(tabId));
            }
        }
    }

    public String getTitleForTabId(int id) {
        int stringResourceId;
        if (id == R.id.tabs_all) {
            stringResourceId = R.string.msg_all;
        } else if (id == R.id.tabs_categories) {
            stringResourceId = R.string.msg_categories;
        } else if (id == R.id.tabs_sources) {
            stringResourceId = R.string.msg_sources;
        } else if (id == R.id.tabs_bookmarks) {
            stringResourceId = R.string.msg_bookmarks;
        } else if (id == R.id.tabs_favorites) {
            stringResourceId = R.string.msg_favorites;
        } else if (id == R.id.tabs_headlines) {
            stringResourceId = R.string.msg_headlines;
        } else if (id == R.id.tabs_info) {
            stringResourceId = R.string.msg_info;
        } else {
            Logx.log(6, getClass(), "No Activity class defined for id: " + id);
            throw new IllegalArgumentException("Unexpected id: " + id);
        }
        return getString(stringResourceId);
    }

    public HorizontalScrollView getTabsScrollView() {
        View view = findViewById(R.id.main_tabs);
        if (view instanceof HorizontalScrollView) {
            return (HorizontalScrollView) view;
        }
        if (view != null) {
            return (HorizontalScrollView) view.findViewById(R.id.tabs_scrolls);
        }
        return (HorizontalScrollView) findViewById(R.id.tabs_scrolls);
    }

    public FeedListAdapter getFeedListAdapter() {
        return getFeedListAdapter(true);
    }

    public FeedListAdapter getFeedListAdapter(boolean create) {
        if (this._lav_accessViaGetter == null && create) {
            this._lav_accessViaGetter = new FeedListAdapter(this, new ArrayList(getDisplayBufferSize()));
            this._lav_accessViaGetter.setNotifyOnChange(true);
            getListFragment().setListAdapter(this._lav_accessViaGetter);
        }
        return this._lav_accessViaGetter;
    }

    public FeedDisplayHandler getFeedDisplayHandler() {
        return getFeedDisplayHandler(true);
    }

    public FeedDisplayHandler getFeedDisplayHandler(boolean create) {
        if (this._fdh_accessViaGetter == null && create) {
            this._fdh_accessViaGetter = createDisplayHandler();
        }
        return this._fdh_accessViaGetter;
    }

    public FeedDisplayHandler createDisplayHandler() {
        Logx.log(Log.DEBUG, getClass(), "Creating display handler");
        return new AnonymousClass2(this, getListFragment().getListView(), getFeedListAdapter(), ListFragmentActivity.this);
    }

    public int getDisplayBufferSize() {
        return App.getPropertiesManager(this).getInt(PropertyName.feedDownloadLimit);
    }

    public int displayTransitionAdvert(Bundle bundle) {
        Intent advertIntent = getAdvertIntent(bundle);
        if (advertIntent == null) {
            return 3;
        }
        displayAdvert(advertIntent);
        return 2;
    }

    public void displayAdvert(Intent intent) {
        if (this.dualPaneVisible) {
            updateDetailsFragment(intent.getExtras(), createAdvertFragment());
            return;
        }
        startActivity(intent);
    }

    public void displayContent(Intent intent) {
        if (this.dualPaneVisible) {
            updateDetailsFragment(intent.getExtras(), createDetailsFragment());
            return;
        }
        startActivity(intent);
    }

    @TargetApi(13)
    public void updateDetailsFragment(Bundle bundle, WebContentFragment fragmentToDisplay) {
        Logx.debug(getClass(), "Current fragment; {0}\nTarget fragment: {1}", this.currentDetailsFragment, fragmentToDisplay);
        fragmentToDisplay.update(bundle, true);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (App.isAcceptableVersion(this, 13) && fragmentToDisplay.equals(this.currentDetailsFragment)) {
            ft.detach(this.currentDetailsFragment).attach(fragmentToDisplay);
        } else {
            ft.replace(getDetailsFragmentId(), fragmentToDisplay);
        }
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
        this.currentDetailsFragment = fragmentToDisplay;
        Logx.debug(getClass(), "Target fragment. in layout: {0}, visible: {1}, resumed: {2}", Boolean.valueOf(fragmentToDisplay.isInLayout()), Boolean.valueOf(fragmentToDisplay.isVisible()), Boolean.valueOf(fragmentToDisplay.isResumed()));
    }

    @TargetApi(17)
    public boolean isDestroyed() {
        return (App.isAcceptableVersion(this, 17) && super.isDestroyed());
    }

    private Intent getAdvertIntent(Bundle bundle) {
        Intent intent = null;
        AdvertManager advertManager = getAdvertManager(false);
        if (advertManager != null) {
            JSONObject advertData = advertManager.getRandomAdvertFeed();
            if (advertData != null) {
                String content = new Feed(advertData).getText(getString(R.string.err));
                intent = new Intent(this, DisplayAdvertActivity.class);
                intent.putExtra(DisplayAdvertActivity.EXTRA_STRING_CONTENT_TO_DISPLAY, content);
                if (!(bundle == null || bundle.isEmpty())) {
                    intent.putExtras(bundle);
                }
            }
        }
        return intent;
    }

    public DefaultListFragment getListFragment() {
        if (this._alf == null) {
            this._alf = (DefaultListFragment) getFragmentManager().findFragmentById(getListFragmentId());
        }
        return this._alf;
    }

    private View getDetailsFrame() {
        if (this._df == null) {
            this._df = findViewById(getDetailsFragmentId());
        }
        return this._df;
    }

    public boolean isDualPaneMode() {
        return this.dualPaneMode;
    }

    public boolean isDualPaneVisible() {
        return this.dualPaneVisible;
    }
}
