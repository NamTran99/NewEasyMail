package app.k9mail.core.android.common.ads

object AdIds {
    val bannerAdId by lazy { getBannerAd() }
    val interAdId by lazy { getInterAd() }

    private fun getBannerAd(): String {
        val debugIds = "/6499/example/banner"
        val newAdId = "/424536528,23041610225/1524961_banner_com.emailclient.mailchecker.outlook.hotmail"
        return newAdId
    }

    private fun getInterAd(): String {
        val debugIds = "/6499/example/interstitial"
        val newAdId = "/424536528,23041610225/1524962_interstitial_com.emailclient.mailchecker.outlook.hotmail"
        return newAdId
    }
}
