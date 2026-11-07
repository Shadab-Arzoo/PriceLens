package com.pricelens.domain.account

data class AccountSession(
    val userId: String,
    val accessToken: String,
    val expiresAtEpochSeconds: Long
) {
    fun isUsable(nowEpochSeconds: Long): Boolean =
        userId.isNotBlank() &&
            accessToken.isNotBlank() &&
            expiresAtEpochSeconds > nowEpochSeconds
}
