package com.looseboxes.idisc.common.activities;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ListAdapter;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.fragments.DefaultListFragment;
import com.looseboxes.idisc.common.fragments.DefaultListFragment.JsonListItemClickHandler;
import com.looseboxes.idisc.common.fragments.DisplayAdvertFragment;
import com.looseboxes.idisc.common.fragments.WebContentFragment;
import com.looseboxes.idisc.common.handlers.FeedDetailsDisplayHandler;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.listeners.TabOnClickListener;
import com.looseboxes.idisc.common.util.AdvertDisplayFinishedListener;
import com.looseboxes.idisc.common.util.AdvertManager;
import com.looseboxes.idisc.common.util.LocalTransitionAdvertDisplay;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.Set;

public abstract class ListFragmentActivity extends AdsActivity
        implements JsonListItemClickHandler, LocalTransitionAdvertDisplay, AdvertDisplayFinishedListener {

    private boolean dualPaneMode;
    private boolean dualPaneVisible;

    private DefaultListFragment listFragment;
    private FrameLayout detailsFrame;
    private WebContentFragment currentDetailsFragment;

    private FeedDetailsDisplayHandler feedDetailsDisplayHandler;

    private class FeedDetailsDisplayHandlerImpl extends FeedDetailsDisplayHandler {
        public FeedDetailsDisplayHandlerImpl(ListAdapter listAdapter) {
            super(ListFragmentActivity.this, listAdapter);
        }
        @Override
        public void displayContent(Intent intent) {
            ListFragmentActivity.this.displayContent(intent);
        }
    }

    class ScrollToCurrentTab implements Runnable {
        final HorizontalScrollView val$scrolls;

        ScrollToCurrentTab(HorizontalScrollView horizontalScrollView) {
            this.val$scrolls = horizontalScrollView;
        }

        public void run() {
            try {
                int tabBtnId = ListFragmentActivity.this.getTabId();
                if (tabBtnId >= 0) {
                    this.val$scrolls.smoothScrollTo(ListFragmentActivity.this.findViewById(tabBtnId).getLeft(), 0);
                }
            } catch (Exception e) {
                Logx.getInstance().log(getClass(), e);
            }
        }
    }

    protected abstract WebContentFragment createDetailsFragment();

    public abstract int getDetailsFragmentId();

    public abstract int getListFragmentId();

    public abstract int getTabId();

    protected DisplayAdvertFragment createAdvertFragment() {
        return new DisplayAdvertFragment();
    }

    @Override
    public JSONObject getItemToScrollToOnResume() {
        return null;
    }

    @Override
    @CallSuper
    protected void doCreate(Bundle savedInstanceState) {

        super.doCreate(savedInstanceState);

        this.listFragment = (DefaultListFragment) getFragmentManager().findFragmentById(getListFragmentId());
        this.detailsFrame = (FrameLayout)findViewById(getDetailsFragmentId());
        this.feedDetailsDisplayHandler = new FeedDetailsDisplayHandlerImpl(listFragment.getListAdapter());

        this.setUpTabs();
    }

    @Override
    @CallSuper
    protected void onResume() {
        try {
            updateDualPaneVisibility();
            super.onResume();
            updateActiveTab(getTabId());
            scrollToRestoredTabPosition();
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(this.feedDetailsDisplayHandler != null) {
            this.feedDetailsDisplayHandler.destroy();
        }
    }

    private void updateDualPaneVisibility() {
        final View detailsFrame = getDetailsFrame();
        this.dualPaneMode = detailsFrame != null;
        this.dualPaneVisible = !(detailsFrame == null || detailsFrame.getVisibility() != View.VISIBLE);
        DefaultApplication app = (DefaultApplication) getApplication();
        app.setDualPaneMode(this.dualPaneMode);
        app.setDualPaneVisible(this.dualPaneVisible);
        if (!this.dualPaneMode) {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(getDetailsFragmentId());
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commit();
            }
        }
    }

    public Set<String> getSearchPhrases() {
        return Collections.EMPTY_SET;
    }

    public void loadNewer() { }

    @Override
    public void onExceedTop() {
        loadNewer();
    }

    @Override
    public void onReachedTop() { }

    @Override
    public void onScrollStopped() { }

    @Override
    public void onReachedBottom() {  }

    @Override
    public void onExceedBottom() { }

    public String getShareText() {
        String appName = getString(R.string.app_label);
        String appPackageName = getApplication().getPackageName();
        int numberOfFeedSources = App.getPropertiesManager(this).getMap(PropertyName.sources).size();
        String downloadUrl = "https://play.google.com/store/apps/details?id=" + appPackageName;
        Logx.getInstance().debug(getClass(), "Share content: {0}", getString(R.string.msg_trythisapp, new Object[]{appName, Integer.valueOf(numberOfFeedSources), downloadUrl}));
        return getString(R.string.msg_trythisapp, new Object[]{appName, Integer.valueOf(numberOfFeedSources), downloadUrl});
    }

    public Bundle getAdvertBundle(int position, long id) {
        Bundle bundle;
        JSONObject feedData = this.feedDetailsDisplayHandler.getItem(position, id);
        if (feedData == null || feedData.isEmpty()) {
            bundle = null;
        }else {
            bundle = new Bundle();
            bundle.putString(DisplayFeedActivity.EXTRA_STRING_SELECTED_FEED_JSON, feedData.toJSONString());
        }
        return bundle;
    }

    @Override
    public void onListItemClick(int position, long id, boolean mayDisplayAdverts) {

        AdvertManager advertManager = mayDisplayAdverts ? getAdvertManager(false) : null;

        feedDetailsDisplayHandler.displayFeed(position, id);

        if (advertManager == null) {
            return;
        }

        Bundle bundle = getAdvertBundle(position, id);

        if(bundle == null) {
            return;
        }

        advertManager.displayAdvert(bundle);
    }

    public void onAdvertDisplayFinished(Bundle bundle) {
        Logx.getInstance().debug(this.getClass(), "onAdvertDisplayFinished(Bundle)");
    }

    public void scrollToRestoredTabPosition() {
        HorizontalScrollView scrolls = getTabsScrollView();
        if (scrolls != null) {
            scrolls.post(new ScrollToCurrentTab(scrolls));
        }
    }

    public int[] getTabIds() {
        return new int[]{R.id.tabs_all, R.id.tabs_bookmarks, R.id.tabs_categories, R.id.tabs_sources, R.id.tabs_favorites, R.id.tabs_headlines, R.id.tabs_info};
    }

    public void updateActiveTab(int activeTabId) {
        if (getTabIds() != null) {
            for (int tabIds : getTabIds()) {
                View tab_mayBeAnyView = findViewById(tabIds);
                boolean flag = tab_mayBeAnyView.getId() == activeTabId;
                tab_mayBeAnyView.setActivated(flag);
                if (App.isAcceptableVersion(this, 14)) {
                    this.setHovered_v14(tab_mayBeAnyView, flag);
                }
                tab_mayBeAnyView.setPressed(flag);
                tab_mayBeAnyView.setSelected(flag);
            }
        }
    }

    @TargetApi(14)
    private void setHovered_v14(View view, boolean flag) {
        view.setHovered(flag);
    }

    private void setUpTabs() {
        try {
            int[] tabIds = getTabIds();
            if (tabIds != null) {
                OnClickListener listener = new TabOnClickListener(getClass());
                for (int tabId : tabIds) {
                    View tab = findViewById(tabId);
                    tab.setOnClickListener(listener);
                }
            }
        }catch(Exception e) {
            Logx.getInstance().log(this.getClass(), e);
        }
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

    public void updateDetailsFragment(Bundle bundle, WebContentFragment fragmentToDisplay) {
        Logx.getInstance().debug(getClass(), "Current fragment; {0}\nTarget fragment: {1}", this.currentDetailsFragment, fragmentToDisplay);
        fragmentToDisplay.update(bundle, true);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (App.isAcceptableVersion(this, 13) && fragmentToDisplay.equals(this.currentDetailsFragment)) {
            this.replace_v13(ft, fragmentToDisplay);
        } else {
            ft.replace(getDetailsFragmentId(), fragmentToDisplay);
        }
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
        this.currentDetailsFragment = fragmentToDisplay;
        Logx.getInstance().debug(getClass(), "Target fragment. in layout: {0}, visible: {1}, resumed: {2}", Boolean.valueOf(fragmentToDisplay.isInLayout()), Boolean.valueOf(fragmentToDisplay.isVisible()), Boolean.valueOf(fragmentToDisplay.isResumed()));
    }

    @TargetApi(13)
    private void replace_v13(FragmentTransaction ft, WebContentFragment fragmentToDisplay) {
        ft.detach(this.currentDetailsFragment).attach(fragmentToDisplay);
    }

    public boolean isDestroyed() {
        return (App.isAcceptableVersion(this, 17) ? this.isDestroyed_v17() : false);
    }

    @TargetApi(17)
    private boolean isDestroyed_v17() {
        return super.isDestroyed();
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
        return this.listFragment;
    }

    public FrameLayout getDetailsFrame() {
        return this.detailsFrame;
    }

    public boolean isDualPaneMode() {
        return this.dualPaneMode;
    }

    public boolean isDualPaneVisible() {
        return this.dualPaneVisible;
    }

    public FeedDetailsDisplayHandler getFeedDetailsDisplayHandler() {
        return feedDetailsDisplayHandler;
    }
}
