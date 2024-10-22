package app.k9mail.feature.account.common.domain.entity

import com.fsck.k9.entity.AuthorizationState
import com.fsck.k9.mail.ServerSettings

data class Account(
    val uuid: String,
    val emailAddress: String,
    val incomingServerSettings: ServerSettings,
    val outgoingServerSettings: ServerSettings,
    val authorizationState: AuthorizationState?,
    val specialFolderSettings: SpecialFolderSettings?,
    val options: AccountOptions,
)
