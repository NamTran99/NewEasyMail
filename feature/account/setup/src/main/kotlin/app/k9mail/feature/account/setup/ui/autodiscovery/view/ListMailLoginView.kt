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
import androidx.compose.ui.platform.LocalContext
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
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.ConfigStep
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@Composable
internal fun ListMailLoginView(
    listMail: List<ConfigStep>,
    onItemClick: (ConfigStep) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    try {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES,
        )
        for (signature in info.signingInfo.apkContentsSigners) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            Log.d(
                "NamTD8 KeyHash",
                "KeyHash:" + Base64.encodeToString(
                    md.digest(), 0,
                ),
            )
        }
    } catch (e: PackageManager.NameNotFoundException) {
    } catch (e: NoSuchAlgorithmException) {
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
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
                if (it == ConfigStep.OTHER) {
                    TextTitleLarge(
                        text = stringResource(id = R.string.account_setup_other_server),
                        modifier = Modifier
                            .wrapContentSize()
                            .weight(1f),
                        style = MainTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        color = MainTheme.colors.primary,
                    )
                } else {
                    it.getDrawable()?.let{ drawableID ->
                        Image(painter = painterResource(id = drawableID), null, modifier = Modifier.weight(2f))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                DividerHorizontal(modifier = Modifier.padding(MainTheme.spacings.double, 0.dp), color = MainTheme.colors.primary)
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
            listMail = listOf(ConfigStep.GMAIL, ConfigStep.OUTLOOK, ConfigStep.YANDEX, ConfigStep.OTHER), {},
        )
    }
}
