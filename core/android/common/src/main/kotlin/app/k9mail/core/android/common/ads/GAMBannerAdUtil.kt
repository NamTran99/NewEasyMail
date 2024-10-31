package app.k9mail.core.android.common.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import app.k9mail.core.android.common.R
import app.k9mail.core.android.common.data.FireBaseScreenEvent
import app.k9mail.core.android.common.data.FirebaseUtil
import app.k9mail.core.android.common.Utils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView

object GAMBannerAdUtil {
    suspend fun inflateBannerAd(activity: Activity, container: ViewGroup?){
        if(Utils.isAppPurchased()
            || container == null){
            Log.d("hungnd", "inflateBannerAd: not inflate, return")
            return
        }
        container.isVisible = true
        val bannerAdView = getInstanceBannerAdView(activity)
        val adSize = bannerAdView?.adSize?.getHeightInPixels(activity)
        if(bannerAdView == null || adSize == null){
            Log.d("hungnd", "inflateBannerAd: cannot get adView or adSize")
            return
        }
        val params = container.layoutParams
        params.height = (adSize
            + activity.resources
            .getDimension(R.dimen.banner_ad_top_padding) //add top padding to prevent policy violation
            + 1).toInt()
        container.layoutParams = params
        container.removeAllViews()
        (bannerAdView.parent as? ViewGroup)?.removeAllViews()
        bannerAdView.adListener = object : AdListener(){
            override fun onAdLoaded() {
                val p = container.layoutParams
                p.height = ViewGroup.LayoutParams.WRAP_CONTENT
                container.layoutParams = p
//                container.setPadding(
//                    0,
//                    context.resources.getDimension(R.dimen.banner_ad_top_padding) as Int,
//                    0,
//                    0,
//                )
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                container.isVisible = false
            }
        }
        container.addView(getDividerView(activity))
        container.addView(bannerAdView)
        FirebaseUtil.logEvent(FireBaseScreenEvent.SHOW_BANNER_ADS)
    }

    @SuppressLint("MissingPermission")
    private fun getInstanceBannerAdView(activity: Activity): AdManagerAdView? {
        val adView = AdManagerAdView(activity)
        adView.adUnitId = AdIds.bannerAdId
        adView.setAdSize(AdSize.BANNER)
        adView.isVisible = true
        val adRequest = AdManagerAdRequest.Builder().build()
        try {
            adView.loadAd(adRequest)
        } catch (e: Exception) {
            return null
        }
        return adView
    }

    private fun getDividerView(activity: Activity): View {
        val separatorView = View(activity)

        // Set layout parameters
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, // Width = match_parent
            activity.resources.getDimension(R.dimen.banner_ad_top_padding).toInt(),
        )
        separatorView.layoutParams = params

        // Set background color to gray
        separatorView.setBackgroundColor(Color.WHITE)
        return separatorView
    }
}
