package com.miczon.photoeditor.eventListeners;

import com.google.android.gms.ads.LoadAdError;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

/**
 * To handle callbacks for Banner Ad
 */
public interface BannerAdLoadListener {

    void onAdClicked();

    void onAdLoaded();

    void onAdFailedToLoad(LoadAdError loadAdError);

}
