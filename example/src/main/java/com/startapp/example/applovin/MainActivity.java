package com.startapp.example.applovin;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.applovin.sdk.AppLovinSdk;

@SuppressWarnings("CodeBlock2Expr")
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Nullable
    private static Boolean initialized;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        if (initialized == null) {
            initialized = false;

            if (BuildConfig.testDevices.size() > 0) {
                AppLovinSdk.getInstance(getApplicationContext())
                        .getSettings()
                        .setTestDeviceAdvertisingIds(BuildConfig.testDevices);
            }

            AppLovinSdk.getInstance(getApplicationContext())
                    .getSettings()
                    .setVerboseLogging(false);

            AppLovinSdk.getInstance(getApplicationContext())
                    .setMediationProvider("max");

            AppLovinSdk.initializeSdk(getApplicationContext(), configuration -> {
                initialized = true;
            });
        }

        setContentView(R.layout.activity_main);

        findViewById(R.id.load_banner).setOnClickListener(view -> {
            onLoadBannerClicked(view, findViewById(R.id.container));
        });

        findViewById(R.id.load_mrec).setOnClickListener(view -> {
            onLoadMrecClicked(view, findViewById(R.id.container));
        });

        findViewById(R.id.load_native).setOnClickListener(view -> {
            onLoadNativeClicked(view, findViewById(R.id.container));
        });

        findViewById(R.id.load_interstitial).setOnClickListener(view -> {
            onLoadInterstitialClicked(view, findViewById(R.id.show_interstitial));
        });

        findViewById(R.id.load_rewarded).setOnClickListener(view -> {
            onLoadRewardedClicked(view, findViewById(R.id.show_rewarded));
        });
    }

    private void onLoadBannerClicked(@NonNull View loadButton, @NonNull FrameLayout container) {
        if (!Boolean.TRUE.equals(initialized)) {
            return;
        }

        String adUnitId = BuildConfig.adUnits.get("applovin.ad.banner");
        if (adUnitId == null || adUnitId.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Ad unit ID not found, see logs for details", Toast.LENGTH_SHORT).show();

            Log.w(LOG_TAG, "local.properties does not define 'applovin.ad.banner'");
            return;
        }

        loadButton.setEnabled(false);

        MaxAdView adView = new MaxAdView(adUnitId, this);
        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.v(LOG_TAG, "onAdLoaded: " + ad);

                loadButton.setEnabled(true);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + adUnitId + ", error: " + error);

                Toast.makeText(getApplicationContext(), "Failed to load Banner", Toast.LENGTH_SHORT).show();

                loadButton.setEnabled(true);
            }

            @Override
            public void onAdExpanded(MaxAd ad) {
                Log.v(LOG_TAG, "onAdExpanded: " + ad);
            }

            @Override
            public void onAdCollapsed(MaxAd ad) {
                Log.v(LOG_TAG, "onAdCollapsed: " + ad);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.v(LOG_TAG, "onAdDisplayed: " + ad);
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.v(LOG_TAG, "onAdHidden: " + ad);
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.v(LOG_TAG, "onAdClicked: " + ad);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.v(LOG_TAG, "onAdDisplayFailed: " + ad + ", error: " + error);
            }
        });

        int heightPx = getResources().getDimensionPixelSize(R.dimen.banner_height);

        container.removeAllViews();
        container.addView(adView, new FrameLayout.LayoutParams(MATCH_PARENT, heightPx, Gravity.CENTER));

        adView.loadAd();
    }

    private void onLoadMrecClicked(@NonNull View loadButton, @NonNull FrameLayout container) {
        if (!Boolean.TRUE.equals(initialized)) {
            return;
        }

        String adUnitId = BuildConfig.adUnits.get("applovin.ad.mrec");
        if (adUnitId == null || adUnitId.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Ad unit ID not found, see logs for details", Toast.LENGTH_SHORT).show();

            Log.w(LOG_TAG, "local.properties does not define 'applovin.ad.mrec'");
            return;
        }

        loadButton.setEnabled(false);

        MaxAdView adView = new MaxAdView(adUnitId, MaxAdFormat.MREC, this);
        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.v(LOG_TAG, "onAdLoaded: " + ad);

                loadButton.setEnabled(true);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + adUnitId + ", error: " + error);

                Toast.makeText(getApplicationContext(), "Failed to load Mrec", Toast.LENGTH_SHORT).show();

                loadButton.setEnabled(true);
            }

            @Override
            public void onAdExpanded(MaxAd ad) {
                Log.v(LOG_TAG, "onAdExpanded: " + ad);
            }

            @Override
            public void onAdCollapsed(MaxAd ad) {
                Log.v(LOG_TAG, "onAdCollapsed: " + ad);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.v(LOG_TAG, "onAdDisplayed: " + ad);
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.v(LOG_TAG, "onAdHidden: " + ad);
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.v(LOG_TAG, "onAdClicked: " + ad);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.v(LOG_TAG, "onAdDisplayFailed: " + ad + ", error: " + error);
            }
        });

        int widthPx = getResources().getDimensionPixelSize(R.dimen.mrec_width);
        int heightPx = getResources().getDimensionPixelSize(R.dimen.mrec_height);

        container.removeAllViews();
        container.addView(adView, new FrameLayout.LayoutParams(widthPx, heightPx, Gravity.CENTER));

        adView.loadAd();
    }

    private void onLoadNativeClicked(@NonNull View loadButton, @NonNull ViewGroup container) {
        if (!Boolean.TRUE.equals(initialized)) {
            return;
        }

        String adUnitId = BuildConfig.adUnits.get("applovin.ad.native");

        if (adUnitId == null || adUnitId.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Ad unit ID not found, see logs for details", Toast.LENGTH_SHORT).show();

            Log.w(LOG_TAG, "local.properties does not define 'applovin.ad.native'");
            return;
        }

        loadButton.setEnabled(false);

        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(adUnitId, this);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(MaxNativeAdView adView, MaxAd ad) {
                Log.v(LOG_TAG, "onNativeAdLoaded: " + ad);

                loadButton.setEnabled(true);

                container.addView(adView);
            }

            @Override
            public void onNativeAdLoadFailed(String adUnitId, MaxError error) {
                Log.v(LOG_TAG, "onNativeAdLoadFailed: " + adUnitId + ", error: " + error);

                Toast.makeText(getApplicationContext(), "Failed to load Native", Toast.LENGTH_SHORT).show();

                loadButton.setEnabled(true);
            }

            @Override
            public void onNativeAdClicked(MaxAd ad) {
                Log.v(LOG_TAG, "onNativeAdClicked: " + ad);
            }
        });

        nativeAdLoader.loadAd();
    }

    private void onLoadInterstitialClicked(@NonNull View loadButton, @NonNull View showButton) {
        if (!Boolean.TRUE.equals(initialized)) {
            return;
        }

        String adUnitId = BuildConfig.adUnits.get("applovin.ad.interstitial");

        if (adUnitId == null || adUnitId.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Ad unit ID not found, see logs for details", Toast.LENGTH_SHORT).show();

            Log.w(LOG_TAG, "local.properties does not define 'applovin.ad.interstitial'");
            return;
        }

        loadButton.setEnabled(false);
        showButton.setEnabled(false);

        MaxInterstitialAd interstitialAd = new MaxInterstitialAd(adUnitId, this);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.v(LOG_TAG, "onAdLoaded");

                loadButton.setEnabled(true);
                showButton.setEnabled(true);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + adUnitId + ", error: " + error);

                Toast.makeText(getApplicationContext(), "Failed to load Interstitial", Toast.LENGTH_SHORT).show();

                loadButton.setEnabled(true);
                showButton.setEnabled(false);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.v(LOG_TAG, "onAdDisplayed: " + ad);

                showButton.setEnabled(false);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.v(LOG_TAG, "onAdDisplayFailed: " + ad);

                showButton.setEnabled(false);
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.v(LOG_TAG, "onAdHidden: " + ad);
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.v(LOG_TAG, "onAdClicked: " + ad);
            }
        });

        interstitialAd.loadAd();

        showButton.setOnClickListener(v -> {
            if (interstitialAd.isReady()) {
                interstitialAd.showAd();
            }
        });
    }

    private void onLoadRewardedClicked(@NonNull View loadButton, @NonNull View showButton) {
        if (!Boolean.TRUE.equals(initialized)) {
            return;
        }

        String adUnitId = BuildConfig.adUnits.get("applovin.ad.rewarded");

        if (adUnitId == null || adUnitId.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Ad unit ID not found, see logs for details", Toast.LENGTH_SHORT).show();

            Log.w(LOG_TAG, "local.properties does not define 'applovin.ad.rewarded'");
            return;
        }

        loadButton.setEnabled(false);
        showButton.setEnabled(false);

        MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(adUnitId, this);
        rewardedAd.setListener(new MaxRewardedAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.v(LOG_TAG, "onAdLoaded");

                loadButton.setEnabled(true);
                showButton.setEnabled(true);
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + adUnitId + ", error: " + error);

                Toast.makeText(getApplicationContext(), "Failed to load Rewarded", Toast.LENGTH_SHORT).show();

                loadButton.setEnabled(true);
                showButton.setEnabled(false);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.v(LOG_TAG, "onAdDisplayed: " + ad);

                showButton.setEnabled(false);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.v(LOG_TAG, "onAdDisplayFailed: " + ad);

                showButton.setEnabled(false);
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.v(LOG_TAG, "onAdHidden: " + ad);
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.v(LOG_TAG, "onAdClicked: " + ad);
            }

            @Override
            public void onRewardedVideoStarted(MaxAd ad) {
                Log.v(LOG_TAG, "onRewardedVideoStarted: " + ad);
            }

            @Override
            public void onRewardedVideoCompleted(MaxAd ad) {
                Log.v(LOG_TAG, "onRewardedVideoCompleted: " + ad);
            }

            @Override
            public void onUserRewarded(MaxAd ad, MaxReward reward) {
                Log.v(LOG_TAG, "onUserRewarded: " + ad + ", reward: " + reward);

                Toast.makeText(getApplicationContext(), "You've gained a reward!", Toast.LENGTH_SHORT).show();
            }
        });

        rewardedAd.loadAd();

        showButton.setOnClickListener(v -> {
            if (rewardedAd.isReady()) {
                rewardedAd.showAd();
            }
        });
    }
}