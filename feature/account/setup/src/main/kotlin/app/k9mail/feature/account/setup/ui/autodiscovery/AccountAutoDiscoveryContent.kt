package app.k9mail.feature.account.setup.ui.autodiscovery

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.k9mail.core.ui.compose.common.activity.LocalActivity
import app.k9mail.core.ui.compose.common.mvi.observe
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilled
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonText
import app.k9mail.core.ui.compose.designsystem.atom.text.TextBodyLarge
import app.k9mail.core.ui.compose.designsystem.atom.text.TextBodyMedium
import app.k9mail.core.ui.compose.designsystem.atom.text.TextTitleLarge
import app.k9mail.core.ui.compose.designsystem.molecule.ContentLoadingErrorView
import app.k9mail.core.ui.compose.designsystem.molecule.ErrorView
import app.k9mail.core.ui.compose.designsystem.molecule.LoadingView
import app.k9mail.core.ui.compose.designsystem.molecule.input.EmailAddressInput
import app.k9mail.core.ui.compose.designsystem.molecule.input.PasswordInput
import app.k9mail.core.ui.compose.designsystem.organism.TopAppBarWithBackButton
import app.k9mail.core.ui.compose.designsystem.template.ResponsiveWidthContainer
import app.k9mail.core.ui.compose.theme2.MainTheme
import app.k9mail.feature.account.common.ui.loadingerror.rememberContentLoadingErrorViewState
import app.k9mail.feature.account.oauth.data.microsoft.IMicrosoftSignIn
import app.k9mail.feature.account.oauth.domain.entity.OAuthResult
import app.k9mail.feature.account.oauth.ui.AccountOAuthContract
import app.k9mail.feature.account.oauth.ui.AccountOAuthContract.Effect
import app.k9mail.feature.account.oauth.ui.toResourceString
import app.k9mail.feature.account.setup.R
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.Event
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.State
import app.k9mail.feature.account.setup.ui.autodiscovery.view.ListMailLoginView
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
internal fun AccountAutoDiscoveryContent(
    state: State,
    onEvent: (Event) -> Unit,
    oAuthViewModel: AccountOAuthContract.ViewModel,
    appName: String,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    ResponsiveWidthContainer(
        modifier = Modifier
            .fillMaxSize()
            .testTag("AccountAutoDiscoveryContent")
            .then(modifier),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopAppBarWithBackButton(
                    title = appName,
                    onBackClick = {
                        onEvent(Event.OnBackClicked)
                    },
                )
                AutoDiscoveryContent(
                    state = state,
                    onEvent = onEvent,
                    oAuthViewModel = oAuthViewModel,
                    modifier = Modifier.weight(1f),
                )

            }
        }
    }
}

