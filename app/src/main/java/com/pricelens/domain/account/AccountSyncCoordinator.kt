package com.pricelens.domain.account

import javax.inject.Inject

class AccountSyncCoordinator @Inject constructor(
    private val gateway: AccountSyncGateway
) {
    suspend fun synchronize(
        session: AccountSession?,
        records: List<SyncRecord>,
        nowEpochSeconds: Long
    ): SyncResult {
        if (session == null || !session.isUsable(nowEpochSeconds)) {
            return SyncResult.SignedOut
        }
        return gateway.synchronize(session, records)
    }
}
