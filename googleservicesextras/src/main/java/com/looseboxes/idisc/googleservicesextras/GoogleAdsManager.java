package com.looseboxes.idisc.googleservicesextras;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.bc.android.core.util.Geo;
import com.bc.android.core.util.Logx;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.drive.events.CompletionEvent;
import com.google.android.gms.plus.model.people.Person;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.activities.DisplayFeedActivity;
import com.looseboxes.idisc.common.activities.ListFragmentActivity;
import com.looseboxes.idisc.common.fragments.DefaultListFragment.JsonListItemClickHandler;
import com.looseboxes.idisc.common.util.AdvertDisplayFinishedListener;
import com.looseboxes.idisc.common.util.AdvertManager;
import com.looseboxes.idisc.common.util.NewsminuteUtil;

import java.util.Locale;

public class GoogleAdsManager extends AdvertManager {
    private AdView _av;
    private InterstitialAd _ia;
    private boolean bannerAdSetupCompleted;
    private boolean interstitialAdSetupCompleted;
    private Location location;

    private class LoadInterstitialAdTask implements Runnable {
        private final InterstitialAd interstitialAd;

        public LoadInterstitialAdTask(InterstitialAd interstitialAd) {
            this.interstitialAd = interstitialAd;
        }

        public void run() {
            long tb4 = System.currentTimeMillis();
            this.interstitialAd.loadAd(GoogleAdsManager.this.getAdRequest());
            Logx.getInstance().log(Log.DEBUG, getClass(), "Time spent: {0}", Long.valueOf(System.currentTimeMillis() - tb4));
        }
    }

    private class InterstitialAdListener extends AdListener {
        private Bundle bundle;

        public void onAdClosed() {
            GoogleAdsManager.this.requestNewInterstitialAd();
            onAdvertDisplayFinished();
        }

        public void onAdFailedToLoad(int errorCode) {
        }

        private void onAdvertDisplayFinished() {
            Activity activity = GoogleAdsManager.this.getActivity();
            try {
                AdvertDisplayFinishedListener listener = (AdvertDisplayFinishedListener) activity;
                listener.onAdvertDisplayFinished(this.bundle);
            } catch (ClassCastException e) {
                Logx.getInstance().log(this.getClass(), e);
                throw new ClassCastException(activity + " must implement " + JsonListItemClickHandler.class.getName());
            }
        }
    }

    public GoogleAdsManager(Activity activity) {
        super(activity);
    }

    public int getDefaultAdvertType() {
        return 2;
    }

