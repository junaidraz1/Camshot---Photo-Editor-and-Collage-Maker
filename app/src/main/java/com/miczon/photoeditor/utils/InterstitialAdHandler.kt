package com.miczon.photoeditor.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.miczon.photoeditor.activity.ApplyEffectActivity

class InterstitialAdHandler {

    companion object {

        var isInterstitialAdShowing = false
        private var mAdMobInterstitial: InterstitialAd? = null

        fun loadInterstitialAd(context: Context, interstitialAdId: String) {
            val adRequest: AdRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                interstitialAdId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        super.onAdLoaded(interstitialAd)
                        mAdMobInterstitial = interstitialAd
                        Log.v("InterstitialAd", "AdMob Interstitial Ad Successfully Loaded!")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        mAdMobInterstitial = null
                        Log.v(
                            "InterstitialAd",
                            "AdMob Interstitial Ad Failed to Load! $loadAdError"
                        )
                    }
                })
        }

        fun showInterstitialAd(
            activity: Activity,
            interstitialAdId: String,
            onAdDismiss: () -> Unit
        ) {
            if (activity is ApplyEffectActivity) {
                if (mAdMobInterstitial != null) {
                    mAdMobInterstitial?.show(activity)
                    mAdMobInterstitial?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                onAdDismiss()
                                mAdMobInterstitial = null
                                isInterstitialAdShowing = false
                                loadInterstitialAd(activity, interstitialAdId)
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                onAdDismiss()
                                mAdMobInterstitial = null
                                isInterstitialAdShowing = false
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                isInterstitialAdShowing = true
                            }
                        }
                } else {
                    onAdDismiss()
                }
            } else if (Constants.countClick % 3 == 0) {
                if (mAdMobInterstitial != null) {
                    mAdMobInterstitial?.show(activity)
                    mAdMobInterstitial?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                onAdDismiss()
                                mAdMobInterstitial = null
                                isInterstitialAdShowing = false
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                onAdDismiss()
                                mAdMobInterstitial = null
                                isInterstitialAdShowing = false
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                isInterstitialAdShowing = true
                            }
                        }
                } else
                    onAdDismiss()
            } else {
                onAdDismiss()
                if (Constants.countClick % 2 == 0) {
                    loadInterstitialAd(activity, interstitialAdId)
                }
            }
            Constants.countClick++
        }
    }
}