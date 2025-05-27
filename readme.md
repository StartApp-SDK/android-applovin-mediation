# Start.io mediation adapter for AppLovin MAX

This library lets you serve ads to your apps from Start.io (formerly StartApp) network via AppLovin MAX mediation.

## Supported formats

- Banner 320x50
- Banner 300x250 (Medium Rectangle)
- Interstitial
- Rewarded Video
- Native

## Setup Start.io

1. [Open a Publisher account][1]
2. [Add an App][2]
3. Make sure you can find an **Account ID** and an **App ID**
![Account Id, App ID](images/step0.png)

## Setup project

Add dependency on Start.io mediation adapter for AppLovin MAX

```groovy
dependencies {
    // noinspection GradleDependency
    implementation 'io.start:applovin-mediation:3.1+'
}
```

## Setup AppLovin MAX

1. Add Start.io network as a Custom Network for mediation. Navigate to Mediation -> Manage -> Networks and click corresponding button.

![Step 1](/images/step1.png)

2. Choose `Network Type` value `SDK` and fill other parameters:

**Custom Network Name**

```
Start.io
```

**iOS Adapter Class Name**

```
StartioAppLovinAdapter
```

**Android / Fire OS Adapter Class Name**

```
com.applovin.mediation.adapters.StartIoMediationAdapter
```

![Step 2](/images/step2.png)

3. Configure newly added network to work with your Ad Units. Navigate to Mediation -> Manage -> Ad Units.

Then choose an Ad Unit you want to use with Start.io and scroll down to section `Custom Networks & Deals`.

Toggle switch `Status` to make it activated and displayed in green.

Field `App ID` is mandatory, no matter that in dashboard it is marked as optional.

Field `Placement ID`: put the **line item ID** if you have one, or put the string `default` instead.

Field `CPM Price`: this value determines the order in which adapters are traversed in the waterfall.

**Important**: You have to use the same App ID from the Start.io portal for all Ad Units in frame of single app!

![Step 3](/images/step3.png)

## Advanced configuration

You can pass plain JSON string into the field `Custom Parameters` for advanced configuration:

```json
{
    "adTag": "string",
    "interstitialMode": "string", // one of "overlay", "video", "offerwall"
    "minCPM": "number",
    "is3DBanner": "boolean", // applicable only for banner 320x50
    "nativeImageSize": "string", // one of "72x72", "100x100", "150x150", "340x340", "1200x628"
    "nativeSecondaryImageSize": "string" // one of "72x72", "100x100", "150x150", "340x340", "1200x628"
}
```

## Testing

The [demo app](/example) is fully workable. Change SDK key and ad unit IDs in [ad_ids.xml](/example/src/main/res/values/ad_ids.xml), then build and run the app to make sure your integration is working correctly.

**Important**: AppLovin mediation doesn't work with test mode. Thus, if you set test devices IDs during AppLovin configuration or if you run test app on emulator, you won't get Start.io ads.  

 [1]: https://support.start.io/hc/en-us/articles/202766673
 [2]: https://support.start.io/hc/en-us/articles/202766743
