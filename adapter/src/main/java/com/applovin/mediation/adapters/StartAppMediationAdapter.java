package com.applovin.mediation.adapters;

import static com.applovin.mediation.adapter.MaxAdapter.InitializationStatus.INITIALIZED_FAILURE;
import static com.applovin.mediation.adapter.MaxAdapter.InitializationStatus.INITIALIZED_SUCCESS;
import static com.applovin.mediation.adapter.MaxAdapterError.INTERNAL_ERROR;
import static com.applovin.mediation.adapter.MaxAdapterError.INVALID_CONFIGURATION;
import static com.applovin.mediation.adapter.MaxAdapterError.INVALID_LOAD_STATE;
import static com.applovin.mediation.adapter.MaxAdapterError.NO_FILL;
import static com.applovin.mediation.adapter.MaxAdapterError.SERVER_ERROR;
import static com.applovin.mediation.adapter.MaxAdapterError.UNSPECIFIED;
import static com.startapp.adapter.applovin.BuildConfig.DEBUG;
import static com.startapp.adapter.applovin.BuildConfig.VERSION_NAME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
import com.applovin.mediation.nativeAds.MaxNativeAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkUtils.Size;
import com.startapp.sdk.ads.banner.BannerBase;
import com.startapp.sdk.ads.banner.BannerCreator;
import com.startapp.sdk.ads.banner.BannerFormat;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.banner.BannerRequest;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdDisplayListener;
import com.startapp.sdk.ads.nativead.NativeAdInterface;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;
import com.startapp.sdk.adsbase.model.AdPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Keep
public class StartAppMediationAdapter extends MediationAdapterBase implements MaxInterstitialAdapter, MaxRewardedInterstitialAdapter, MaxRewardedAdapter, MaxAdViewAdapter, MaxNativeAdAdapter, MaxSignalProvider {
    private static final String LOG_TAG = StartAppMediationAdapter.class.getSimpleName();

    private static final String APP_ID = "app_id";
    private static final String NETWORK_NAME = "network_name";
    private static final String IS_MUTED = "is_muted";
    private static final String TEMPLATE = "template";
    private static final String AD_TAG = "adTag";
    private static final String INTERSTITIAL_MODE = "interstitialMode";
    private static final String MIN_CPM = "minCPM";
    private static final String IS_3D_BANNER = "is3DBanner";
    private static final String NATIVE_IMAGE_SIZE = "nativeImageSize";
    private static final String NATIVE_SECONDARY_IMAGE_SIZE = "nativeSecondaryImageSize";

