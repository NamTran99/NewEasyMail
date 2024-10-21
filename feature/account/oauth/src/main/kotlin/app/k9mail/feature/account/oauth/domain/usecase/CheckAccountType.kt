package app.k9mail.feature.account.oauth.domain.usecase

import app.k9mail.feature.account.oauth.domain.AccountOAuthDomainContract.UseCase
import app.k9mail.feature.account.oauth.domain.OauthAccountType

internal class CheckAccountType : UseCase.CheckAccountType {
    override fun execute(hostname: String): OauthAccountType? {
        for (domain in domainGoogle) {
            if (hostname.lowercase().endsWith(domain)) {
                return OauthAccountType.GOOGLE
            }
        }

        for (domain in domainOutlook) {
            if (hostname.lowercase().endsWith(domain)) {
                return OauthAccountType.MICROSOFT
            }
        }

        return null
    }

    private companion object {
        val domainGoogle = listOf(
            ".gmail.com",
            ".googlemail.com",
            ".google.com",
        )

        val domainOutlook = listOf(
            ".outlook.com",
            ".office365.com",
            ".hotmail.com",
            ".live.com",
            ".msn.com",
        )
    }
}
