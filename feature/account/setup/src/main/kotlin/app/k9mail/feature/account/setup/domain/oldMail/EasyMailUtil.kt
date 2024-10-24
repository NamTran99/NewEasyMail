package app.k9mail.feature.account.setup.domain.oldMail

import app.k9mail.autodiscovery.api.AuthenticationType
import app.k9mail.autodiscovery.api.AutoDiscoveryResult
import app.k9mail.autodiscovery.api.ConnectionSecurity
import app.k9mail.autodiscovery.api.ImapServerSettings
import app.k9mail.autodiscovery.api.SmtpServerSettings
import app.k9mail.core.android.common.data.FireBaseScreenEvent
import app.k9mail.core.android.common.data.FirebaseUtil
import app.k9mail.core.common.net.Hostname
import app.k9mail.core.common.net.Port
import com.fsck.k9.helper.EmailHelper
import com.hungbang.email2018.f.c.SignInConfigs
import com.hungbang.email2018.f.c.a
import io.paperdb.Paper

object EasyMailUtil {
    /**
     * used to get account email & password
     */
    fun getSavedAccountFromEasyMail(): a? {
            return Paper.book().read<a>("CURRENT_ACCOUNT", null)
//        val fakeAcc = a(
//            "trandinhnam1199@yandex.com",
//            7,
//            "sbfksbfprvricpvk",
//            "This is signature",
//        )
//        return fakeAcc
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

    suspend fun getDiscoverySettingsFromFirebaseConfig(emailAddress: String): AutoDiscoveryResult.Settings? {
        val savedConfigFromFirebase = EmailHelper.getDomainFromEmailAddress(emailAddress)
            ?.let { FirebaseUtil.getSignInConfigFromFirebase(it) } ?: return null
        try {
            val serverSettings = AutoDiscoveryResult.Settings(
                incomingServerSettings = ImapServerSettings(
                    hostname = Hostname(savedConfigFromFirebase.imap_host),
                    port = Port(savedConfigFromFirebase.imap_port.toInt()),
                    connectionSecurity = ConnectionSecurity.TLS,
                    authenticationTypes = listOf(AuthenticationType.PasswordCleartext),
                    username = emailAddress
                ),
                outgoingServerSettings = SmtpServerSettings(
                    hostname = Hostname(savedConfigFromFirebase.smtp_host),
                    port = Port(savedConfigFromFirebase.smtp_port.toInt()),
                    connectionSecurity = if (savedConfigFromFirebase.isSmtpStartTLS()) ConnectionSecurity.StartTLS else ConnectionSecurity.TLS,
                    authenticationTypes = listOf(AuthenticationType.PasswordCleartext),
                    username = emailAddress
                ),
                source = ""
            )
            FirebaseUtil.logEvent(FireBaseScreenEvent.GET_DISCOVERY_SETTING_FROM_FIREBASE_SUCCESS)
            return serverSettings
        } catch (e: Exception) {
            return null
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
