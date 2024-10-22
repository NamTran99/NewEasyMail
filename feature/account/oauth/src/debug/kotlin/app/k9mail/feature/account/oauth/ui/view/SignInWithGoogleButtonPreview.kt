package app.k9mail.feature.account.oauth.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.k9mail.core.ui.compose.designsystem.PreviewWithThemes
import app.k9mail.feature.account.oauth.domain.OauthAccountType

@Composable
@Preview(showBackground = true)
internal fun SignInWithGoogleButtonPreview() {
    PreviewWithThemes {
        SignInWithOauthServerButton(
            oauthType = OauthAccountType.GOOGLE,
            onClick = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
internal fun SignInWithGoogleButtonDisabledPreview() {
    PreviewWithThemes {
        SignInWithOauthServerButton(
            onClick = {},
            enabled = false,
            oauthType = OauthAccountType.GOOGLE,
        )
    }
}
