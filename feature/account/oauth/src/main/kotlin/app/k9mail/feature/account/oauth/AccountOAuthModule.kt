package app.k9mail.feature.account.oauth

import app.k9mail.core.common.coreCommonModule
import app.k9mail.feature.account.oauth.data.AuthorizationRepository
import app.k9mail.feature.account.oauth.data.AuthorizationStateRepository
import app.k9mail.feature.account.oauth.data.microsoft.IMicrosoftSignIn
import app.k9mail.feature.account.oauth.data.microsoft.MicrosoftSignInService
import app.k9mail.feature.account.oauth.domain.AccountOAuthDomainContract
import app.k9mail.feature.account.oauth.domain.AccountOAuthDomainContract.UseCase
import app.k9mail.feature.account.oauth.domain.usecase.CheckAccountType
import app.k9mail.feature.account.oauth.domain.usecase.FinishOAuthSignIn
import app.k9mail.feature.account.oauth.domain.usecase.GetOAuthRequestIntent
import app.k9mail.feature.account.oauth.ui.AccountOAuthContract
import app.k9mail.feature.account.oauth.ui.AccountOAuthViewModel
import net.openid.appauth.AuthorizationService
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

val featureAccountOAuthModule: Module = module {
    includes(coreCommonModule)

    single<IMicrosoftSignIn> {
        MicrosoftSignInService(configID = R.raw.account_oauth_microsoft_oauth_config, accountStateRepository = get())
    }

    factory {
        AuthorizationService(
            androidApplication(),
        )
    }

    factory<AccountOAuthDomainContract.AuthorizationRepository> {
        AuthorizationRepository(
            service = get(),
            accountStateRepository = get()
        )
    }

    factory<AccountOAuthDomainContract.AuthorizationStateRepository> {
        AuthorizationStateRepository()
    }

    factory<UseCase.GetOAuthRequestIntent> {
        GetOAuthRequestIntent(
            repository = get(),
            configurationProvider = get(),
        )
    }

    factory<UseCase.FinishOAuthSignIn> { FinishOAuthSignIn(repository = get()) }

    factory<UseCase.CheckAccountType> { CheckAccountType() }

    factory<AccountOAuthContract.ViewModel> {
        AccountOAuthViewModel(
            getOAuthRequestIntent = get(),
            finishOAuthSignIn = get(),
            checkIsGoogleSignIn = get(),
        )
    }
}
