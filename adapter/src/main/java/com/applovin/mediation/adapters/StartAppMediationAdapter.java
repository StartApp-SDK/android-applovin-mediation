package com.applovin.mediation.adapters;

import static com.applovin.mediation.adapter.MaxAdapter.InitializationStatus.INITIALIZED_FAILURE;
import static com.applovin.mediation.adapter.MaxAdapter.InitializationStatus.INITIALIZED_SUCCESS;
import static com.applovin.mediation.adapter.MaxAdapterError.INTERNAL_ERROR;
import static com.applovin.mediation.adapter.MaxAdapterError.NO_FILL;
import static com.startapp.adapter.applovin.BuildConfig.DEBUG;
import static com.startapp.adapter.applovin.BuildConfig.VERSION_NAME;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.impl.mediation.MaxRewardImpl;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.adapter.MaxAdViewAdapter;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.MaxInterstitialAdapter;
import com.applovin.mediation.adapter.MaxNativeAdAdapter;
import com.applovin.mediation.adapter.MaxRewardedAdapter;
import com.applovin.mediation.adapter.MaxRewardedInterstitialAdapter;
import com.applovin.mediation.adapter.MaxSignalProvider;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxNativeAdAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxRewardedAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxRewardedInterstitialAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxSignalCollectionListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterInitializationParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterSignalCollectionParameters;
import com.applovin.sdk.AppLovinSdk;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerBase;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.banner.Mrec;
import com.startapp.sdk.ads.banner.banner3d.Banner3D;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;
import com.startapp.sdk.adsbase.model.AdPreferences;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Keep
public class StartAppMediationAdapter extends MediationAdapterBase implements MaxInterstitialAdapter, MaxRewardedInterstitialAdapter, MaxRewardedAdapter, MaxAdViewAdapter, MaxNativeAdAdapter, MaxSignalProvider {
    private static final String LOG_TAG = StartAppMediationAdapter.class.getSimpleName();

    private static final String APP_ID = "app_id";
    private static final String NETWORK_NAME = "network_name";
    private static final String IS_MUTED = "is_muted";
    private static final String AD_TAG = "adTag";
    private static final String INTERSTITIAL_MODE = "interstitialMode";
    private static final String MIN_CPM = "minCPM";
    private static final String IS_3D_BANNER = "is3DBanner";
    private static final String NATIVE_IMAGE_SIZE = "nativeImageSize";
    private static final String NATIVE_SECONDARY_IMAGE_SIZE = "nativeSecondaryImageSize";

    @NonNull
    private static final Object lock = new Object();

    @Nullable
    private static String initializedAppId;

    @Nullable
    private static String initializedAdUnit;

    @Nullable
    private static Map<String, StartAppAd> interstitials;

    @Nullable
    private static Map<String, StartAppAd> rewardedVideos;

    @Keep
    public StartAppMediationAdapter(AppLovinSdk sdk) {
        super(sdk);

        if (DEBUG) {
            Log.v(LOG_TAG, "constructor");
        }
    }

