package com.looseboxes.idisc.common.util;

import android.app.Activity;
import android.os.Bundle;

import com.bc.android.core.util.Logx;
import com.bc.util.Util;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdvertManager {
    public static final int ADVERT_DISPLAYED_AND_NEXT_ACTION_INITIATED = 2;
    public static final int ADVERT_DISPLAYED_NEXT_ACTION_NOT_INITIATED = 1;
    public static final int ADVERT_NOT_DISPLAYED = 3;
    public static final int BANNER_ADVERT = 2;
    public static final int INTERSTITIAL_ADVERT = 3;
    public static final int LOCAL_ADVERT = 1;
    public static final int NONE = -1;
    private int[] _at;
    private Activity activity;
    private int advertTypeToDisplay;
    private float bannerAddFreq;
    private float interstitialAddFreq;
    private float localAddFreq;

    public AdvertManager(Activity activity) {
        this.advertTypeToDisplay = NONE;
        setActivity(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public void onDestroy() {
    }

    public int[] getAdvertTypes() {
        if (this._at == null) {
            this._at = new int[]{LOCAL_ADVERT, BANNER_ADVERT, INTERSTITIAL_ADVERT};
        }
        return this._at;
    }

    public int getDefaultAdvertType() {
        return BANNER_ADVERT;
    }

    public int getAdvertTypeToDisplay() {
        if (this.advertTypeToDisplay == NONE) {
            this.advertTypeToDisplay = getDefaultAdvertType();
            double random = Math.random();
            float freq = getAdvertFrequency();
            float pos = 0.0f;
            int[] arr$ = getAdvertTypes();
            int len$ = arr$.length;
            for (int i$ = 0; i$ < len$; i$ += LOCAL_ADVERT) {
                int type = arr$[i$];
                pos += getAdvertFrequency(type) / freq;
                if (random < ((double) pos)) {
                    this.advertTypeToDisplay = type;
                    Logx.getInstance().debug(getClass(), "Updated advert type to display to: " + type);
                    break;
                }
            }
        }
        return this.advertTypeToDisplay;
    }

    public final int displayAdvert(Bundle bundle) {
        if (!isToDisplayAdvert()) {
            return INTERSTITIAL_ADVERT;
        }
        int type = getAdvertTypeToDisplay();
        this.advertTypeToDisplay = NONE;
        return displayAdvert(type, bundle);
    }

    public int displayAdvert(int type, Bundle bundle) {
        switch (type) {
            case LOCAL_ADVERT /*1*/:
                return displayLocalTransitionAdvert(bundle);
            case BANNER_ADVERT /*2*/:
            case INTERSTITIAL_ADVERT /*3*/:
                return INTERSTITIAL_ADVERT;
            default:
                throw new IllegalArgumentException("Unexpected advert type int: " + type);
        }
    }

    public boolean isToDisplayAdvert() {
        return Math.random() < ((double) getAdvertFrequency());
    }

    public float getAdvertFrequency() {
        PropertiesManager props = App.getPropertiesManager(this.activity);
        if (User.getInstance().isActivated(this.activity)) {
            return 0.0f;
        }
        return (this.localAddFreq + this.bannerAddFreq) + this.interstitialAddFreq;
    }

    public float getAdvertFrequency(int type) {
        switch (type) {
            case LOCAL_ADVERT /*1*/:
                return this.localAddFreq;
            case BANNER_ADVERT /*2*/:
                return this.bannerAddFreq;
            case INTERSTITIAL_ADVERT /*3*/:
                return this.interstitialAddFreq;
            default:
                throw new IllegalArgumentException("Unexpected advert type int: " + type);
        }
    }

    private int displayLocalTransitionAdvert(Bundle bundle) {
        if (this.activity instanceof LocalTransitionAdvertDisplay) {
            return ((LocalTransitionAdvertDisplay)this.activity).displayTransitionAdvert(bundle);
        }
        return INTERSTITIAL_ADVERT;
    }

    public JSONObject getRandomAdvertFeed() {
        try {
            return getRandomAdvertFeedFromCachedDownloads(App.getPropertiesManager(this.activity).getString(PropertyName.advertCategoryName));
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
            return null;
        }
    }

    private JSONObject getRandomAdvertFeedFromCachedDownloads(String catName) {
        List<JSONObject> download = FeedDownloadManager.getDownload(this.activity);
        if (download == null || download.isEmpty()) {
            return null;
        }
        List<JSONObject> adverts = new ArrayList();
        for (JSONObject json : download) {
            Object categories = json.get(FeedNames.categories);
            if (categories != null && categories.toString().contains(catName)) {
                adverts.add(json);
            }
        }
        if (adverts.isEmpty()) {
// For testing only
//            Activity activity = this.getActivity();
//            DefaultApplication app = (DefaultApplication)activity.getApplication();
//            final long feedid = app.getSharedContext().getFeedidsService().getFeedids().getUpdateNoticeFeedid();
//            return new DefaultJsonObject(this.getActivity(), feedid, "Jesus is Lord", "<html><h3>Jesus is Lord</h3><img alt=\"Rock Art\" src=\"http://christsaves.deviantart.com/art/The-Lord-is-my-rock-431357799\"/><p>Time is coming when all shall proclaim - Jesus is Lord!</p></html>", catName);
            return null;
        }
        return adverts.get(Util.randomInt(adverts.size()));
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        PropertiesManager props = App.getPropertiesManager(activity);
        this.localAddFreq = props.getFloat(PropertyName.advertFrequencyLocalAds);
        this.bannerAddFreq = props.getFloat(PropertyName.advertFrequencyBannerAds);
        this.interstitialAddFreq = props.getFloat(PropertyName.advertFrequencyInterstitialAds);
    }

    public Activity getActivity() {
        return this.activity;
    }

    public float getInterstitialAddFreq() {
        return this.interstitialAddFreq;
    }

    public float getBannerAddFreq() {
        return this.bannerAddFreq;
    }

    public float getLocalAddFreq() {
        return this.localAddFreq;
    }
}
