package app.swiftmail.core.ui.compose.designsystem.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.k9mail.core.ui.compose.common.annotation.PreviewDevices
import app.k9mail.core.ui.compose.designsystem.PreviewWithTheme
import app.k9mail.core.ui.compose.designsystem.R
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilled
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonText
import app.k9mail.core.ui.compose.designsystem.atom.text.TextBodyMedium
import app.k9mail.core.ui.compose.designsystem.atom.text.TextHeadlineLarge
import app.k9mail.core.ui.compose.designsystem.atom.text.TextHeadlineMedium
import app.k9mail.core.ui.compose.designsystem.atom.text.TextHeadlineSmall
import app.k9mail.core.ui.compose.theme2.MainTheme

data class DialogData(
    val titleID: Int,
    val contentID: Int,
    val onDismissRequest: () -> Unit,
    val onPositiveClick: () -> Unit,
)

@Composable
fun InformationLottieDialog(
    dialogData: DialogData,
    properties:DialogProperties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
) {
    Dialog(
        onDismissRequest = dialogData.onDismissRequest,
        properties = properties,
    ) {
        Surface(
            Modifier
                .clip(MainTheme.shapes.large)
                .wrapContentHeight(),
            ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(10.dp)) {
                TextHeadlineSmall(
                    modifier = Modifier.padding(horizontal = MainTheme.spacings.double),
                    text = stringResource(id = dialogData.titleID),
                    textAlign = TextAlign.Start,
                )

                Spacer(Modifier.height(MainTheme.spacings.double))

                TextBodyMedium(
                    modifier = Modifier.padding(horizontal = MainTheme.spacings.double),
                    text = stringResource(id = dialogData.contentID),
                    textAlign = TextAlign.Start,
                )

                Spacer(Modifier.height(MainTheme.spacings.double))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    ButtonText(
                        text = stringResource(id = R.string.designsystem_cancel),
                        onClick = { dialogData.onDismissRequest.invoke() },
                    )

                    Spacer(modifier = Modifier.width(MainTheme.spacings.double))

                    ButtonFilled(
                        text = stringResource(id = R.string.designsystem_molecule_button_re_login),
                        onClick = { dialogData.onPositiveClick.invoke() },
                    )
                }

            }
        }
    }
}

@Composable
@Preview(showBackground = true)
@PreviewDevices
private fun ComingSoonDialogPreview() {
    PreviewWithTheme {
        InformationLottieDialog(
            dialogData = DialogData(
                contentID = R.string.designsystem_molecule_button_back,
                titleID = R.string.designsystem_molecule_button_back,
                onDismissRequest = {},onPositiveClick = {},
            ),
        )
    }
}
