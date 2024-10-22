package com.fsck.k9.backends

import com.fsck.k9.Account
import com.fsck.k9.entity.AuthorizationState
import com.fsck.k9.mail.oauth.AuthStateStorage
import com.fsck.k9.preferences.AccountManager

class AccountAuthStateStorage(
    private val accountManager: AccountManager,
    private val account: Account,
) : AuthStateStorage {
    override fun getAuthorizationState(): AuthorizationState? {
        return account.oAuthState
    }

    override fun updateAuthorizationState(authorizationState: AuthorizationState?) {
        account.oAuthState = authorizationState
        accountManager.saveAccount(account)
    }
}
