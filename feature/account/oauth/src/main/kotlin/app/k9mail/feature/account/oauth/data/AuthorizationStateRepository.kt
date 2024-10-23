package app.k9mail.feature.account.oauth.data

import com.fsck.k9.entity.AuthorizationState
import app.k9mail.feature.account.oauth.domain.AccountOAuthDomainContract
import com.fsck.k9.entity.OauthMailType

class AuthorizationStateRepository : AccountOAuthDomainContract.AuthorizationStateRepository {
    override fun isAuthorized(authorizationState: AuthorizationState): Boolean {
        return  when(authorizationState.type){
            OauthMailType.Microsoft ->{
                authorizationState.value != null
            }
            OauthMailType.Other -> {
                authorizationState.toAuthState().isAuthorized
            }
        }
    }
}