    public void onCreate(Bundle savedInstanceState) {
        try {
            Logx.getInstance().log(Log.VERBOSE, getClass(), "Entering: {0}#onCreate(android.os.Bundle)", getClass().getName());
            requestNewAd();
            AdView adView = getAdView(false);
            if (adView != null) {
                this.bannerAdSetupCompleted = layoutBannerAd(adView);
            }
            InterstitialAd interstitialAd = getInterstitialAd(false);
            if (interstitialAd != null) {
                this.interstitialAdSetupCompleted = addAdListener(interstitialAd);
            }
            Logx.getInstance().log(Log.VERBOSE, getClass(), "Exiting: {0}#onCreate(android.os.Bundle)", getClass().getName());
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    public void onPause() {
        try {
            Logx.getInstance().log(2, getClass(), "Entering: {0}#onPause", getClass().getName());
            super.onPause();
            AdView adView = getAdView(false);
            if (adView != null) {
                adView.pause();
            }
            Logx.getInstance().log(2, getClass(), "Exiting: {0}#onPause", getClass().getName());
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    public void onResume() {
        try {
            Logx.getInstance().log(2, getClass(), "Entering: {0}#onResume", getClass().getName());
            super.onResume();
            AdView adView = getAdView(false);
            if (adView != null) {
                adView.resume();
            }
            Logx.getInstance().log(2, getClass(), "Exiting: {0}#onResume", getClass().getName());
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    public void onDestroy() {
        try {
            Logx.getInstance().log(2, getClass(), "Entering: {0}#onDestroy", getClass().getName());
            super.onDestroy();
            AdView adView = getAdView(false);
            if (adView != null) {
                adView.destroy();
            }
            Logx.getInstance().log(2, getClass(), "Exiting: {0}#onDestroy", getClass().getName());
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    public void requestNewAd() {
        switch (getAdvertTypeToDisplay()) {
            case CompletionEvent.STATUS_CONFLICT /*2*/:
                requestNewBannerAd();
            case CompletionEvent.STATUS_CANCELED /*3*/:
                requestNewInterstitialAd();
            default:
        }
    }

    public void requestNewBannerAd() {
        try {
            AdView adView = getAdView(true);
            Logx.getInstance().debug(getClass(), "Banner ad is already loading: {0} for activity: {1}", Boolean.valueOf(adView.isLoading()), getActivity().getClass().getName());
            if (!adView.isLoading()) {
                adView.loadAd(getAdRequest());
            }
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    public void requestNewInterstitialAd() {
        try {
            InterstitialAd interstitialAd = getInterstitialAd(true);
            Logx.getInstance().debug(getClass(), "Interstitial ad is already, loaded: {0}, loading: {1}, for activity: {2}", Boolean.valueOf(interstitialAd.isLoaded()), Boolean.valueOf(interstitialAd.isLoading()), getActivity().getClass().getName());
            if (!interstitialAd.isLoading()) {
                NewsminuteUtil.runAsync(new LoadInterstitialAdTask(interstitialAd));
            }
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    private AdRequest getAdRequest() {
        try {
            Builder adBuilder = new Builder();
            if (Logx.getInstance().isDebugMode()) {
                adBuilder = adBuilder.addTestDevice(Logx.getInstance().TEST_DEVICE_ID);
            }
            return addTargeting(adBuilder).build();
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
            return null;
        }
    }

    private Builder addTargeting(Builder adBuilder) {
        try {
            if (this.location == null) {
                Geo geo = new Geo();
                this.location = geo.getLocation(this.getActivity(), geo.getDefaultLocation());
                Logx.getInstance().debug(this.getClass(), "Location: {0}", location);
            }
            if (this.location != null) {
                adBuilder = adBuilder.setLocation(this.location);
            }
            String gender = User.getInstance().getGender(getActivity(), null);
            if (gender == null) {
                return adBuilder;
            }
            switch (gender.toLowerCase()) {
                case "female":
                    adBuilder.setGender(Person.Gender.FEMALE);
                    break;
                case "male":
                    adBuilder.setGender(Person.Gender.MALE);
                    break;
            }
        } catch (Exception e) {
            Logx.getInstance().log(GoogleAdsManager.class, e);
        }
        return adBuilder;
    }

    public int displayAdvert(int type, Bundle bundle) {
        int output = 1;
        boolean displayed = false;
        switch (type) {
            case CompletionEvent.STATUS_CONFLICT /*2*/:
                if (getAdView(false) != null && this.bannerAdSetupCompleted) {
                    displayed = true;
                }
                if (!displayed) {
                    output = 3;
                }
                return output;
            case CompletionEvent.STATUS_CANCELED /*3*/:
                return displayInterstitialAdvert(bundle);
            default:
                return super.displayAdvert(type, bundle);
        }
    }

    public String getInterstitialAdId() {
        return getInterstitialAdId(getActivity());
    }

    public static String getInterstitialAdId(Context context) {
        if (Logx.getInstance().isDebugMode()) {
            return context.getString(R.string.ad_unit_id_interstitial_before_displayfeeds_test);
        }
        return context.getString(R.string.ad_unit_id_interstitial_before_displayfeeds);
    }

    public String getBannerAdId() {
        return getBannerAdId(getActivity());
    }

    public static String getBannerAdId(Context context) {
        if (Logx.getInstance().isDebugMode()) {
            return context.getString(R.string.ad_unit_id_banner_before_displayfeeds_test);
        }
        return context.getString(R.string.ad_unit_id_banner_before_displayfeeds);
    }

    public InterstitialAd getInterstitialAd(boolean createInNotExists) {
        if (this._ia == null && createInNotExists) {
            this._ia = new InterstitialAd(getActivity());
            this._ia.setAdUnitId(getInterstitialAdId());
        }
        return this._ia;
    }

    public AdView getAdView(boolean createInNotExists) {
        if (this._av == null && createInNotExists) {
            this._av = new AdView(getActivity());
            this._av.setAdSize(AdSize.SMART_BANNER);
            this._av.setAdUnitId(getBannerAdId());
        }
        return this._av;
    }

    private boolean layoutBannerAd(AdView adView) {
        try {
            int layoutResId;
            this.bannerAdSetupCompleted = false;
            Activity activity = getActivity();
            if (activity instanceof DisplayFeedActivity) {
                layoutResId = R.id.feedview_root_linearlayout;
            } else {
                layoutResId = R.id.main_root_linearlayout;
            }
            LinearLayout linearLayout = (LinearLayout) activity.findViewById(layoutResId);
            Logx.getInstance().debug(getClass(), "Activity: {0}, LinearLayout: {1}", activity.getClass().getName(), linearLayout);
            if (linearLayout != null) {
                if (linearLayout.indexOfChild(adView) == -1) {
                    linearLayout.addView(adView);
                }
                this.bannerAdSetupCompleted = true;
            }
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
        return this.bannerAdSetupCompleted;
    }

    private boolean addAdListener(InterstitialAd interstitialAd) {
        try {
            this.interstitialAdSetupCompleted = false;
            AdListener adListener = createInterstitialAdListener();
            Logx.getInstance().debug(getClass(), "Activity: {0}, AdListener: {1}", getActivity().getClass().getName(), adListener);
            if (adListener != null) {
                interstitialAd.setAdListener(adListener);
                this.interstitialAdSetupCompleted = true;
            }
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
        return this.interstitialAdSetupCompleted;
    }

    private AdListener createInterstitialAdListener() {
        if (getActivity() instanceof ListFragmentActivity) {
            return new InterstitialAdListener();
        }
        return null;
    }

    private int displayInterstitialAdvert(Bundle bundle) {
        InterstitialAd interstitialAd = getInterstitialAd(false);
        if (interstitialAd != null && this.interstitialAdSetupCompleted && interstitialAd.isLoaded()) {
            ((InterstitialAdListener) interstitialAd.getAdListener()).bundle = bundle;
            interstitialAd.show();
            requestNewInterstitialAd();
            return 2;
        }
        requestNewInterstitialAd();
        return 3;
    }
}