    @Override
    public void initialize(MaxAdapterInitializationParameters parameters, Activity activity, OnCompletionListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "initialize");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null) {
            listener.onCompletion(INITIALIZED_FAILURE, null);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, "initialize: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onCompletion(INITIALIZED_FAILURE, null);
            return;
        }

        if (DEBUG) {
            Log.v(LOG_TAG, "initialize: " + parameters.getAdUnitId());
            Log.v(LOG_TAG, "initialize: " + parameters.getServerParameters());
            Log.v(LOG_TAG, "initialize: " + parameters.getCustomParameters());
        }

        listener.onCompletion(INITIALIZED_SUCCESS, null);
    }

    @Override
    public String getSdkVersion() {
        if (DEBUG) {
            Log.v(LOG_TAG, "getSdkVersion");
        }

        return StartAppSDK.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        if (DEBUG) {
            Log.v(LOG_TAG, "getAdapterVersion");
        }

        return VERSION_NAME;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.v(LOG_TAG, "onDestroy");
        }
    }

    @Override
    public void loadAdViewAd(
            @Nullable MaxAdapterResponseParameters parameters,
            @Nullable final MaxAdFormat format,
            @Nullable final Activity activity,
            @Nullable final MaxAdViewAdapterListener listener
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "loadAdViewAd");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || format == null || activity == null) {
            listener.onAdViewAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, "loadAdViewAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onAdViewAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        if (DEBUG) {
            Log.v(LOG_TAG, "loadAdViewAd: " + parameters.getAdUnitId());
            Log.v(LOG_TAG, "loadAdViewAd: " + parameters.getServerParameters());
            Log.v(LOG_TAG, "loadAdViewAd: " + parameters.getCustomParameters());
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onAdViewAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        final AdPreferences adPreferences = createAdPreferences(parameters);

        final Bundle customParameters = parameters.getCustomParameters();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BannerListener bannerListener = new BannerListener() {
                    @Override
                    public void onReceiveAd(View view) {
                        if (DEBUG) {
                            Log.v(LOG_TAG, "loadAdViewAd: onReceiveAd: " + Thread.currentThread());
                        }

                        // listener.onAdViewAdLoaded(banner);
                    }

                    @Override
                    public void onFailedToReceiveAd(View view) {
                        if (DEBUG) {
                            Log.v(LOG_TAG, "loadAdViewAd: onFailedToReceiveAd");
                        }

                        listener.onAdViewAdDisplayFailed(resolveError(view));
                    }

                    @Override
                    public void onImpression(View view) {
                        // none
                    }

                    @Override
                    public void onClick(View view) {
                        listener.onAdViewAdClicked();
                    }
                };

                final BannerBase banner;
                final int width;
                final int height;

                if (format == MaxAdFormat.BANNER) {
                    width = 320;
                    height = 50;

                    if (customParameters != null && customParameters.getBoolean(IS_3D_BANNER)) {
                        banner = new Banner3D(activity, adPreferences, bannerListener);
                    } else {
                        banner = new Banner(activity, adPreferences, bannerListener);
                    }
                } else if (format == MaxAdFormat.MREC) {
                    width = 300;
                    height = 250;

                    banner = new Mrec(activity, adPreferences, bannerListener);
                } else {
                    listener.onAdViewAdLoadFailed(INTERNAL_ERROR);
                    return;
                }

                banner.loadAd(width, height);
                listener.onAdViewAdLoaded(banner);

                if (DEBUG) {
                    Log.v(LOG_TAG, "loadAdViewAd: done");
                }
            }
        });
    }

    @Override
    public void loadInterstitialAd(
            @Nullable MaxAdapterResponseParameters parameters,
            @Nullable Activity activity,
            @Nullable final MaxInterstitialAdapterListener listener
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "loadInterstitialAd");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || activity == null) {
            listener.onInterstitialAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, "loadInterstitialAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onInterstitialAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        if (DEBUG) {
            Log.v(LOG_TAG, "loadInterstitialAd: " + parameters.getAdUnitId());
            Log.v(LOG_TAG, "loadInterstitialAd: " + parameters.getServerParameters());
            Log.v(LOG_TAG, "loadInterstitialAd: " + parameters.getCustomParameters());
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            listener.onInterstitialAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onInterstitialAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        StartAppAd ad = null;

        synchronized (lock) {
            if (interstitials == null) {
                interstitials = new HashMap<>();
            } else {
                ad = interstitials.get(adUnitId);
            }

            if (ad == null) {
                ad = new StartAppAd(activity.getApplicationContext());

                interstitials.put(adUnitId, ad);
            }
        }

        StartAppAd.AdMode adMode = StartAppAd.AdMode.AUTOMATIC;

        Bundle customParameters = parameters.getCustomParameters();
        if (customParameters != null) {
            String adModeString = customParameters.getString(INTERSTITIAL_MODE);
            if (adModeString != null) {
                switch (adModeString.toLowerCase(Locale.ENGLISH)) {
                    case "overlay":
                        // noinspection deprecation
                        adMode = StartAppAd.AdMode.OVERLAY;
                        break;

                    case "video":
                        adMode = StartAppAd.AdMode.VIDEO;
                        break;

                    case "offerwall":
                        adMode = StartAppAd.AdMode.OFFERWALL;
                        break;
                }
            }
        }

        ad.loadAd(adMode, createAdPreferences(parameters), new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                listener.onInterstitialAdLoaded();
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                listener.onInterstitialAdLoadFailed(resolveError(ad));
            }
        });
    }

    @Override
    public void showInterstitialAd(
            @Nullable MaxAdapterResponseParameters parameters,
            @Nullable Activity activity,
            @Nullable final MaxInterstitialAdapterListener listener
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "showInterstitialAd");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || activity == null) {
            listener.onInterstitialAdDisplayFailed(INTERNAL_ERROR);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, "showInterstitialAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onInterstitialAdDisplayFailed(INTERNAL_ERROR);
            return;
        }

        if (DEBUG) {
            Log.v(LOG_TAG, "showInterstitialAd: " + parameters.getAdUnitId());
            Log.v(LOG_TAG, "showInterstitialAd: " + parameters.getServerParameters());
            Log.v(LOG_TAG, "showInterstitialAd: " + parameters.getCustomParameters());
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            listener.onInterstitialAdDisplayFailed(INTERNAL_ERROR);
            return;
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onInterstitialAdDisplayFailed(INTERNAL_ERROR);
            return;
        }

        StartAppAd ad = null;

        synchronized (lock) {
            if (interstitials != null) {
                ad = interstitials.remove(adUnitId);
            }
        }

        if (ad == null) {
            listener.onInterstitialAdDisplayFailed(INTERNAL_ERROR);
            return;
        }

        ad.showAd(new AdDisplayListener() {
            @Override
            public void adHidden(Ad ad) {
                listener.onInterstitialAdHidden();
            }

            @Override
            public void adDisplayed(Ad ad) {
                listener.onInterstitialAdDisplayed();
            }

            @Override
            public void adClicked(Ad ad) {
                listener.onInterstitialAdClicked();
            }

            @Override
            public void adNotDisplayed(Ad ad) {
                listener.onInterstitialAdDisplayFailed(resolveError(ad));
            }
        });
    }

    @Override
    public void loadRewardedAd(
            @Nullable MaxAdapterResponseParameters parameters,
            @Nullable Activity activity,
            @Nullable final MaxRewardedAdapterListener listener
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "loadRewardedAd");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || activity == null) {
            listener.onRewardedAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, "loadRewardedAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onRewardedAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        if (DEBUG) {
            Log.v(LOG_TAG, "loadRewardedAd: " + parameters.getAdUnitId());
            Log.v(LOG_TAG, "loadRewardedAd: " + parameters.getServerParameters());
            Log.v(LOG_TAG, "loadRewardedAd: " + parameters.getCustomParameters());
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            listener.onRewardedAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onRewardedAdLoadFailed(INTERNAL_ERROR);
            return;
        }

        StartAppAd ad = null;

        synchronized (lock) {
            if (rewardedVideos == null) {
                rewardedVideos = new HashMap<>();
            } else {
                ad = rewardedVideos.get(adUnitId);
            }

            if (ad == null) {
                ad = new StartAppAd(activity.getApplicationContext());

                rewardedVideos.put(adUnitId, ad);
            }
        }

        AdPreferences adPreferences = createAdPreferences(parameters);
        adPreferences.setType(Ad.AdType.REWARDED_VIDEO);

        ad.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, adPreferences, new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                listener.onRewardedAdLoaded();
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                listener.onRewardedAdLoadFailed(resolveError(ad));
            }
        });
    }

    @Override
    public void showRewardedAd(
            @Nullable MaxAdapterResponseParameters parameters,
            @Nullable Activity activity,
            @Nullable final MaxRewardedAdapterListener listener
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "showRewardedAd");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || activity == null) {
            listener.onRewardedAdDisplayFailed(INTERNAL_ERROR);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, "showRewardedAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onRewardedAdDisplayFailed(INTERNAL_ERROR);
            return;
        }

        if (DEBUG) {
            Log.v(LOG_TAG, "showRewardedAd: " + parameters.getAdUnitId());
            Log.v(LOG_TAG, "showRewardedAd: " + parameters.getServerParameters());
            Log.v(LOG_TAG, "showRewardedAd: " + parameters.getCustomParameters());
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            listener.onRewardedAdDisplayFailed(INTERNAL_ERROR);
            return;
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onRewardedAdDisplayFailed(INTERNAL_ERROR);
            return;
        }

        StartAppAd ad = null;

        synchronized (lock) {
            if (rewardedVideos != null) {
                ad = rewardedVideos.remove(adUnitId);
            }
        }

        if (ad == null) {
            listener.onRewardedAdDisplayFailed(INTERNAL_ERROR);
            return;
        }

        ad.setVideoListener(new VideoListener() {
            @Override
            public void onVideoCompleted() {
                if (DEBUG) {
                    Log.v(LOG_TAG, "showRewardedAd: onVideoCompleted");
                }

                listener.onUserRewarded(MaxRewardImpl.createDefault());
            }
        });

        ad.showAd(new AdDisplayListener() {
            @Override
            public void adHidden(Ad ad) {
                listener.onRewardedAdHidden();
            }

            @Override
            public void adDisplayed(Ad ad) {
                listener.onRewardedAdDisplayed();
            }

            @Override
            public void adClicked(Ad ad) {
                listener.onRewardedAdClicked();
            }

            @Override
            public void adNotDisplayed(Ad ad) {
                listener.onRewardedAdDisplayFailed(resolveError(ad));
            }
        });
    }

    @Override
    public void loadRewardedInterstitialAd(MaxAdapterResponseParameters parameters, Activity activity, MaxRewardedInterstitialAdapterListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "loadRewardedInterstitialAd");
        }

        listener.onRewardedInterstitialAdLoadFailed(INTERNAL_ERROR);
    }

    @Override
    public void showRewardedInterstitialAd(MaxAdapterResponseParameters parameters, Activity activity, MaxRewardedInterstitialAdapterListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "showRewardedInterstitialAd");
        }

        listener.onRewardedInterstitialAdDisplayFailed(INTERNAL_ERROR);
    }

    @Override
    public void loadNativeAd(MaxAdapterResponseParameters parameters, Activity activity, MaxNativeAdAdapterListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "loadNativeAd");
        }

        listener.onNativeAdLoadFailed(INTERNAL_ERROR);
    }

    @Override
    public void collectSignal(MaxAdapterSignalCollectionParameters parameters, Activity activity, MaxSignalCollectionListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "collectSignal");
        }

        listener.onSignalCollectionFailed(null);
    }

    private boolean isInvalidAdapter(@NonNull MaxAdapterParameters parameters) {
        Bundle serverParameters = parameters.getServerParameters();
        return serverParameters == null || !StartAppMediationAdapter.class.getName().equals(serverParameters.getString("adapter_class"));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean ensureInitialized(@NonNull Context context, @NonNull MaxAdapterParameters parameters) {
        Bundle serverParameters = parameters.getServerParameters();
        if (serverParameters == null) {
            if (DEBUG) {
                Log.e(LOG_TAG, "ensureInitialized: no server parameters");
            }

            return false;
        }

        String appId = serverParameters.getString(APP_ID);
        if (appId == null || appId.isEmpty()) {
            if (DEBUG) {
                Log.v(LOG_TAG, "ensureInitialized: no app ID");
            }

            return false;
        }

        String networkName = serverParameters.getString(NETWORK_NAME);

        synchronized (StartAppMediationAdapter.class) {
            if (initializedAppId == null) {
                StartAppAd.disableSplash();
                StartAppAd.disableAutoInterstitial();
                StartAppSDK.setTestAdsEnabled(true);
                StartAppSDK.init(context, appId, false);
                initializedAppId = appId;
                initializedAdUnit = parameters.getAdUnitId();

                log(networkName + " initialized with app ID " + appId);
                return true;
            } else if (appId.equals(initializedAppId)) {
                return true;
            } else {
                log("Ad unit " + parameters.getAdUnitId() + " is configured with app ID " + appId +
                        ", but the adapter has been initialized earlier for ad unit " + initializedAdUnit +
                        " with app ID " + initializedAppId + ". " + networkName +
                        " won't be re-initialized again.");

                return false;
            }
        }
    }

    @NonNull
    private AdPreferences createAdPreferences(@NonNull MaxAdapterParameters parameters) {
        AdPreferences result = new AdPreferences();

        Bundle serverParameters = parameters.getServerParameters();
        if (serverParameters != null) {
            if (serverParameters.getBoolean(IS_MUTED)) {
                result.muteVideo();
            }
        }

        Bundle customParameters = parameters.getCustomParameters();
        if (customParameters != null) {
            if (customParameters.containsKey(AD_TAG)) {
                result.setAdTag(customParameters.getString(AD_TAG));
            }

            if (customParameters.containsKey(MIN_CPM)) {
                result.setMinCpm(customParameters.getDouble(MIN_CPM));
            }

            if (customParameters.containsKey(NATIVE_IMAGE_SIZE)) {
                // TODO size
                // nativeImageSize = (Size) customParameters.getSerializable(NATIVE_IMAGE_SIZE);
            }

            if (customParameters.containsKey(NATIVE_SECONDARY_IMAGE_SIZE)) {
                // TODO size
                // nativeSecondaryImageSize = (Size) customParameters.getSerializable(NATIVE_SECONDARY_IMAGE_SIZE);
            }
        }

        return result;
    }

    @NonNull
    private static MaxAdapterError resolveError(@Nullable View view) {
        if (view instanceof BannerBase) {
            return resolveError(((BannerBase) view).getErrorMessage());
        }

        return INTERNAL_ERROR;
    }

    @NonNull
    private static MaxAdapterError resolveError(@Nullable Ad ad) {
        return resolveError(ad != null ? ad.getErrorMessage() : null);
    }

    @NonNull
    private static MaxAdapterError resolveError(@Nullable String message) {
        if (message == null) {
            return INTERNAL_ERROR;
        }

        if (message.contains("204") || message.contains("Empty Response")) {
            return NO_FILL;
        }

        return INTERNAL_ERROR;
    }
}
