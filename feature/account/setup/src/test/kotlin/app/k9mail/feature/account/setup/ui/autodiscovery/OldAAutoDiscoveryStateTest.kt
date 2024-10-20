package app.k9mail.feature.account.setup.ui.autodiscovery

import app.k9mail.feature.account.common.domain.input.BooleanInputField
import app.k9mail.feature.account.common.domain.input.StringInputField
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.ConfigStep
import app.k9mail.feature.account.setup.ui.autodiscovery.AccountAutoDiscoveryContract.State
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class OldAAutoDiscoveryStateTest {

    @Test
    fun `should set default values`() {
        val state = State()

        assertThat(state).isEqualTo(
            State(
                configStep = ConfigStep.LIST_MAIL_SERVER,
                emailAddress = StringInputField(),
                password = StringInputField(),
                autoDiscoverySettings = null,
                configurationApproved = BooleanInputField(),
                error = null,
                isLoading = false,
            ),
        )
    }
}
