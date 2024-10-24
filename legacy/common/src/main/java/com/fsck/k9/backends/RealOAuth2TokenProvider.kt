package com.fsck.k9.backends

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import app.k9mail.feature.account.oauth.data.microsoft.IMicrosoftSignIn
import app.k9mail.feature.account.oauth.data.microsoft.MicrosoftAuthResult
import app.k9mail.feature.account.oauth.data.microsoft.convertToMicrosoftAuthResult
import com.fsck.k9.CommonApp
import com.fsck.k9.entity.AuthorizationState
import com.fsck.k9.entity.OauthMailType
import com.fsck.k9.mail.AuthenticationFailedException
import com.fsck.k9.mail.oauth.AuthStateStorage
import com.fsck.k9.mail.oauth.OAuth2TokenProvider
import com.google.gson.Gson
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors
import net.openid.appauth.AuthorizationException.GeneralErrors
import net.openid.appauth.AuthorizationService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class RealOAuth2TokenProvider(
    context: Context,
    private val authStateStorage: AuthStateStorage,
) : OAuth2TokenProvider, KoinComponent {
    private val authService = AuthorizationService(context)
    private var requestFreshToken = false
    private var authState: AuthorizationState? = null
    private var timeOut: Long = 3000

    @Suppress("TooGenericExceptionCaught")
    override fun getToken(timeoutMillis: Long): String {
        timeOut = timeoutMillis
        authState = authStateStorage.getAuthorizationState()
        return authState?.let {
            when (it.type) {
                OauthMailType.Microsoft -> getMicrosoftToken()
                OauthMailType.Other -> getTokenCommon()
            }
        } ?: throw AuthenticationFailedException("Failed to fetch an access token")
    }

    private fun getTokenCommon(): String? {
        val latch = CountDownLatch(1)
        var token: String? = null
        var exception: AuthorizationException? = null

        val authState = authState?.value?.let { AuthState.jsonDeserialize(it) }

        if (requestFreshToken) {
            authState?.needsTokenRefresh = true
        }

        val oldAccessToken = authState?.accessToken

        try {
            authState?.performActionWithFreshTokens(
                authService,
            ) { accessToken: String?, _, authException: AuthorizationException? ->
                token = accessToken
                exception = authException

                latch.countDown()
            }

            latch.await(timeOut, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            Timber.w(e, "Failed to fetch an access token. Clearing authorization state.")

            authStateStorage.updateAuthorizationState(authorizationState = null)

            throw AuthenticationFailedException(
                message = "Failed to fetch an access token",
                throwable = e,
            )
        }

        val authException = exception
        if (authException == GeneralErrors.NETWORK_ERROR ||
            authException == GeneralErrors.SERVER_ERROR ||
            authException == AuthorizationRequestErrors.SERVER_ERROR ||
            authException == AuthorizationRequestErrors.TEMPORARILY_UNAVAILABLE
        ) {
            throw IOException("Error while fetching an access token", authException)
        } else if (authException != null) {
            authStateStorage.updateAuthorizationState(authorizationState = null)

            throw AuthenticationFailedException(
                message = "Failed to fetch an access token",
                throwable = authException,
                messageFromServer = authException.error,
            )
        } else if (token != oldAccessToken) {
            requestFreshToken = false
            authStateStorage.updateAuthorizationState(authorizationState =  AuthorizationState(authState?.jsonSerializeString(), OauthMailType.Other) )
        }

        return token
    }

    private fun getMicrosoftToken(): String {
        val scope = CoroutineScope(Dispatchers.IO)
        val microsoftService: IMicrosoftSignIn by inject()

        val authState = authStateStorage.getAuthorizationState()
            ?.let { Gson().fromJson(it.value, MicrosoftAuthResult::class.java) }
            ?: throw AuthenticationFailedException("Login required")
        val oldAccessToken: String = authState.accessToken
        var token = oldAccessToken
        if (requestFreshToken) {
            val job = scope.launch {
                microsoftService.refreshToken(authState.accountId)?.let { result ->
                    token = result.accessToken
                    if(oldAccessToken != result.accessToken){
                        authStateStorage.updateAuthorizationState(authorizationState =  AuthorizationState(
                            value = Gson().toJson(result.convertToMicrosoftAuthResult()),
                            type = OauthMailType.Microsoft,
                        )
                        )
                    }
                }
            }
            runBlocking {
                job.join()
            }
        }
        return token
    }

    override fun invalidateToken() {
        requestFreshToken = true
    }
}
