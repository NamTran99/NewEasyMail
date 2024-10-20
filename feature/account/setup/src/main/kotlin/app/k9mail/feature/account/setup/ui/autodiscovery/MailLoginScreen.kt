package app.k9mail.feature.account.setup.ui.autodiscovery

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import app.k9mail.core.android.common.data.FireBaseScreenEvent
import app.k9mail.core.android.common.data.FirebaseUtil
import app.k9mail.core.common.provider.AppNameProvider
import app.k9mail.core.ui.compose.common.mvi.observe
import app.k9mail.feature.account.setup.R
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.AutoDiscoveryUiResult
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.Effect
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.Event
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.ViewModel
import app.swiftmail.core.ui.compose.designsystem.dialog.DialogData
import app.swiftmail.core.ui.compose.designsystem.dialog.InformationLottieDialog
import kotlinx.coroutines.delay

@Composable
internal fun MailLoginScreen(
    onNext: (AutoDiscoveryUiResult) -> Unit,
    onBack: () -> Unit,
    viewModel: ViewModel,
    appNameProvider: AppNameProvider,
    modifier: Modifier = Modifier,
) {

    var isShowDialogReLogin by remember {
        mutableStateOf(false)
    }

    val (state, dispatch) = viewModel.observe { effect ->
        when (effect) {
            Effect.NavigateBack -> onBack()
            is Effect.NavigateNext -> onNext(effect.result)
            Effect.ShowDialogReLogin -> isShowDialogReLogin = true
        }
    }

    if (isShowDialogReLogin) {
        LaunchedEffect(Unit) {
            FirebaseUtil.logEvent(FireBaseScreenEvent.SHOW_DIALOG_REQUIRE_RE_LOGIN)
        }
        InformationLottieDialog(
            dialogData = DialogData(
                titleID = R.string.account_setup_relogin_required_title,
                contentID = R.string.account_setup_relogin_required_content,
                onPositiveClick = {
                    FirebaseUtil.logEvent(FireBaseScreenEvent.CLICK_RE_LOGIN)
                    isShowDialogReLogin = false
                    dispatch(Event.OnReLoginClicked)
                },
                onDismissRequest = {
                    FirebaseUtil.logEvent(FireBaseScreenEvent.CLICK_CANCEL_RE_LOGIN)
                    isShowDialogReLogin = false
                },
            ),
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }


    BackHandler {
        dispatch(Event.OnBackClicked)
    }

    LaunchedEffect(Unit) {
        delay(300)
        dispatch(Event.OnScreenShown)
    }

    AccountAutoDiscoveryContent(
        state = state.value,
        onEvent = { dispatch(it) },
        oAuthViewModel = viewModel.oAuthViewModel,
        appName = appNameProvider.appName,
        modifier = modifier,
    )
}
