package app.k9mail.feature.account.setup.ui.autodiscovery.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.k9mail.core.ui.compose.common.annotation.PreviewDevices
import app.k9mail.core.ui.compose.designsystem.PreviewWithTheme
import app.k9mail.core.ui.compose.designsystem.atom.DividerHorizontal
import app.k9mail.core.ui.compose.designsystem.atom.text.TextTitleLarge
import app.k9mail.core.ui.compose.theme2.MainTheme
import app.k9mail.feature.account.setup.R
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.MailState


@Composable
internal fun ListMailLoginView(
    listMail: List<MailState>,
    onItemClick: (MailState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .background(MainTheme.colors.surfaceBright),
    ) {
        listMail.forEach {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .height(80.dp)
                    .clickable {
                        onItemClick.invoke(it)
                    },
            ) {
                Spacer(modifier = Modifier.weight(1f))
                if (it == MailState.OTHER) {
                    TextTitleLarge(
                        text = stringResource(id = R.string.account_setup_other_server),
                        modifier = Modifier
                            .wrapContentSize()
                            .weight(1f),
                        style = MainTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        color = MainTheme.colors.primary,
                    )
                } else {
                    Image(painter = painterResource(id = it.drawableResID), null, modifier = Modifier.weight(2f))
                }
                Spacer(modifier = Modifier.weight(1f))
                DividerHorizontal(modifier = Modifier.padding(MainTheme.spacings.double, 0.dp))
            }

        }

    }
}

@Composable
@Preview(showBackground = true)
@PreviewDevices
internal fun ColorContentPreview() {
    PreviewWithTheme {
        ListMailLoginView(
            listMail = listOf(MailState.GMAIL, MailState.OUTLOOK, MailState.YANDEX, MailState.OTHER), {},
        )
    }
}