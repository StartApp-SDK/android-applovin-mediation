package com.applovin.mediation.adapters;

import static com.applovin.mediation.adapter.MaxAdapterError.INTERNAL_ERROR;
import static com.startapp.adapter.applovin.BuildConfig.DEBUG;
import static com.startapp.adapter.applovin.BuildConfig.VERSION_NAME;

import android.app.Activity;
import android.util.Log;

import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.adapter.MaxAdViewAdapter;
import com.applovin.mediation.adapter.MaxInterstitialAdapter;
import com.applovin.mediation.adapter.MaxNativeAdAdapter;
import com.applovin.mediation.adapter.MaxRewardedAdapter;
import com.applovin.mediation.adapter.MaxRewardedInterstitialAdapter;
import com.applovin.mediation.adapter.MaxSignalProvider;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxRewardedAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxRewardedInterstitialAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxSignalCollectionListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterInitializationParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterSignalCollectionParameters;
import com.applovin.sdk.AppLovinSdk;

public class StartAppMediationAdapter extends MediationAdapterBase implements MaxInterstitialAdapter, MaxRewardedInterstitialAdapter, MaxRewardedAdapter, MaxAdViewAdapter, MaxNativeAdAdapter, MaxSignalProvider {
    private static final String LOG_TAG = StartAppMediationAdapter.class.getSimpleName();

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
    }

    @Override
    public String getSdkVersion() {
        if (DEBUG) {
            Log.v(LOG_TAG, "getSdkVersion");
        }

        return "1.0.0";
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
    public void loadAdViewAd(MaxAdapterResponseParameters parameters, MaxAdFormat format, Activity activity, MaxAdViewAdapterListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "loadAdViewAd");
        }

        listener.onAdViewAdLoadFailed(INTERNAL_ERROR);
    }

    @Override
    public void loadInterstitialAd(MaxAdapterResponseParameters parameters, Activity activity, MaxInterstitialAdapterListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "loadInterstitialAd");
        }

        listener.onInterstitialAdLoadFailed(INTERNAL_ERROR);
    }

    @Override
    public void showInterstitialAd(MaxAdapterResponseParameters parameters, Activity activity, MaxInterstitialAdapterListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "showInterstitialAd");
        }

        listener.onInterstitialAdDisplayFailed(INTERNAL_ERROR);
    }

    @Override
    public void loadRewardedAd(MaxAdapterResponseParameters parameters, Activity activity, MaxRewardedAdapterListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "loadRewardedAd");
        }

        listener.onRewardedAdLoadFailed(INTERNAL_ERROR);
    }

    @Override
    public void showRewardedAd(MaxAdapterResponseParameters parameters, Activity activity, MaxRewardedAdapterListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "showRewardedAd");
        }

        listener.onRewardedAdDisplayFailed(INTERNAL_ERROR);
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
    public void collectSignal(MaxAdapterSignalCollectionParameters parameters, Activity activity, MaxSignalCollectionListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "collectSignal");
        }

        listener.onSignalCollectionFailed(null);
    }
}
