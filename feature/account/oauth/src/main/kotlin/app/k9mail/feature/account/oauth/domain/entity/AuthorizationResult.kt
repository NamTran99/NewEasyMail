package app.k9mail.feature.account.oauth.domain.entity

import com.fsck.k9.entity.AuthorizationState

sealed interface AuthorizationResult {

    data class Success(
        val state: AuthorizationState,
    ) : AuthorizationResult

    data class Failure(
        val error: Exception,
    ) : AuthorizationResult

    object BrowserNotAvailable : AuthorizationResult

    object Canceled : AuthorizationResult
}
