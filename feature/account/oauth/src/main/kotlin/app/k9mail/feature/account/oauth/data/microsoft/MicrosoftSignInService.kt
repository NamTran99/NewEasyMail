package app.k9mail.feature.account.oauth.data.microsoft

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RawRes
import app.k9mail.feature.account.common.domain.AccountDomainContract
import app.k9mail.feature.account.common.domain.entity.AuthorizationState
import app.k9mail.feature.account.common.domain.entity.OauthMailType
import app.k9mail.feature.account.oauth.domain.entity.AuthorizationResult
import com.google.gson.Gson
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication
import com.microsoft.identity.client.IPublicClientApplication.IMultipleAccountApplicationCreatedListener
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MicrosoftSignInService(
    @RawRes val configID: Int,
    val accountStateRepository: AccountDomainContract.AccountStateRepository,
) :
    IMicrosoftSignIn {
    private val scope: List<String> = listOf(
        "https://outlook.office.com/IMAP.AccessAsUser.All",
        "https://outlook.office.com/SMTP.Send",
    )
    private var mFirstAccount: IAccount? = null
    private var error: MsalException? = null
    private var mMultipleAccountApp: IMultipleAccountPublicClientApplication? = null

    override fun init(context: Context) {
        PublicClientApplication.createMultipleAccountPublicClientApplication(
            context, configID,
            object : IMultipleAccountApplicationCreatedListener {
                override fun onCreated(application: IMultipleAccountPublicClientApplication?) {
                    error = null
                    mMultipleAccountApp = application
                }

                override fun onError(exception: MsalException?) {
                    error = exception
                }
            },
        )
    }

    override suspend fun refreshToken(accountId: String): IAuthenticationResult? = suspendCoroutine { continuation ->
        mMultipleAccountApp?.let {
            val builder: AcquireTokenSilentParameters.Builder = AcquireTokenSilentParameters.Builder().apply {
                fromAuthority(it.configuration.defaultAuthority.authorityURL.toString())
                withScopes(scope)
                forAccount(it.getAccount(accountId))
                withCallback(
                    object : AuthenticationCallback {
                        override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                            Log.d("TAG", "onSuccess: NamTD8 ")
                            continuation.resume(authenticationResult)
                        }

                        override fun onError(exception: MsalException?) {
                            Log.d("TAG", "onError: NamTD8  ${exception?.localizedMessage}")
                            continuation.resume(null)
                        }

                        override fun onCancel() {
                            continuation.resume(null)
                        }
                    },
                )
            }
            it.acquireTokenSilentAsync(builder.build())
        }?: continuation.resume(null)
    }

    override suspend fun requestLogin(
        context: ComponentActivity,
    ): AuthorizationResult = suspendCoroutine { continuation ->
        if (mMultipleAccountApp == null) continuation.resume(
            AuthorizationResult.Failure(
                Exception(
                    error?.localizedMessage ?: "Unknown error",
                ),
            ),
        )
        val builder: AcquireTokenParameters.Builder = AcquireTokenParameters.Builder().apply {
            startAuthorizationFromActivity(context)
            withScopes(scope)
                .withCallback(
                    object : AuthenticationCallback {
                        override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                            Log.d("TAG", "success: NamTD8-callback  ")
                            mFirstAccount = authenticationResult?.account
                            mFirstAccount?.let { it ->
                                it.claims?.let { it["email"] ?: it["preferred_username"] }?.let {
                                    accountStateRepository.setEmailAddress(it.toString())
                                }

                                continuation.resume(
                                    AuthorizationResult.Success(
                                        AuthorizationState(
                                            value = Gson().toJson(authenticationResult?.convertToMicrosoftAuthResult()),
                                            type = OauthMailType.Microsoft,
                                        ),
                                    ),
                                )
                            } ?: AuthorizationResult.Failure(Exception("Unknown error"))
                        }

                        override fun onError(exception: MsalException?) {
                            Log.d("TAG", "Error: NamTD8-callback ${exception?.localizedMessage} ")
                            continuation.resume(
                                AuthorizationResult.Failure(
                                    Exception(
                                        exception?.localizedMessage ?: "Unknown error",
                                    ),
                                ),
                            )
                        }

                        override fun onCancel() {
                            continuation.resume(AuthorizationResult.Canceled)
                        }
                    },
                )
        }
        mMultipleAccountApp?.acquireToken(builder.build())
    }
}
