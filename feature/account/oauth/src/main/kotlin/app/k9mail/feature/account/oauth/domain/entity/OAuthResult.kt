package app.k9mail.feature.account.oauth.domain.entity

import com.fsck.k9.entity.AuthorizationState

sealed interface OAuthResult {
    data class Success(
        val authorizationState: AuthorizationState,
    ) : OAuthResult

    object Failure : OAuthResult
}
