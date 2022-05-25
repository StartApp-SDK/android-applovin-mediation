package com.startapp.example.applovin;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
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
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.sdk.AppLovinSdk;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.util.Map;

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

            if (BuildConfig.DEBUG) {
                StartAppSDK.setTestAdsEnabled(true);
            }

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

        setTitle("AppLovin " + AppLovinSdk.VERSION + " - Start.io " + StartAppSDK.getVersion());

        setContentView(R.layout.activity_main);

        findViewById(R.id.load_banner).setOnClickListener(view -> {
            onLoadBannerClicked(view, findViewById(R.id.container));
        });

        findViewById(R.id.load_mrec).setOnClickListener(view -> {
            onLoadMrecClicked(view, findViewById(R.id.container));
        });

        findViewById(R.id.load_native_small).setOnClickListener(view -> {
            onLoadNativeClicked(view, findViewById(R.id.container), "small");
        });

        findViewById(R.id.load_native_medium).setOnClickListener(view -> {
            onLoadNativeClicked(view, findViewById(R.id.container), "medium");
        });

        findViewById(R.id.load_native_manual).setOnClickListener(view -> {
            onLoadNativeClicked(view, findViewById(R.id.container), "manual");
        });

        findViewById(R.id.load_interstitial).setOnClickListener(view -> {
            onLoadInterstitialClicked(view, findViewById(R.id.show_interstitial));
        });

        findViewById(R.id.load_rewarded).setOnClickListener(view -> {
            onLoadRewardedClicked(view, findViewById(R.id.show_rewarded));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        StringBuilder sb = null;

        Map<String, ?> values = getPreferences(MODE_PRIVATE).getAll();
        if (values != null) {
            for (Map.Entry<String, ?> e : values.entrySet()) {
                if (sb == null) {
                    sb = new StringBuilder("Rewards:\n");
                }

                sb.append(e.getKey()).append(" = ").append(e.getValue()).append('\n');
            }
        }

        TextView rewardsTextView = findViewById(R.id.rewards);
        rewardsTextView.setText(sb);
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

    private void onLoadNativeClicked(@NonNull View loadButton, @NonNull ViewGroup container, @NonNull String type) {
        if (!Boolean.TRUE.equals(initialized)) {
            return;
        }

        String key = "applovin.ad.native." + type;

        String adUnitId = BuildConfig.adUnits.get(key);

        if (adUnitId == null || adUnitId.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Ad unit ID not found, see logs for details", Toast.LENGTH_SHORT).show();

            Log.w(LOG_TAG, "local.properties does not define '" + key + "'");
            return;
        }

        loadButton.setEnabled(false);

        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(adUnitId, this);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(MaxNativeAdView adView, MaxAd ad) {
                Log.v(LOG_TAG, "onNativeAdLoaded: " + adView + ", " + ad);

                loadButton.setEnabled(true);

                if (adView != null) {
                    container.removeAllViews();
                    container.addView(adView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, Gravity.CENTER));
                }
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

        if ("manual".equals(type)) {
            MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(R.layout.native_custom_ad_view)
                    .setTitleTextViewId(R.id.title_text_view)
                    .setBodyTextViewId(R.id.body_text_view)
                    .setAdvertiserTextViewId(R.id.advertiser_textView)
                    .setIconImageViewId(R.id.icon_image_view)
                    .setMediaContentViewGroupId(R.id.media_view_container)
                    .setOptionsContentViewGroupId(R.id.options_view)
                    .setCallToActionButtonId(R.id.cta_button)
                    .build();

            nativeAdLoader.loadAd(new MaxNativeAdView(binder, this));
        } else {
            nativeAdLoader.loadAd();
        }
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

                SharedPreferences prefs = getPreferences(MODE_PRIVATE);

                String key = reward.getLabel();
                if (key == null || key.isEmpty()) {
                    key = "<default>";
                }

                prefs.edit()
                        .putInt(key, prefs.getInt(key, 0) + Math.max(reward.getAmount(), 1))
                        .apply();
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
