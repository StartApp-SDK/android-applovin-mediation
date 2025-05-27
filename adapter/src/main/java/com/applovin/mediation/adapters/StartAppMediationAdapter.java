package com.applovin.mediation.adapters;

import androidx.annotation.Keep;

import com.applovin.sdk.AppLovinSdk;

@Keep
public class StartAppMediationAdapter extends StartIoMediationAdapter {
    public StartAppMediationAdapter(AppLovinSdk sdk) {
        super(sdk);
    }
}
