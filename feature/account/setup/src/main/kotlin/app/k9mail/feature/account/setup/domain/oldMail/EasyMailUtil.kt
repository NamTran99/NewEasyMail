package app.k9mail.feature.account.setup.domain.oldMail

import app.k9mail.core.android.common.data.FirebaseUtil
import com.hungbang.email2018.f.c.SignInConfigs
import com.hungbang.email2018.f.c.a

object EasyMailUtil {
    /**
     * used to get account email & password
     */
    fun getSavedAccountFromEasyMail(): a? {
//            return Paper.book().read<a>("CURRENT_ACCOUNT", null)
        val fakeAcc = a(
            "trandinhnam1199@yandex.com",
            3,
            "sbfksbfprvricpvk",
            "This is signature",
        )
        return fakeAcc
    }

    /**
     * used to get host, port...
     */
    suspend fun getSavedSignInConfigFromEasyMail(mailDomain: String?): SignInConfigs? {
        if (mailDomain == null) {
            return null
        }
        return try {
            FirebaseUtil.getSignInConfigFromFirebase(mailDomain)
        } catch (e: Exception) {
            null
        }
    }

//    fun testGetSavedDataFromEasyMail(){
//        val acc = getSavedAccountFromEasyMail()
//        Log.d("hungnd", "testGetSavedDataFromEasyMail: saved acc: $acc")
//        if(acc != null){
//            val domain = EmailHelper.getDomainFromEmailAddress(acc.accountEmail)
//            val configs = domain?.let { getSavedSignInConfigFromEasyMail(it) }
//            Log.d("hungnd", "testGetSavedDataFromEasyMail: configs: $configs")
//        } else {
//            Log.d("hungnd", "testGetSavedDataFromEasyMail: no saved acc")
//        }
//    }
}
