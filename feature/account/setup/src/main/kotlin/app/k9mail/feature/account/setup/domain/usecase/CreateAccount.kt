package app.k9mail.feature.account.setup.domain.usecase

import app.k9mail.feature.account.common.domain.entity.Account
import app.k9mail.feature.account.common.domain.entity.AccountDisplayOptions
import app.k9mail.feature.account.common.domain.entity.AccountOptions
import app.k9mail.feature.account.common.domain.entity.AccountState
import app.k9mail.feature.account.common.domain.entity.AccountSyncOptions
import app.k9mail.feature.account.setup.AccountSetupExternalContract.AccountCreator
import app.k9mail.feature.account.setup.AccountSetupExternalContract.AccountCreator.AccountCreatorResult
import app.k9mail.feature.account.setup.domain.DomainContract.UseCase
import app.k9mail.feature.account.setup.domain.entity.EmailDisplayCount
import java.util.UUID

class CreateAccount(
    private val accountCreator: AccountCreator,
    private val uuidGenerator: () -> String = { UUID.randomUUID().toString() },
) : UseCase.CreateAccount {
    override suspend fun execute(accountState: AccountState): AccountCreatorResult {
        val defaultSynOptions = AccountSyncOptions(
            showNotification = true,
            messageDisplayCount = EmailDisplayCount.DEFAULT.count
        )
        val account = Account(
            uuid = uuidGenerator(),
            emailAddress = accountState.emailAddress!!,
            incomingServerSettings = accountState.incomingServerSettings!!.copy(),
            outgoingServerSettings = accountState.outgoingServerSettings!!.copy(),
            authorizationState = accountState.authorizationState,
            specialFolderSettings = accountState.specialFolderSettings,
            options = mapOptions(accountState.displayOptions!!,defaultSynOptions),
        )

        return accountCreator.createAccount(account)
    }

    private fun mapOptions(
        displayOptions: AccountDisplayOptions,
        syncOptions: AccountSyncOptions,
    ): AccountOptions {
        return AccountOptions(
            displayName = displayOptions.displayName,
            emailSignature = displayOptions.emailSignature,
            messageDisplayCount = syncOptions.messageDisplayCount,
            showNotification = syncOptions.showNotification,
        )
    }
}
