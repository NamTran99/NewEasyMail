package app.k9mail.core.android.common

import app.k9mail.core.android.common.data.DataStoreHelper
import app.k9mail.core.android.common.data.RemoteConfig

class Utils {
    companion object {
        var lastTimeShowFullScreenAd = 0L
        suspend fun isAppPurchased(): Boolean {
            return DataStoreHelper.readData(DataStoreHelper.Key.IS_APP_PURCHASED, false)
        }

        fun isReadyToShowFullScreenAd(): Boolean {
            val deltaTime = System.currentTimeMillis() - lastTimeShowFullScreenAd
            val isReady = deltaTime >= RemoteConfig.FULL_SCREEN_AD_INTERVAL_MS
            return isReady
        }

        suspend fun setIsAppPurchased(value: Boolean) {
            DataStoreHelper.saveData(DataStoreHelper.Key.IS_APP_PURCHASED, value)
        }
    }
}