@Composable
internal fun AutoDiscoveryContent(
    state: State,
    onEvent: (Event) -> Unit,
    oAuthViewModel: AccountOAuthContract.ViewModel,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources
    val context = LocalContext.current

    val oAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        oAuthViewModel.event(AccountOAuthContract.Event.OnOAuthResult(it.resultCode, it.data))
    }
    val scope = rememberCoroutineScope()
    var isShowLoadingOauth by remember {
        mutableStateOf(false)
    }
    val activity = LocalActivity.current
    val microsoftSignIn: IMicrosoftSignIn = koinInject()

    oAuthViewModel.observe { effect ->
        when (effect) {
            is Effect.NavigateNext -> onEvent(Event.OnOAuthResult(OAuthResult.Success(effect.state)))
            is Effect.NavigateBack -> onEvent(Event.OnOAuthResult(OAuthResult.Failure))
            is Effect.LaunchOAuth -> oAuthLauncher.launch(effect.intent)
            Effect.LaunchOAuthMicrosoft -> {
                isShowLoadingOauth = true
                scope.launch {
                    oAuthViewModel.event(
                        AccountOAuthContract.Event.OnOAuthMicrosoftResult(
                            microsoftSignIn.requestLogin(
                                context = activity,
                            ),
                        ),
                    )
                    isShowLoadingOauth = false
                }
            }
            is Effect.ShowError -> {
                Toast.makeText(context, effect.message.toResourceString(context.resources), Toast.LENGTH_SHORT).show()
            }
        }
    }

    ContentLoadingErrorView(
        state = rememberContentLoadingErrorViewState(state),
        loading = {
            LoadingView(
                message = stringResource(id = R.string.account_setup_auto_discovery_loading_message),
                modifier = Modifier.fillMaxSize(),
            )
        },
        error = {
            ErrorView(
                title = stringResource(id = R.string.account_setup_auto_discovery_loading_error),
                message = state.error?.toAutoDiscoveryErrorString(resources),
                onRetry = { onEvent(Event.OnRetryClicked) },
                modifier = Modifier.fillMaxSize(),
            )
        },
        content = {
            ContentView(
                state = state,
                onEvent = onEvent,
                resources = resources,
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
    )
}

@Composable
internal fun ContentView(
    state: State,
    onEvent: (Event) -> Unit,
    resources: Resources,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(vertical = MainTheme.spacings.double)
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        if (state.configStep in listOf(
                AccountAutoDiscoveryContract.ConfigStep.YANDEX,
                AccountAutoDiscoveryContract.ConfigStep.OTHER,
            )
        ) {
            if (state.configStep == AccountAutoDiscoveryContract.ConfigStep.OTHER) {
                TextTitleLarge(
                    text = stringResource(id = R.string.account_setup_other_server),
                    color = MainTheme.colors.primary,
                )
                Spacer(modifier = Modifier.height(MainTheme.spacings.quadruple))

                TextBodyLarge(
                    text = stringResource(id = R.string.account_setup_sign_in_to_your_account),
                    color = MainTheme.colors.primary,
                    configTextStyle = { it.copy(fontWeight = FontWeight.Medium) },
                )

                if (state.autoDiscoverySettings != null) {
                    Spacer(modifier = Modifier.height(MainTheme.spacings.double))
//                    AutoDiscoveryResultView(
//                        settings = state.autoDiscoverySettings,
//                        onEditConfigurationClick = { onEvent(Event.OnEditConfigurationClicked) },
//                    )
                }
                Spacer(modifier = Modifier.height(MainTheme.spacings.double))
            } else {
                AnimatedVisibility(
                    modifier = Modifier.wrapContentHeight(),
                    visible = state.configStep in listOf(
                        AccountAutoDiscoveryContract.ConfigStep.YANDEX,
                    ),
                    exit = ExitTransition.None,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(MainTheme.spacings.double))
                        state.configStep.getDrawable()?.let { drawableID ->
                            Image(
                                painter = painterResource(id = drawableID),
                                null,
                                modifier = Modifier
                                    .height(80.dp)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Fit,
                            )
                        }

                        Spacer(modifier = Modifier.height(MainTheme.spacings.double))
                        TextBodyLarge(
                            text = stringResource(id = R.string.account_setup_sign_in_to_your_account),
                            color = MainTheme.colors.primary,
                        )

                        Spacer(modifier = Modifier.height(MainTheme.spacings.double))
//                if (state.configStep in listOf(
//                        AccountAutoDiscoveryContract.ConfigStep.GMAIL,
//                        AccountAutoDiscoveryContract.ConfigStep.OUTLOOK,
//                    )
//                ) {
//                    AccountOAuthView(
//                        onOAuthResult = { result -> onEvent(Event.OnOAuthResult(result)) },
//                        viewModel = oAuthViewModel,
//                    )
//                }
                    }
                }
            }

            EmailAddressInput(
                emailAddress = state.emailAddress.value,
                errorMessage = state.emailAddress.error?.toAutoDiscoveryValidationErrorString(resources),
                onEmailAddressChange = { onEvent(Event.EmailAddressChanged(it)) },
                contentPadding = PaddingValues(),
            )
            Spacer(modifier = Modifier.height(MainTheme.spacings.default))
            PasswordInput(
                password = state.password.value,
                errorMessage = state.password.error?.toAutoDiscoveryValidationErrorString(resources),
                onPasswordChange = { onEvent(Event.PasswordChanged(it)) },
                contentPadding = PaddingValues(),
            )

            Spacer(modifier = Modifier.height(MainTheme.spacings.triple))
            ButtonFilled(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                text = stringResource(id = R.string.account_setup_login),
                onClick = {
                    onEvent(Event.OnSignInPasswordClicked)
                },
            )

            Spacer(modifier = Modifier.height(MainTheme.spacings.double))

            ButtonText(
                text = stringResource(id = R.string.account_setup_manual_configuration),
                color = MainTheme.colors.primary,
                onClick = {
                    onEvent(Event.OnManualConfigurationClicked)
                },
            )
        } else {
            ListMailLoginView(
                modifier = Modifier.padding(0.dp, MainTheme.spacings.double, 0.dp, 0.dp),
                listMail = state.listMailState,
                onItemClick = {
                    onEvent(Event.OnSelectServer(it))
                },
            )

            Spacer(modifier = Modifier.weight(1f))


            TextBodyMedium(
                text = stringResource(id = R.string.account_setup_application_homepage),
                color = MainTheme.colors.primary,
                textStyle = { textStyle ->
                    textStyle.copy(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                    )
                },
                modifier = Modifier.clickable {
                    context.loadPage("https://hbsolution.site")
                },
            )
            Spacer(modifier = Modifier.height(MainTheme.spacings.default))
            TextBodyMedium(
                text = stringResource(id = R.string.account_setup_privacy_policy),
                color = MainTheme.colors.primary,
                textStyle = { textStyle ->
                    textStyle.copy(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                    )
                },
                modifier = Modifier.clickable {
                    context.loadPage("https://hbsolution.site/policy.html")
                },
            )
        }
    }
}

//@Composable
//@Preview(showBackground = true)
//internal fun AccountAutoDiscoveryContentGmailPreview() {
//    PreviewWithTheme {
//        AccountAutoDiscoveryContent(
//            state = State(
//                configStep = AccountAutoDiscoveryContract.ConfigStep.LIST_MAIL_SERVER,
//                emailAddress = StringInputField(value = "test@example.com"),
//                isShowToolbar = true,
//                autoDiscoverySettings = fakeAutoDiscoveryResultSettings(isTrusted = true),
//            ),
//            onEvent = {},
//            oAuthViewModel = FakeAccountOAuthViewModel(),
//            appName = "AppName",
//        )
//    }
//}


fun Context.loadPage(uriString: String?) {
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
        startActivity(browserIntent)
    } catch (_: ActivityNotFoundException) {
    }
}
