package app.k9mail.feature.account.oauth.data.microsoft

import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import java.util.Date

data class MicrosoftAuthResult(
    val accessToken: String,
    val expiresOn: Date,
    val authenticationScheme: String,
    val accountId: String,
)

//data class MicrosoftAccount(
//    val mIdToken: String?,
//    val mClaims: Map<String, *>?,
//    val mTenantID: String,
//    val mUserName: String,
//    val mId: String,
//    val mAuthority: String,
//) : IAccount {
//    constructor(account: IAccount) : this(
//        mIdToken = account.idToken,
//        mClaims = account.claims,
//        mTenantID = account.tenantId,
//        mUserName = account.username,
//        mId = account.id,
//        mAuthority = account.authority,
//    )
//
//    override fun getIdToken(): String? = mIdToken
//    override fun getClaims(): Map<String, *>? = mClaims
//    override fun getUsername(): String = mUserName
//    override fun getTenantId(): String = mTenantID
//    override fun getId(): String = mId
//    override fun getAuthority(): String = mAuthority
//}

fun IAuthenticationResult.convertToMicrosoftAuthResult(): MicrosoftAuthResult {
    return MicrosoftAuthResult(
        accessToken = this.accessToken,
        expiresOn = this.expiresOn,
        authenticationScheme = this.authenticationScheme,
        accountId = this.account.id,
    )
}
