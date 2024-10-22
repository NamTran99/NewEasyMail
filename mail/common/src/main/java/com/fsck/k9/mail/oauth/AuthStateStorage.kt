package com.fsck.k9.mail.oauth

import com.fsck.k9.entity.AuthorizationState

interface AuthStateStorage {
    fun getAuthorizationState(): AuthorizationState?
    fun updateAuthorizationState(authorizationState: AuthorizationState?)
}
