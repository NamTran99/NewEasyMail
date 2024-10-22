package app.k9mail.feature.account.oauth.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.viewModelScope
import app.k9mail.core.android.common.data.FireBaseParam.SIGN_IN_ERROR
import app.k9mail.core.android.common.data.FireBaseParam.SIGN_IN_ERROR_CONTENT
import app.k9mail.core.android.common.data.FireBaseScreenEvent.SIGN_IN_OUTLOOK
import app.k9mail.core.android.common.data.FirebaseUtil
import app.k9mail.core.ui.compose.common.mvi.BaseViewModel
import com.fsck.k9.entity.AuthorizationState
import app.k9mail.feature.account.oauth.domain.AccountOAuthDomainContract.UseCase
import app.k9mail.feature.account.oauth.domain.OauthAccountType
import app.k9mail.feature.account.oauth.domain.entity.AuthorizationIntentResult
import app.k9mail.feature.account.oauth.domain.entity.AuthorizationResult
import app.k9mail.feature.account.oauth.ui.AccountOAuthContract.Effect
import app.k9mail.feature.account.oauth.ui.AccountOAuthContract.Error
import app.k9mail.feature.account.oauth.ui.AccountOAuthContract.Event
import app.k9mail.feature.account.oauth.ui.AccountOAuthContract.State
import app.k9mail.feature.account.oauth.ui.AccountOAuthContract.ViewModel
import kotlinx.coroutines.launch

class AccountOAuthViewModel(
    initialState: State = State(),
    private val getOAuthRequestIntent: UseCase.GetOAuthRequestIntent,
    private val finishOAuthSignIn: UseCase.FinishOAuthSignIn,
    private val checkIsGoogleSignIn: UseCase.CheckAccountType,
) : BaseViewModel<State, Event, Effect>(initialState), ViewModel {

    private var oauthType :  OauthAccountType? = null

    override fun initState(state: State) {
        oauthType = checkIsGoogleSignIn.execute(state.hostname)
        updateState {
            state.copy(
                oauthAccountType = oauthType,
            )
        }
    }

    override fun event(event: Event) {
        when (event) {
            is Event.OnOAuthResult -> onOAuthResult(event.resultCode, event.data)

            is Event.OnOAuthMicrosoftResult -> {
                handleSignInResult(event.result)
            }

            Event.SignInClicked -> onSignIn()

            Event.OnOAuthMicrosoftClick -> emitEffect(Effect.LaunchOAuthMicrosoft)

            Event.OnBackClicked -> navigateBack()

            Event.OnRetryClicked -> onRetry()
        }
    }

    private fun handleSignInResult(result: AuthorizationResult) {
        when (result) {
            AuthorizationResult.BrowserNotAvailable -> updateErrorState(Error.BrowserNotAvailable)
            AuthorizationResult.Canceled -> updateErrorState(Error.Canceled)
            is AuthorizationResult.Failure -> updateErrorState(Error.Unknown(result.error))
            is AuthorizationResult.Success -> {
                updateState { state ->
                    state.copy(isLoading = false)
                }
                navigateNext(authorizationState = result.state)
            }
        }
    }


    private fun onSignIn() {
        if(oauthType == OauthAccountType.MICROSOFT){
            emitEffect(Effect.LaunchOAuthMicrosoft)
        }else{
            val result = getOAuthRequestIntent.execute(
                hostname = state.value.hostname,
                emailAddress = state.value.emailAddress,
            )

            when (result) {
                AuthorizationIntentResult.NotSupported -> {
                    updateState { state ->
                        state.copy(
                            error = Error.NotSupported,
                        )
                    }
                }

                is AuthorizationIntentResult.Success -> {
                    emitEffect(Effect.LaunchOAuth(result.intent))
                }
            }
        }
    }

    private fun onRetry() {
        updateState { state ->
            state.copy(
                error = null,
            )
        }
        onSignIn()
    }

    private fun onOAuthResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            finishSignIn(data)
        } else {
            updateState { state ->
                state.copy(error = Error.Canceled)
            }
        }
    }

    private fun finishSignIn(data: Intent) {
        updateState { state ->
            state.copy(
                isLoading = true,
            )
        }
        viewModelScope.launch {
            handleSignInResult(finishOAuthSignIn.execute(data))
        }
    }

    private fun updateErrorState(error: Error) {
        val dataError = mutableListOf(SIGN_IN_ERROR to error.javaClass.simpleName)
        if(error is Error.Unknown){
            dataError.add(SIGN_IN_ERROR_CONTENT to (error.error.localizedMessage?: ""))
        }

        FirebaseUtil.logEvent(SIGN_IN_OUTLOOK, dataError.toTypedArray())
        emitEffect(Effect.ShowError(error))
    }

    private fun navigateBack() = emitEffect(Effect.NavigateBack)

    private fun navigateNext(authorizationState: AuthorizationState) {
        emitEffect(Effect.NavigateNext(authorizationState))
    }
}
