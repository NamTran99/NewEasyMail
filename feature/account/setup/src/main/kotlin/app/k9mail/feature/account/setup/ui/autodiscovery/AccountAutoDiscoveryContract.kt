package app.k9mail.feature.account.setup.ui.autodiscovery

import app.k9mail.autodiscovery.api.AutoDiscoveryResult
import app.k9mail.core.common.domain.usecase.validation.ValidationResult
import app.k9mail.core.ui.compose.common.mvi.UnidirectionalViewModel
import app.k9mail.feature.account.common.domain.entity.AuthorizationState
import app.k9mail.feature.account.common.domain.entity.IncomingProtocolType
import app.k9mail.feature.account.common.domain.input.StringInputField
import app.k9mail.feature.account.common.ui.loadingerror.LoadingErrorState
import app.k9mail.feature.account.oauth.domain.entity.OAuthResult
import app.k9mail.feature.account.oauth.ui.AccountOAuthContract
import app.k9mail.feature.account.setup.R

interface AccountAutoDiscoveryContract {

    enum class ConfigStep {
        LIST_MAIL_SERVER,
        OAUTH,
        YANDEX,
        GMAIL,
        OUTLOOK,
        OTHER;

        fun getDrawable(): Int?{
            return when(this){
                GMAIL -> R.drawable.account_setup_ic_mail
                OUTLOOK -> R.drawable.account_setup_ic_outlook
                YANDEX -> R.drawable.account_setup_ic_yandex
                else -> null
            }
        }
    }

    interface ViewModel : UnidirectionalViewModel<State, Event, Effect> {
        val oAuthViewModel: AccountOAuthContract.ViewModel
    }

    data class State(
        val isReLogin : Boolean = false,
        val configStep: ConfigStep = ConfigStep.LIST_MAIL_SERVER,
        val emailAddress: StringInputField = StringInputField(),
        val password: StringInputField = StringInputField(),
        val autoDiscoverySettings: AutoDiscoveryResult.Settings? = null,
//        val configurationApproved: BooleanInputField = BooleanInputField(),
        val authorizationState: AuthorizationState? = null,
        val instructionContent: Int = R.string.account_setup_select_server,
        val listMailState: List<ConfigStep> = listOf(
            ConfigStep.GMAIL,
            ConfigStep.OUTLOOK,
            ConfigStep.YANDEX,
            ConfigStep.OTHER,
        ),
//        val currentMailState: MailState? = null,

        val isSuccess: Boolean = false,
        override val error: Error? = null,
        override val isLoading: Boolean = false,

        val isNextButtonVisible: Boolean = false,
        val isShowToolbar: Boolean = false,
    ) : LoadingErrorState<Error>

    sealed interface Event {
        data class EmailAddressChanged(val emailAddress: String) : Event
        data class PasswordChanged(val password: String) : Event
        data class OnOAuthResult(val result: OAuthResult) : Event
        data class OnSelectServer(val state: ConfigStep) : Event
        data object OnNextClicked : Event
        data object OnSignInPasswordClicked : Event
        data object OnBackClicked : Event
        data object OnRetryClicked : Event
        data object OnManualConfigurationClicked : Event
        data object OnScreenShown : Event
        data object OnReLoginClicked : Event
    }

    sealed class Effect {
        data class NavigateNext(
            val result: AutoDiscoveryUiResult,
        ) : Effect()

        data object NavigateBack : Effect()
        data object ShowDialogReLogin : Effect()
    }

    interface Validator {
        fun validateEmailAddress(emailAddress: String): ValidationResult
        fun validatePassword(password: String): ValidationResult
        fun validateConfigurationApproval(isApproved: Boolean?, isAutoDiscoveryTrusted: Boolean?): ValidationResult
    }

    sealed interface Error {
        data object NoUsableSettingsError : Error
        data object NetworkError : Error
        data object UnknownError : Error
    }

    data class AutoDiscoveryUiResult(
        val isAutomaticConfig: Boolean,
        val incomingProtocolType: IncomingProtocolType?,
    )
}
