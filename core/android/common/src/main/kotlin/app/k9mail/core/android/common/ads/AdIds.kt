package app.k9mail.core.android.common.ads

object AdIds {
    val bannerAdId by lazy { getBannerAd() }
    val interAdId by lazy { getInterAd() }

    private fun getBannerAd(): String {
        val debugIds = "/6499/example/banner"
        val newAdId = "/424536528,22760543955/1524228_banner_mail.emailapp.easymail2018"
        return newAdId
    }

    private fun getInterAd(): String {
        val debugIds = "/6499/example/interstitial"
        val newAdId = "/424536528,23041610225/1524226_interstitial_mail.emailapp.easymail2018"
        return newAdId
    }
}
