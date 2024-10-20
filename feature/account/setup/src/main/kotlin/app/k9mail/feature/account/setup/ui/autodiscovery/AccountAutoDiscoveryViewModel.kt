package app.k9mail.feature.account.setup.ui.autodiscovery

import androidx.lifecycle.viewModelScope
import app.k9mail.autodiscovery.api.AuthenticationType
import app.k9mail.autodiscovery.api.AutoDiscoveryResult
import app.k9mail.autodiscovery.api.ConnectionSecurity
import app.k9mail.autodiscovery.api.ImapServerSettings
import app.k9mail.autodiscovery.api.IncomingServerSettings
import app.k9mail.autodiscovery.api.SmtpServerSettings
import app.k9mail.core.android.common.data.FireBaseParam
import app.k9mail.core.android.common.data.FireBaseScreenEvent
import app.k9mail.core.android.common.data.FirebaseUtil
import app.k9mail.core.common.domain.usecase.validation.ValidationResult
import app.k9mail.core.common.net.Hostname
import app.k9mail.core.common.net.Port
import app.k9mail.core.ui.compose.common.mvi.BaseViewModel
import app.k9mail.feature.account.common.domain.AccountDomainContract
import app.k9mail.feature.account.common.domain.entity.IncomingProtocolType
import app.k9mail.feature.account.common.domain.input.StringInputField
import app.k9mail.feature.account.oauth.domain.entity.OAuthResult
import app.k9mail.feature.account.oauth.ui.AccountOAuthContract
import app.k9mail.feature.account.setup.domain.DomainContract.UseCase
import app.k9mail.feature.account.setup.domain.entity.AutoDiscoveryAuthenticationType
import app.k9mail.feature.account.setup.domain.oldMail.EasyMailUtil
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.AutoDiscoveryUiResult
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.ConfigStep
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.ConfigStep.GMAIL
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.ConfigStep.LIST_MAIL_SERVER
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.ConfigStep.OTHER
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.ConfigStep.OUTLOOK
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.Effect
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.Error
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.Event
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.State
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.Validator
import com.fsck.k9.K9
import com.fsck.k9.helper.EmailHelper
import com.hungbang.email2018.f.c.OldMailAccountType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class AccountAutoDiscoveryViewModel(
    initialState: State = State(),
    private val validator: Validator,
    private val getAutoDiscovery: UseCase.GetAutoDiscovery,
    private val getManualDiscovery: UseCase.GetManualDiscoveryResult,
    private val accountStateRepository: AccountDomainContract.AccountStateRepository,
    override val oAuthViewModel: AccountOAuthContract.ViewModel,
) : BaseViewModel<State, Event, Effect>(initialState), AccountAutoDiscoveryContract.ViewModel {


    private var oldSignature: String? = null
    private var isReLogin: Boolean = false
    private val savedAccount = EasyMailUtil.getSavedAccountFromEasyMail()

    override fun event(event: Event) {
        when (event) {
            is Event.EmailAddressChanged -> changeEmailAddress(event.emailAddress)
            is Event.PasswordChanged -> changePassword(event.password)
            is Event.OnOAuthResult -> onOAuthResult(event.result)
            is Event.OnSelectServer -> {
                selectServer(event.state)
            }

            Event.OnNextClicked -> onNext()
            Event.OnBackClicked -> onBack()
            Event.OnRetryClicked -> onRetry()
            Event.OnSignInPasswordClicked -> {
                submitEmail()
            }

            Event.OnManualConfigurationClicked -> {
                navigateNext(isAutomaticConfig = false)
            }

            Event.OnScreenShown -> {
//                if (!K9.isCheckReLogin) {
                if(true){
                    viewModelScope.launch(Dispatchers.Main) {
                        checkIfHaveDataOldAccount()
                        K9.isCheckReLogin = true
                        K9.saveSettingsAsync()
                    }
                }
            }

            Event.OnReLoginClicked -> {
                viewModelScope.launch(Dispatchers.Main) {
                    val step = savedAccount?.b?.let { OldMailAccountType.fromInt(it).conVertAccountTypeToConfigStep() }
                    if (step == OTHER) {
                        requestReLoginWithPass()
                    } else {
                        requestToReLoginMicrosoft()
                    }
                }
            }
        }
    }

    private fun requestToReLoginMicrosoft() {
        selectServer(OUTLOOK)
    }

    private fun checkIfHaveDataOldAccount() {
        if (savedAccount != null) {
            emitEffect(Effect.ShowDialogReLogin)
            oldSignature = savedAccount.i
        }
    }

    private fun selectServer(step: ConfigStep) {
        clearData()
        updateState {
            it.copy(
                emailAddress = StringInputField(),
                password = StringInputField(),
                configStep = step,
                autoDiscoverySettings = if (step == OTHER) null else it.autoDiscoverySettings,
                isShowToolbar = step == OTHER,
                isNextButtonVisible = step == OTHER,
            )
        }
        setUpMailServerConfig(step)

        if (step  ==  ConfigStep.OUTLOOK) {
            oAuthViewModel.event(AccountOAuthContract.Event.OnOAuthMicrosoftClick)
        }

        if(step != LIST_MAIL_SERVER){
            FirebaseUtil.logEvent(FireBaseScreenEvent.SIGN_IN + step.name, arrayOf(FireBaseParam.SIGN_IN_TYPE to step.name))
        }
    }

    private fun clearData() {
        accountStateRepository.clear()
        updateState {
            it.copy(
                password = StringInputField(),
                autoDiscoverySettings = null,
                authorizationState = null,
            )
        }
    }


    private fun OldMailAccountType.conVertAccountTypeToConfigStep(): ConfigStep {
        return when (this) {
            OldMailAccountType.GOOGLE -> GMAIL
            OldMailAccountType.OUTLOOK -> ConfigStep.OUTLOOK
            OldMailAccountType.YANDEX -> ConfigStep.YANDEX
        }
    }

    private suspend fun requestReLoginWithPass() {
        isReLogin = true
        accountStateRepository.clear()
        savedAccount?.let {
            updateState {
                it.copy(
                    configStep = OTHER,
                    emailAddress = StringInputField(savedAccount.a),
                    password = StringInputField(savedAccount.h),
                )
            }
            val savedMailSigning =
                EasyMailUtil.getSavedSignInConfigFromEasyMail(EmailHelper.getDomainFromEmailAddress(savedAccount.a))
            if (savedMailSigning != null) {
                FirebaseUtil.logEvent(FireBaseScreenEvent.SAVED_CONFIG_FOUND)
                val result = AutoDiscoveryResult.Settings(
                    incomingServerSettings = ImapServerSettings(
                        hostname = Hostname(savedMailSigning.imap_host),
                        port = Port(savedMailSigning.imap_port.toInt()),
                        connectionSecurity = ConnectionSecurity.TLS,
                        authenticationTypes = listOf(AuthenticationType.PasswordCleartext),
                        username = it.a,
                    ),
                    outgoingServerSettings = SmtpServerSettings(
                        hostname = Hostname(savedMailSigning.smtp_host),
                        port = Port(savedMailSigning.smtp_port.toInt()),
                        connectionSecurity = if (savedMailSigning.isSmtpStartTLS()) ConnectionSecurity.StartTLS else ConnectionSecurity.TLS,
                        authenticationTypes = listOf(AuthenticationType.PasswordCleartext),
                        username = savedAccount.a,
                    ),
                    source = "",
                )
                updateAutoDiscoverySettings(result)
            } else {
                FirebaseUtil.logEvent(FireBaseScreenEvent.SAVED_CONFIG_NULL)
                if (!savedAccount.h.isNullOrBlank()) {
                    submitEmail()
                }
            }
        }

    }


    private fun setUpMailServerConfig(mailState: ConfigStep) {
        val result = getManualDiscovery.execute(mailState, false)
        updateAutoDiscoverySettings(result ?: return)
    }

    private fun changeEmailAddress(emailAddress: String) {
        clearData()
        updateState {
            it.copy(
                emailAddress = StringInputField(value = emailAddress),
            )
        }
    }

    private fun changePassword(password: String) {
        updateState {
            it.copy(
                password = it.password.updateValue(password),
            )
        }
    }

    private fun onNext() {
        when (state.value.configStep) {
            ConfigStep.LIST_MAIL_SERVER -> Unit
            ConfigStep.OTHER -> {
                submitEmail()
            }

            else -> Unit
        }
    }

    private fun onRetry()= viewModelScope.launch {
        updateState {
            it.copy(error = null)
        }
        loadAutoDiscovery()
    }

    private fun submitEmail()= viewModelScope.launch {
        with(state.value) {
            val emailValidationResult = validator.validateEmailAddress(emailAddress.value)
            val passwordValidationResult = validator.validatePassword(password.value)
            val hasError = listOf(
                emailValidationResult,
                passwordValidationResult,
            ).any { it is ValidationResult.Failure }

            updateState {
                it.copy(
                    emailAddress = it.emailAddress.updateFromValidationResult(emailValidationResult),
                    password = it.password.updateFromValidationResult(passwordValidationResult),
                )
            }


            if (!hasError) {
                if (state.value.configStep == ConfigStep.OTHER) {
                    loadAutoDiscovery()
                } else {
                    navigateNext(state.value.autoDiscoverySettings != null)
                }
            }
        }
    }

    private suspend fun loadAutoDiscovery() {
        updateState {
            it.copy(
                isLoading = true,
            )
        }

        when (val result = getAutoDiscovery.execute(state.value.emailAddress.value)) {
            AutoDiscoveryResult.NoUsableSettingsFound -> updateError(Error.NoUsableSettingsError)
            is AutoDiscoveryResult.Settings -> updateAutoDiscoverySettings(result)
            is AutoDiscoveryResult.NetworkError -> updateError(Error.NetworkError)
            is AutoDiscoveryResult.UnexpectedException -> updateError(Error.UnknownError)
        }
    }

    private fun updateAutoDiscoverySettings(settings: AutoDiscoveryResult.Settings) {
        val imapServerSettings = settings.incomingServerSettings as ImapServerSettings
        val isOAuth = imapServerSettings.authenticationTypes.first() == AutoDiscoveryAuthenticationType.OAuth2

        if (isOAuth) {
            oAuthViewModel.initState(
                AccountOAuthContract.State(
                    hostname = imapServerSettings.hostname.value,
                    emailAddress = state.value.emailAddress.value,
                ),
            )
        }

        updateState {
            it.copy(
                isLoading = false,
                autoDiscoverySettings = settings,
                isNextButtonVisible = !isOAuth,
            )
        }

        if (state.value.configStep == ConfigStep.OTHER) {
            navigateNext(state.value.autoDiscoverySettings != null)
        }
    }

    private fun updateError(error: Error) {
        val dataError = mutableListOf(
            FireBaseParam.SIGN_IN_ERROR to error.javaClass.simpleName,
            FireBaseParam.SIGN_IN_RE_LOGIN to isReLogin.toString(),
        )
        if (error is Error.NoUsableSettingsError) {
            with(state.value) {
                dataError.add(FireBaseParam.SIGN_IN_EMAIL to emailAddress.toString())
            }
        }

        FirebaseUtil.logEvent(
            FireBaseScreenEvent.FIND_SERVER_ERROR,
            dataError.toTypedArray(),
        )

        updateState {
            it.copy(
                isLoading = false,
                error = error,
            )
        }
    }

    private fun submitPassword() {
        with(state.value) {
            val emailValidationResult = validator.validateEmailAddress(emailAddress.value)
            val passwordValidationResult = validator.validatePassword(password.value)
//            val configurationApprovalValidationResult = validator.validateConfigurationApproval(
//                isApproved = configurationApproved.value,
//                isAutoDiscoveryTrusted = autoDiscoverySettings?.isTrusted,
//            )
            val hasError = listOf(
                emailValidationResult,
                passwordValidationResult,
//                configurationApprovalValidationResult,
            ).any { it is ValidationResult.Failure }

            updateState {
                it.copy(
                    emailAddress = it.emailAddress.updateFromValidationResult(emailValidationResult),
                    password = it.password.updateFromValidationResult(passwordValidationResult),
                )
            }

            if (!hasError) {
                navigateNext(state.value.autoDiscoverySettings != null)
            }
        }
    }

    private fun onBack() {
        with(state.value) {
            if (error != null) {
                // back
                updateState {
                    it.copy(error = null, configStep = ConfigStep.OTHER)
                }
                return
            }
        }
        when (state.value.configStep) {
            LIST_MAIL_SERVER -> {
                if (state.value.error != null) {
                    updateState {
                        it.copy(error = null)
                    }
                } else {
                    navigateBack()
                }
            }

            ConfigStep.OAUTH,
            -> {
                updateState {
                    it.copy(
                        configStep = LIST_MAIL_SERVER,
                    )
                }
            }

            else -> {
                updateState {
                    it.copy(
                        configStep = LIST_MAIL_SERVER,
                        isShowToolbar = false,
                    )
                }
            }

        }
    }

    private fun onOAuthResult(result: OAuthResult) {
        if (result is OAuthResult.Success) {
            updateState {
                it.copy(authorizationState = result.authorizationState)
            }
            navigateNext(isAutomaticConfig = true)
        } else {
            updateState {
                it.copy(authorizationState = null)
            }
        }
    }

    private fun navigateBack() = emitEffect(Effect.NavigateBack)

    private fun navigateNext(isAutomaticConfig: Boolean) {
        val addressOauth = accountStateRepository.getState().emailAddress ?: state.value.emailAddress.value
        updateState {
            it.copy(
                autoDiscoverySettings = it.autoDiscoverySettings?.changeAddress(addressOauth),
                emailAddress = StringInputField(addressOauth),
            )
        }
        accountStateRepository.setState(state.value.toAccountState(addressOauth, oldSignature))

        emitEffect(
            Effect.NavigateNext(
                result = mapToAutoDiscoveryResult(
                    isAutomaticConfig = isAutomaticConfig,
                    incomingServerSettings = state.value.autoDiscoverySettings?.incomingServerSettings,
                ),
            ),
        )
    }

    private fun mapToAutoDiscoveryResult(
        isAutomaticConfig: Boolean,
        incomingServerSettings: IncomingServerSettings?,
    ): AutoDiscoveryUiResult {
        val incomingProtocolType = if (incomingServerSettings is ImapServerSettings) {
            IncomingProtocolType.IMAP
        } else {
            null
        }

        return AutoDiscoveryUiResult(
            isAutomaticConfig = isAutomaticConfig,
            incomingProtocolType = incomingProtocolType,
        )
    }
}
