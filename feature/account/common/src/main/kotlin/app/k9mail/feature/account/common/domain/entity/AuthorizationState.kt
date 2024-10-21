package app.k9mail.feature.account.common.domain.entity

import com.google.gson.Gson

data class AuthorizationState(
    val value: String? = null,
    val type: OauthMailType = OauthMailType.Other,
){
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

enum class OauthMailType{ Microsoft, Other}
