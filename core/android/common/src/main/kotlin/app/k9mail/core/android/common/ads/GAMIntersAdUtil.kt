package app.k9mail.core.android.common.ads

import android.app.Activity
import app.k9mail.core.android.common.Utils
import app.k9mail.core.android.common.data.FireBaseScreenEvent
import app.k9mail.core.android.common.data.FirebaseUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback

object GAMIntersAdUtil {
    var mAdManagerInterstitialAd: AdManagerInterstitialAd? = null
    var adListener: AdListener? = null

    suspend fun showAd(activity: Activity, adListener: AdListener){
        if( Utils.isAppPurchased()
            || !Utils.isReadyToShowFullScreenAd()){
            adListener.onAdClosed()
            return
        }
        if(mAdManagerInterstitialAd != null){
            this.adListener = adListener
            mAdManagerInterstitialAd?.show(activity)
            Utils.lastTimeShowFullScreenAd = System.currentTimeMillis()
            FirebaseUtil.logEvent(FireBaseScreenEvent.SHOW_INTERS_AD)
        } else {
            adListener.onAdClosed()
        }
    }

    suspend fun fetchAd(activity: Activity){
        if(Utils.isAppPurchased()
            || mAdManagerInterstitialAd != null){
            return
        }
        val adRequest = AdManagerAdRequest.Builder().build()
        AdManagerInterstitialAd.load(
            activity, AdIds.interAdId, adRequest,
            object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdLoaded(interAd: AdManagerInterstitialAd) {
                    mAdManagerInterstitialAd = interAd
                    mAdManagerInterstitialAd?.fullScreenContentCallback = getFullScreenContentCallback()
                }
            },
        )
    }

    private fun getFullScreenContentCallback(): FullScreenContentCallback? {
        return object : FullScreenContentCallback(){
            override fun onAdDismissedFullScreenContent() {
                adListener?.onAdClosed()
            }

            override fun onAdShowedFullScreenContent() {
                mAdManagerInterstitialAd = null
            }
        }
    }

    suspend fun isAdAvailable(): Boolean {
        return (mAdManagerInterstitialAd != null && Utils.isReadyToShowFullScreenAd()
            && !Utils.isAppPurchased())
    }
}
