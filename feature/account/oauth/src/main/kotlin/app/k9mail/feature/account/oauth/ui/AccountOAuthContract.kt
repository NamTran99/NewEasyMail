package app.k9mail.feature.account.oauth.ui

import android.content.Intent
import app.k9mail.core.ui.compose.common.mvi.UnidirectionalViewModel
import com.fsck.k9.entity.AuthorizationState
import app.k9mail.feature.account.common.ui.WizardNavigationBarState
import app.k9mail.feature.account.oauth.domain.OauthAccountType
import app.k9mail.feature.account.oauth.domain.entity.AuthorizationResult

interface AccountOAuthContract {

    interface ViewModel : UnidirectionalViewModel<State, Event, Effect> {
        fun initState(state: State)

    }

    data class State(
        val hostname: String = "",
        val emailAddress: String = "",
        val wizardNavigationBarState: WizardNavigationBarState = WizardNavigationBarState(
            isNextEnabled = false,
        ),
        val oauthAccountType: OauthAccountType? = null,
        val error: Error? = null,
        val isLoading: Boolean = false,
    )

    sealed interface Event {
        data class OnOAuthResult(
            val resultCode: Int,
            val data: Intent?,
        ) : Event
        data class OnOAuthMicrosoftResult(
            val result: AuthorizationResult,
        ) : Event
        data object OnOAuthMicrosoftClick :  Event
        data object SignInClicked : Event
        data object OnBackClicked : Event
        data object OnRetryClicked : Event
    }

    sealed interface Effect {
        data object LaunchOAuthMicrosoft: Effect

        data class LaunchOAuth(
            val intent: Intent,
        ) : Effect

        data class NavigateNext(
            val state: AuthorizationState,
        ) : Effect
        data object NavigateBack : Effect

        data class ShowError(
            val message: Error,
        ) : Effect
    }

    sealed interface Error {
        data object NotSupported : Error
        data object Canceled : Error

        data object BrowserNotAvailable : Error
        data class Unknown(val error: Exception) : Error
    }
}