    private static final int IMAGE_SIZE_72X72 = 0;
    private static final int IMAGE_SIZE_100X100 = 1;
    private static final int IMAGE_SIZE_150X150 = 2;
    private static final int IMAGE_SIZE_340X340 = 3;
    private static final int IMAGE_SIZE_1200X628 = 4;

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
            Log.v(LOG_TAG, debugPrefix() + "constructor");
        }
    }

    @Override
    public void initialize(MaxAdapterInitializationParameters parameters, Activity activity, OnCompletionListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, debugPrefix() + "initialize");
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
                Log.w(LOG_TAG, debugPrefix() + "initialize: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onCompletion(INITIALIZED_FAILURE, null);
            return;
        }

        listener.onCompletion(INITIALIZED_SUCCESS, null);
    }

    @Override
    public String getSdkVersion() {
        if (DEBUG) {
            Log.v(LOG_TAG, debugPrefix() + "getSdkVersion");
        }

        return StartAppSDK.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        if (DEBUG) {
            Log.v(LOG_TAG, debugPrefix() + "getAdapterVersion");
        }

        return VERSION_NAME;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.v(LOG_TAG, debugPrefix() + "onDestroy");
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
            Log.v(LOG_TAG, debugPrefix() + "loadAdViewAd: " + (parameters != null ? parameters.getAdUnitId() : null));
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || format == null || activity == null) {
            listener.onAdViewAdLoadFailed(UNSPECIFIED);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, debugPrefix() + "loadAdViewAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onAdViewAdLoadFailed(INVALID_LOAD_STATE);
            return;
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            listener.onAdViewAdLoadFailed(INVALID_CONFIGURATION);
            return;
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onAdViewAdLoadFailed(INVALID_CONFIGURATION);
            return;
        }

        final AdPreferences adPreferences = createAdPreferences(parameters);
        final Bundle customParameters = parameters.getCustomParameters();
        final BannerFormat bannerFormat;

        if (format == MaxAdFormat.BANNER) {
            bannerFormat = BannerFormat.BANNER;
        } else if (format == MaxAdFormat.MREC) {
            bannerFormat = BannerFormat.MREC;
        } else {
            listener.onAdViewAdLoadFailed(INVALID_CONFIGURATION);
            return;
        }

        Map<String, Object> localExtras = parameters.getLocalExtraParameters();
        boolean adaptive = localExtras != null && Boolean.parseBoolean(String.valueOf(localExtras.get("adaptive_banner")));
        Size size = adaptive ? format.getAdaptiveSize(activity) : format.getSize();

        if (DEBUG) {
            Log.v(LOG_TAG, debugPrefix() + "loadAdViewAd: size: " + size.getWidth() + "x" + size.getHeight());
        }

        new BannerRequest(getApplicationContext())
                .setAdFormat(bannerFormat)
                .setAdSize(size.getWidth(), size.getHeight())
                .setAdPreferences(adPreferences)
                .load(new BannerRequest.Callback() {
                    @Override
                    public void onFinished(@Nullable final BannerCreator creator, @Nullable String error) {
                        if (DEBUG) {
                            Log.v(LOG_TAG, debugPrefix() + "loadAdViewAd: onFinished: " + error);
                        }

                        if (creator != null) {
                            final FrameLayout frameLayout = new FrameLayout(getApplicationContext());
                            frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                boolean called;

                                @Override
                                public void onGlobalLayout() {
                                    if (called) {
                                        return;
                                    } else {
                                        called = true;
                                    }

                                    frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    frameLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            crateBanner(listener, creator, frameLayout);
                                        }
                                    });
                                }
                            });
                            listener.onAdViewAdLoaded(frameLayout);
                        } else {
                            listener.onAdViewAdLoadFailed(resolveError(error));
                        }
                    }
                });
    }

    private void crateBanner(final MaxAdViewAdapterListener listener,
                             BannerCreator creator,
                             FrameLayout outerLayout
    ) {

        View banner = creator.create(getApplicationContext(), new BannerListener() {
            @Override
            public void onReceiveAd(View view) {
                if (DEBUG) {
                    Log.v(LOG_TAG, debugPrefix() + "loadAdViewAd: onReceiveAd");
                }
            }

            @Override
            public void onFailedToReceiveAd(View view) {
                if (DEBUG) {
                    Log.v(LOG_TAG, debugPrefix() + "loadAdViewAd: onFailedToReceiveAd");
                }
            }

            @Override
            public void onImpression(View view) {
                if (DEBUG) {
                    Log.v(LOG_TAG, debugPrefix() + "loadAdViewAd: onImpression");
                }

                listener.onAdViewAdDisplayed();
            }

            @Override
            public void onClick(View view) {
                if (DEBUG) {
                    Log.v(LOG_TAG, debugPrefix() + "loadAdViewAd: onClick");
                }

                listener.onAdViewAdClicked();
            }
        });
        outerLayout.addView(banner);
    }

    @Override
    public void loadInterstitialAd(
            @Nullable MaxAdapterResponseParameters parameters,
            @Nullable Activity activity,
            @Nullable final MaxInterstitialAdapterListener listener
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, debugPrefix() + "loadInterstitialAd");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || activity == null) {
            listener.onInterstitialAdLoadFailed(UNSPECIFIED);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, debugPrefix() + "loadInterstitialAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onInterstitialAdLoadFailed(INVALID_LOAD_STATE);
            return;
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            listener.onInterstitialAdLoadFailed(INVALID_CONFIGURATION);
            return;
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onInterstitialAdLoadFailed(INVALID_CONFIGURATION);
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
            Log.v(LOG_TAG, debugPrefix() + "showInterstitialAd");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || activity == null) {
            listener.onInterstitialAdDisplayFailed(UNSPECIFIED);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, debugPrefix() + "showInterstitialAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onInterstitialAdDisplayFailed(INVALID_LOAD_STATE);
            return;
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            listener.onInterstitialAdDisplayFailed(INVALID_CONFIGURATION);
            return;
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onInterstitialAdDisplayFailed(INVALID_CONFIGURATION);
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
            Log.v(LOG_TAG, debugPrefix() + "loadRewardedAd");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || activity == null) {
            listener.onRewardedAdLoadFailed(UNSPECIFIED);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, debugPrefix() + "loadRewardedAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onRewardedAdLoadFailed(INVALID_LOAD_STATE);
            return;
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            listener.onRewardedAdLoadFailed(INVALID_CONFIGURATION);
            return;
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onRewardedAdLoadFailed(INVALID_CONFIGURATION);
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
            Log.v(LOG_TAG, debugPrefix() + "showRewardedAd");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || activity == null) {
            listener.onRewardedAdDisplayFailed(UNSPECIFIED);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, debugPrefix() + "showRewardedAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onRewardedAdDisplayFailed(INVALID_LOAD_STATE);
            return;
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            listener.onRewardedAdDisplayFailed(INVALID_CONFIGURATION);
            return;
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onRewardedAdDisplayFailed(INVALID_CONFIGURATION);
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
                    Log.v(LOG_TAG, debugPrefix() + "showRewardedAd: onVideoCompleted");
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
            Log.v(LOG_TAG, debugPrefix() + "loadRewardedInterstitialAd");
        }

        listener.onRewardedInterstitialAdLoadFailed(NO_FILL);
    }

    @Override
    public void showRewardedInterstitialAd(MaxAdapterResponseParameters parameters, Activity activity, MaxRewardedInterstitialAdapterListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, debugPrefix() + "showRewardedInterstitialAd");
        }

        listener.onRewardedInterstitialAdDisplayFailed(UNSPECIFIED);
    }

    @Override
    public void loadNativeAd(MaxAdapterResponseParameters parameters, Activity activity, final MaxNativeAdAdapterListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, debugPrefix() + "loadNativeAd");
        }

        if (listener == null) {
            return;
        }

        if (parameters == null || activity == null) {
            listener.onNativeAdLoadFailed(UNSPECIFIED);
            return;
        }

        if (isInvalidAdapter(parameters)) {
            if (DEBUG) {
                Log.w(LOG_TAG, debugPrefix() + "loadNativeAd: invalid adapter: " + parameters.getServerParameters());
            }

            listener.onNativeAdLoadFailed(INVALID_LOAD_STATE);
            return;
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId == null || adUnitId.isEmpty()) {
            listener.onNativeAdLoadFailed(INVALID_CONFIGURATION);
            return;
        }

        if (!ensureInitialized(activity, parameters)) {
            listener.onNativeAdLoadFailed(INVALID_CONFIGURATION);
            return;
        }

        NativeAdPreferences adPreferences = createNativeAdPreferences(parameters);

        final StartAppNativeAd nativeAd = new StartAppNativeAd(getApplicationContext());
        nativeAd.loadAd(adPreferences, new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                if (DEBUG) {
                    Log.v(LOG_TAG, debugPrefix() + "loadNativeAd: onReceiveAd: " + ad);
                }

                ArrayList<NativeAdDetails> nativeAdDetailsList = nativeAd.getNativeAds();
                if (nativeAdDetailsList == null || nativeAdDetailsList.size() < 1) {
                    listener.onNativeAdLoadFailed(NO_FILL);
                    return;
                }

                NativeAdDetails nativeAdDetails = nativeAdDetailsList.get(0);
                if (nativeAdDetails == null) {
                    listener.onNativeAdLoadFailed(NO_FILL);
                    return;
                }

                if (DEBUG) {
                    Log.v(LOG_TAG, debugPrefix() + "loadNativeAd: onReceiveAd: notify listener");
                }

                listener.onNativeAdLoaded(new MaxStartAppNativeAd(getApplicationContext(), nativeAdDetails, listener), null);
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                listener.onNativeAdLoadFailed(resolveError(ad));
            }
        });
    }

    @Override
    public void collectSignal(MaxAdapterSignalCollectionParameters parameters, Activity activity, MaxSignalCollectionListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, debugPrefix() + "collectSignal");
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
                Log.e(LOG_TAG, debugPrefix() + "ensureInitialized: no server parameters");
            }

            return false;
        }

        String appId = serverParameters.getString(APP_ID);
        if (appId == null || appId.isEmpty()) {
            if (DEBUG) {
                Log.v(LOG_TAG, debugPrefix() + "ensureInitialized: no app ID");
            }

            return false;
        }

        String networkName = serverParameters.getString(NETWORK_NAME);

        synchronized (StartAppMediationAdapter.class) {
            if (initializedAppId == null) {
                if (parameters.isTesting()) {
                    StartAppSDK.setTestAdsEnabled(true);
                }

                StartAppSDK.init(context, appId, false);
                StartAppSDK.enableMediationMode(context, "applovin", getAdapterVersion());
                initializedAppId = appId;
                initializedAdUnit = parameters.getAdUnitId();

                initPrivacyParams(context);

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

    private void initPrivacyParams(@NonNull Context context) {
        SharedPreferences.Editor extrasEditor = null;

        if (AppLovinPrivacySettings.isUserConsentSet(context)) {
            // noinspection ConstantConditions
            extrasEditor = ensureExtrasEditor(context, extrasEditor);
            extrasEditor.putBoolean("medPas", AppLovinPrivacySettings.hasUserConsent(context));
        }

        if (AppLovinPrivacySettings.isDoNotSellSet(context)) {
            extrasEditor = ensureExtrasEditor(context, extrasEditor);
            extrasEditor.putBoolean("medCCPA", AppLovinPrivacySettings.isDoNotSell(context));
        }

        if (AppLovinPrivacySettings.isAgeRestrictedUserSet(context)) {
            extrasEditor = ensureExtrasEditor(context, extrasEditor);
            extrasEditor.putBoolean("medAgeRestrict", AppLovinPrivacySettings.isAgeRestrictedUser(context));
        }

        if (extrasEditor != null) {
            extrasEditor.apply();
        }
    }

    @NonNull
    @SuppressLint("CommitPrefEdits")
    private static SharedPreferences.Editor ensureExtrasEditor(@NonNull Context context, @Nullable SharedPreferences.Editor editor) {
        if (editor == null) {
            editor = StartAppSDK.getExtras(context).edit();
        }

        return editor;
    }

    @NonNull
    private NativeAdPreferences createNativeAdPreferences(@NonNull MaxAdapterResponseParameters parameters) {
        NativeAdPreferences result = new NativeAdPreferences();
        result.setAdTag(parameters.getThirdPartyAdPlacementId());

        fillAdPreferences(parameters, result);

        result.setAutoBitmapDownload(true);

        Bundle customParameters = parameters.getServerParameters();
        if (customParameters != null) {
            String template = customParameters.getString(TEMPLATE);
            if (template != null) {
                if (template.startsWith("medium_")) {
                    result.setSecondaryImageSize(IMAGE_SIZE_1200X628);
                } else if (template.startsWith("small_")) {
                    result.setSecondaryImageSize(IMAGE_SIZE_340X340);
                }
            }

            String imageSize = customParameters.getString(NATIVE_IMAGE_SIZE);
            if (imageSize != null) {
                result.setPrimaryImageSize(imageSizeToInt(imageSize));
            }

            String secondaryImageSize = customParameters.getString(NATIVE_SECONDARY_IMAGE_SIZE);
            if (secondaryImageSize != null) {
                result.setSecondaryImageSize(imageSizeToInt(secondaryImageSize));
            }
        }

        return result;
    }

    private static int imageSizeToInt(@NonNull String input) {
        switch (input) {
            case "72x72":
                return IMAGE_SIZE_72X72;
            case "100x100":
                return IMAGE_SIZE_100X100;
            case "150x150":
                return IMAGE_SIZE_150X150;
            case "340x340":
                return IMAGE_SIZE_340X340;
            case "1200x628":
                return IMAGE_SIZE_1200X628;
        }

        return IMAGE_SIZE_150X150;
    }

    @NonNull
    private AdPreferences createAdPreferences(@NonNull MaxAdapterResponseParameters parameters) {
        AdPreferences result = new AdPreferences();
        result.setAdTag(parameters.getThirdPartyAdPlacementId());

        fillAdPreferences(parameters, result);
        return result;
    }

    private void fillAdPreferences(@NonNull MaxAdapterParameters parameters, @NonNull AdPreferences result) {
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
        }

        String adUnitId = parameters.getAdUnitId();
        if (adUnitId != null && adUnitId.length() > 0) {
            result.setPlacementId(adUnitId);
        }
    }

    @NonNull
    private static MaxAdapterError resolveError(@Nullable View view) {
        if (view instanceof BannerBase) {
            return resolveError(((BannerBase) view).getErrorMessage());
        }

        return UNSPECIFIED;
    }

    @NonNull
    private static MaxAdapterError resolveError(@Nullable Ad ad) {
        return resolveError(ad != null ? ad.getErrorMessage() : null);
    }

    @NonNull
    private static MaxAdapterError resolveError(@Nullable String message) {
        if (message == null) {
            return UNSPECIFIED;
        }

        if (message.contains("204") || message.contains("Empty Response")) {
            return NO_FILL;
        }

        if (message.contains(", status 5")) {
            return SERVER_ERROR;
        }

        return UNSPECIFIED;
    }

    static class MaxStartAppNativeAd extends MaxNativeAd {
        @NonNull
        final NativeAdDetails nativeAdDetails;

        @NonNull
        final MaxNativeAdAdapterListener listener;

        @NonNull
        static Builder createBuilder(@NonNull Context context, @NonNull NativeAdDetails nativeAdDetails) {
            MaxNativeAd.Builder builder = new MaxNativeAd.Builder()
                    .setAdFormat(MaxAdFormat.NATIVE)
                    .setTitle(nativeAdDetails.getTitle())
                    .setBody(nativeAdDetails.getDescription())
                    .setCallToAction(nativeAdDetails.getCallToAction());

            Bitmap imageBitmap = nativeAdDetails.getImageBitmap();
            if (imageBitmap != null) {
                builder.setIcon(new MaxNativeAdImage(new BitmapDrawable(context.getResources(), imageBitmap)));
            }

            Bitmap secondaryBitmap = nativeAdDetails.getSecondaryImageBitmap();
            if (secondaryBitmap != null) {
                ImageView imageView = new ImageView(context);
                imageView.setImageDrawable(new BitmapDrawable(context.getResources(), secondaryBitmap));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                builder.setMediaView(imageView);
            }

            return builder;
        }

        MaxStartAppNativeAd(@NonNull Context context, @NonNull NativeAdDetails nativeAdDetails, @NonNull MaxNativeAdAdapterListener listener) {
            super(createBuilder(context, nativeAdDetails));

            this.nativeAdDetails = nativeAdDetails;
            this.listener = listener;
        }

        @Override
        public void prepareViewForInteraction(@NonNull MaxNativeAdView maxNativeAdView) {
            super.prepareViewForInteraction(maxNativeAdView);

            List<View> clickableViews = new ArrayList<>(2);
            clickableViews.add(maxNativeAdView);

            findAllButtons(maxNativeAdView, clickableViews);

            nativeAdDetails.registerViewForInteraction(maxNativeAdView, clickableViews, new NativeAdDisplayListener() {
                @Override
                public void adDisplayed(NativeAdInterface nativeAdInterface) {
                    listener.onNativeAdDisplayed(null);
                }

                @Override
                public void adClicked(NativeAdInterface nativeAdInterface) {
                    listener.onNativeAdClicked();
                }

                @Override
                public void adHidden(NativeAdInterface nativeAdInterface) {
                    // none
                }

                @Override
                public void adNotDisplayed(NativeAdInterface nativeAdInterface) {
                    // none
                }
            });
        }

        private void findAllButtons(@NonNull ViewGroup parent, @NonNull List<View> clickableViews) {
            for (int i = 0, n = parent.getChildCount(); i < n; ++i) {
                View child = parent.getChildAt(i);
                if (child instanceof Button) {
                    clickableViews.add(child);
                } else if (child instanceof ViewGroup) {
                    findAllButtons((ViewGroup) child, clickableViews);
                }
            }
        }
    }

    @NonNull
    private String debugPrefix() {
        if (DEBUG) {
            return "[" + Integer.toHexString(hashCode()) + "]: ";
        }

        return "";
    }
}
