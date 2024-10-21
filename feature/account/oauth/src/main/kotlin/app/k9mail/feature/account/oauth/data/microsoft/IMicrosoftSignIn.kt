package app.k9mail.feature.account.oauth.data.microsoft

import android.content.Context
import androidx.activity.ComponentActivity
import app.k9mail.feature.account.oauth.domain.entity.AuthorizationResult
import com.microsoft.identity.client.IAuthenticationResult

interface IMicrosoftSignIn {
    fun init(context: Context)
   suspend fun requestLogin(context: ComponentActivity): AuthorizationResult
   suspend fun refreshToken(accountId: String): IAuthenticationResult?
}
