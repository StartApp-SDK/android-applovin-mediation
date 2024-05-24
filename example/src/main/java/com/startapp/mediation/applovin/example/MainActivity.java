package com.startapp.mediation.applovin.example;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.startapp.sdk.adsbase.StartAppSDK;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    enum AdState {
        IDLE,
        LOADING,
        VISIBLE,
    }

    private static final MutableLiveData<Boolean> initialized = new MutableLiveData<>(null);

    private final MutableLiveData<Pair<MaxInterstitialAd, AdState>> interstitialLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<MaxRewardedAd, AdState>> rewardedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<MaxAdView, AdState>> bannerLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<MaxAdView, AdState>> mrecLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<MaxNativeAdView, AdState>> nativeSmallLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<MaxNativeAdView, AdState>> nativeMediumLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<MaxNativeAdView, AdState>> nativeManualLiveData = new MutableLiveData<>();

    private ViewGroup bannerContainer;
    private ViewGroup mrecContainer;
    private ViewGroup nativeSmallContainer;
    private ViewGroup nativeMediumContainer;
    private ViewGroup nativeManualContainer;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        setTitle("AppLovin " + AppLovinSdk.VERSION + " - Start.io " + StartAppSDK.getVersion());

        setContentView(R.layout.activity_main);

        // region UI initialization

        View loadInterstitial = findViewById(R.id.load_interstitial);
        View showInterstitial = findViewById(R.id.show_interstitial);
        View loadRewarded = findViewById(R.id.load_rewarded);
        View showRewarded = findViewById(R.id.show_rewarded);
        View loadBanner = findViewById(R.id.load_banner);
        View showBanner = findViewById(R.id.show_banner);
        View hideBanner = findViewById(R.id.hide_banner);
        View loadMrec = findViewById(R.id.load_mrec);
        View showMrec = findViewById(R.id.show_mrec);
        View hideMrec = findViewById(R.id.hide_mrec);
        View loadNativeSmall = findViewById(R.id.load_native_small);
        View showNativeSmall = findViewById(R.id.show_native_small);
        View hideNativeSmall = findViewById(R.id.hide_native_small);
        View loadNativeMedium = findViewById(R.id.load_native_medium);
        View showNativeMedium = findViewById(R.id.show_native_medium);
        View hideNativeMedium = findViewById(R.id.hide_native_medium);
        View loadNativeManual = findViewById(R.id.load_native_manual);
        View showNativeManual = findViewById(R.id.show_native_manual);
        View hideNativeManual = findViewById(R.id.hide_native_manual);
        bannerContainer = findViewById(R.id.banner_container);
        mrecContainer = findViewById(R.id.mrec_container);
        nativeSmallContainer = findViewById(R.id.native_small_container);
        nativeMediumContainer = findViewById(R.id.native_medium_container);
        nativeManualContainer = findViewById(R.id.native_manual_container);

        loadInterstitial.setOnClickListener(this::loadInterstitial);
        showInterstitial.setOnClickListener(this::showInterstitial);
        loadRewarded.setOnClickListener(this::loadRewarded);
        showRewarded.setOnClickListener(this::showRewarded);
        loadBanner.setOnClickListener(this::loadBanner);
        showBanner.setOnClickListener(this::showBanner);
        hideBanner.setOnClickListener(this::hideBanner);
        loadMrec.setOnClickListener(this::loadMrec);
        showMrec.setOnClickListener(this::showMrec);
        hideMrec.setOnClickListener(this::hideMrec);
        loadNativeSmall.setOnClickListener(this::loadNativeSmall);
        showNativeSmall.setOnClickListener(this::showNativeSmall);
        hideNativeSmall.setOnClickListener(this::hideNativeSmall);
        loadNativeMedium.setOnClickListener(this::loadNativeMedium);
        showNativeMedium.setOnClickListener(this::showNativeMedium);
        hideNativeMedium.setOnClickListener(this::hideNativeMedium);
        loadNativeManual.setOnClickListener(this::loadNativeManual);
        showNativeManual.setOnClickListener(this::showNativeManual);
        hideNativeManual.setOnClickListener(this::hideNativeManual);

        interstitialLiveData.observe(this, pair -> {
            loadInterstitial.setEnabled(isLoadButtonEnabled(pair));
            showInterstitial.setEnabled(isShowButtonEnabled(pair));
        });

        rewardedLiveData.observe(this, pair -> {
            loadRewarded.setEnabled(isLoadButtonEnabled(pair));
            showRewarded.setEnabled(isShowButtonEnabled(pair));
        });

        bannerLiveData.observe(this, pair -> {
            loadBanner.setEnabled(isLoadButtonEnabled(pair));
            showBanner.setEnabled(isShowButtonEnabled(pair));
            hideBanner.setEnabled(isHideButtonVisible(pair));
            bannerContainer.setVisibility(isHideButtonVisible(pair) ? View.VISIBLE : View.GONE);
        });

        mrecLiveData.observe(this, pair -> {
            loadMrec.setEnabled(isLoadButtonEnabled(pair));
            showMrec.setEnabled(isShowButtonEnabled(pair));
            hideMrec.setEnabled(isHideButtonVisible(pair));
            mrecContainer.setVisibility(isHideButtonVisible(pair) ? View.VISIBLE : View.GONE);
        });

        nativeSmallLiveData.observe(this, pair -> {
            loadNativeSmall.setEnabled(isLoadButtonEnabled(pair));
            showNativeSmall.setEnabled(isShowButtonEnabled(pair));
            hideNativeSmall.setEnabled(isHideButtonVisible(pair));
            nativeSmallContainer.setVisibility(isHideButtonVisible(pair) ? View.VISIBLE : View.GONE);
        });

        nativeMediumLiveData.observe(this, pair -> {
            loadNativeMedium.setEnabled(isLoadButtonEnabled(pair));
            showNativeMedium.setEnabled(isShowButtonEnabled(pair));
            hideNativeMedium.setEnabled(isHideButtonVisible(pair));
            nativeMediumContainer.setVisibility(isHideButtonVisible(pair) ? View.VISIBLE : View.GONE);
        });

        nativeManualLiveData.observe(this, pair -> {
            loadNativeManual.setEnabled(isLoadButtonEnabled(pair));
            showNativeManual.setEnabled(isShowButtonEnabled(pair));
            hideNativeManual.setEnabled(isHideButtonVisible(pair));
            nativeManualContainer.setVisibility(isHideButtonVisible(pair) ? View.VISIBLE : View.GONE);
        });

        // endregion

        initialized.observe(this, value -> {
            if (value == null) {
                initialized.setValue(false);

                AppLovinSdkInitializationConfiguration configuration = AppLovinSdkInitializationConfiguration.builder(getString(R.string.sdk_key), this)
                        .setMediationProvider(AppLovinMediationProvider.MAX)
                        .build();

                AppLovinSdk.getInstance(this).initialize(configuration, sdkConfig -> {
                    initialized.setValue(true);

                    // TODO remove this line in production
                    StartAppSDK.setTestAdsEnabled(true);
                });
            } else if (value) {
                interstitialLiveData.setValue(null);
                rewardedLiveData.setValue(null);
                bannerLiveData.setValue(null);
                mrecLiveData.setValue(null);
                nativeSmallLiveData.setValue(null);
                nativeMediumLiveData.setValue(null);
                nativeManualLiveData.setValue(null);
            }
        });
    }

    private static boolean isInitialized() {
        return Boolean.TRUE.equals(initialized.getValue());
    }

    private static <T> boolean isLoadButtonEnabled(@Nullable Pair<T, AdState> pair) {
        return (pair == null || pair.first == null && pair.second != AdState.LOADING) && isInitialized();
    }

    private static <T> boolean isShowButtonEnabled(@Nullable Pair<T, AdState> pair) {
        return pair != null && pair.first != null && pair.second != AdState.VISIBLE;
    }

    private static <T> boolean isHideButtonVisible(@Nullable Pair<T, AdState> pair) {
        return pair != null && pair.second == AdState.VISIBLE;
    }

    // region Banner & Mrec

    private void loadBanner(@NonNull View view) {
        int heightPx = getResources().getDimensionPixelSize(R.dimen.banner_height);
        ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(MATCH_PARENT, heightPx, Gravity.CENTER);
        loadAdView(MaxAdFormat.BANNER, R.string.ad_unit_banner, layoutParams, bannerLiveData);
    }

    private void loadMrec(@NonNull View view) {
        int widthPx = getResources().getDimensionPixelSize(R.dimen.mrec_width);
        int heightPx = getResources().getDimensionPixelSize(R.dimen.mrec_height);
        ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(widthPx, heightPx, Gravity.CENTER);
        loadAdView(MaxAdFormat.MREC, R.string.ad_unit_mrec, layoutParams, mrecLiveData);
    }

    private void showBanner(@NonNull View view) {
        showAdView(bannerLiveData, bannerContainer);
    }

    private void showMrec(@NonNull View view) {
        showAdView(mrecLiveData, mrecContainer);
    }

    private void hideBanner(@NonNull View view) {
        hideAdView(bannerLiveData, bannerContainer);
    }

    private void hideMrec(@NonNull View view) {
        hideAdView(mrecLiveData, mrecContainer);
    }

    private void loadAdView(@NonNull MaxAdFormat format, @StringRes int adUnitStringId, @NonNull ViewGroup.LayoutParams layoutParams, @NonNull MutableLiveData<Pair<MaxAdView, AdState>> liveData) {
        MaxAdView adView = new MaxAdView(getString(adUnitStringId), format, this);
        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdLoaded(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdLoaded: " + ad);

                Pair<MaxAdView, AdState> pair = liveData.getValue();

                if (pair == null || pair.second == AdState.LOADING) {
                    liveData.setValue(new Pair<>(adView, AdState.IDLE));
                }
            }

            @Override
            public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + adUnitId + ", error: " + error);

                liveData.setValue(null);
            }

            @Override
            public void onAdExpanded(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdExpanded: " + ad);
            }

            @Override
            public void onAdCollapsed(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdCollapsed: " + ad);
            }

            @Override
            public void onAdDisplayed(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdDisplayed: " + ad);
            }

            @Override
            public void onAdHidden(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdHidden: " + ad);
            }

            @Override
            public void onAdClicked(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdClicked: " + ad);
            }

            @Override
            public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
                Log.v(LOG_TAG, "onAdDisplayFailed: " + ad + ", error: " + error);
            }
        });

        liveData.setValue(new Pair<>(null, AdState.LOADING));

        adView.setLayoutParams(layoutParams);
        adView.loadAd();
    }

    private void showAdView(@NonNull MutableLiveData<Pair<MaxAdView, AdState>> liveData, @NonNull ViewGroup container) {
        Pair<MaxAdView, AdState> pair = liveData.getValue();
        if (pair != null && pair.first != null) {
            container.removeAllViews();
            container.addView(pair.first);
            liveData.setValue(new Pair<>(pair.first, AdState.VISIBLE));
        } else {
            Toast.makeText(this, "AdView is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideAdView(@NonNull MutableLiveData<Pair<MaxAdView, AdState>> liveData, @NonNull ViewGroup container) {
        container.removeAllViews();
        liveData.setValue(new Pair<>(null, AdState.IDLE));
    }

    // endregion

    // region Native

    private void loadNativeSmall(@NonNull View view) {
        loadNative(nativeSmallLiveData, R.string.ad_unit_native_small);
    }

    private void loadNativeMedium(@NonNull View view) {
        loadNative(nativeMediumLiveData, R.string.ad_unit_native_small);
    }

    private void loadNativeManual(@NonNull View view) {
        loadNative(nativeManualLiveData, R.string.ad_unit_native_manual);
    }

    private void showNativeSmall(@NonNull View view) {
        showNative(nativeSmallLiveData, nativeSmallContainer);
    }

    private void showNativeMedium(@NonNull View view) {
        showNative(nativeMediumLiveData, nativeMediumContainer);
    }

    private void showNativeManual(@NonNull View view) {
        showNative(nativeManualLiveData, nativeManualContainer);
    }

    private void hideNativeSmall(@NonNull View view) {
        hideNative(nativeSmallLiveData, nativeSmallContainer);
    }

    private void hideNativeMedium(@NonNull View view) {
        hideNative(nativeMediumLiveData, nativeMediumContainer);
    }

    private void hideNativeManual(@NonNull View view) {
        hideNative(nativeManualLiveData, nativeManualContainer);
    }

    private void loadNative(@NonNull MutableLiveData<Pair<MaxNativeAdView, AdState>> liveData, @StringRes int adUnitStringId) {
        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(getString(adUnitStringId), this);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(@Nullable MaxNativeAdView adView, @NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onNativeAdLoaded: " + adView + ", " + ad);

                liveData.setValue(new Pair<>(adView, AdState.IDLE));
            }

            @Override
            public void onNativeAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
                Log.v(LOG_TAG, "onNativeAdLoadFailed: " + adUnitId + ", error: " + error);

                liveData.setValue(null);
            }

            @Override
            public void onNativeAdClicked(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onNativeAdClicked: " + ad);
            }
        });

        liveData.setValue(new Pair<>(null, AdState.LOADING));

        if (adUnitStringId == R.string.ad_unit_native_manual) {
            MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(R.layout.native_custom_ad_view)
                    .setTitleTextViewId(R.id.title_text_view)
                    .setBodyTextViewId(R.id.body_text_view)
                    .setAdvertiserTextViewId(R.id.advertiser_textView)
                    .setIconImageViewId(R.id.icon_image_view)
                    .setMediaContentViewGroupId(R.id.media_view_container)
                    .setOptionsContentViewGroupId(R.id.ad_options_view)
                    .setCallToActionButtonId(R.id.cta_button)
                    .build();

            nativeAdLoader.loadAd(new MaxNativeAdView(binder, this));
        } else {
            nativeAdLoader.loadAd();
        }
    }

    private void showNative(@NonNull MutableLiveData<Pair<MaxNativeAdView, AdState>> liveData, @NonNull ViewGroup container) {
        Pair<MaxNativeAdView, AdState> pair = liveData.getValue();
        if (pair != null && pair.first != null) {
            container.removeAllViews();
            container.addView(pair.first);
            liveData.setValue(new Pair<>(pair.first, AdState.VISIBLE));
        } else {
            Toast.makeText(this, "Native is not ready", Toast.LENGTH_SHORT).show();
            liveData.setValue(null);
        }
    }

    private void hideNative(@NonNull MutableLiveData<Pair<MaxNativeAdView, AdState>> liveData, @NonNull ViewGroup container) {
        container.removeAllViews();
        liveData.setValue(null);
    }

    // endregion

    // region Interstitial

    private void loadInterstitial(@NonNull View view) {
        MaxInterstitialAd interstitialAd = new MaxInterstitialAd(getString(R.string.ad_unit_interstitial), this);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdLoaded");

                interstitialLiveData.setValue(new Pair<>(interstitialAd, AdState.IDLE));
            }

            @Override
            public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + adUnitId + ", error: " + error);

                interstitialLiveData.setValue(null);
            }

            @Override
            public void onAdDisplayed(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdDisplayed: " + ad);

                interstitialLiveData.setValue(null);
            }

            @Override
            public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
                Log.v(LOG_TAG, "onAdDisplayFailed: " + ad);

                interstitialLiveData.setValue(null);
            }

            @Override
            public void onAdHidden(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdHidden: " + ad);
            }

            @Override
            public void onAdClicked(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdClicked: " + ad);
            }
        });

        interstitialLiveData.setValue(new Pair<>(null, AdState.LOADING));
        interstitialAd.loadAd();
    }

    public void showInterstitial(@NonNull View view) {
        Pair<MaxInterstitialAd, AdState> pair = interstitialLiveData.getValue();
        if (pair != null && pair.first != null) {
            pair.first.showAd(this);
        } else {
            Toast.makeText(this, "Interstitial is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    // endregion

    // region Rewarded

    private void loadRewarded(@NonNull View view) {
        MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(getString(R.string.ad_unit_rewarded), this);
        rewardedAd.setListener(new MaxRewardedAdListener() {
            @Override
            public void onAdLoaded(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdLoaded");

                rewardedLiveData.setValue(new Pair<>(rewardedAd, AdState.IDLE));
            }

            @Override
            public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + adUnitId + ", error: " + error);

                rewardedLiveData.setValue(null);
            }

            @Override
            public void onAdDisplayed(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdDisplayed: " + ad);

                rewardedLiveData.setValue(null);
            }

            @Override
            public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
                Log.v(LOG_TAG, "onAdDisplayFailed: " + ad);

                rewardedLiveData.setValue(null);
            }

            @Override
            public void onAdHidden(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdHidden: " + ad);
            }

            @Override
            public void onAdClicked(@NonNull MaxAd ad) {
                Log.v(LOG_TAG, "onAdClicked: " + ad);
            }

            @Override
            public void onUserRewarded(@NonNull MaxAd ad, @NonNull MaxReward reward) {
                Log.v(LOG_TAG, "onUserRewarded: " + ad + ", reward: " + reward);

                Toast.makeText(getApplicationContext(), "User earned a reward", Toast.LENGTH_SHORT).show();
            }
        });

        rewardedLiveData.setValue(new Pair<>(null, AdState.LOADING));
        rewardedAd.loadAd();
    }

    public void showRewarded(@NonNull View view) {
        Pair<MaxRewardedAd, AdState> pair = rewardedLiveData.getValue();
        if (pair != null && pair.first != null) {
            pair.first.showAd(this);
        } else {
            Toast.makeText(this, "Rewarded is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    // endregion
}
