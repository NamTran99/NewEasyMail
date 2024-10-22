package com.fsck.k9.entity

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
