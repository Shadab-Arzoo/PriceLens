package com.pricelens

import com.pricelens.domain.account.AccountSession
import com.pricelens.domain.account.AccountSyncCoordinator
import com.pricelens.domain.account.AccountSyncGateway
import com.pricelens.domain.account.SyncRecord
import com.pricelens.domain.account.SyncEntityType
import com.pricelens.domain.account.SyncResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountSyncCoordinatorTest {
    @Test
    fun expiredSessionDoesNotSendPrivateDataToGateway() = runTest {
        var called = false
        val coordinator = AccountSyncCoordinator(object : AccountSyncGateway {
            override suspend fun synchronize(
                session: AccountSession,
                records: List<SyncRecord>
            ): SyncResult {
                called = true
                return SyncResult.Synchronized(records)
            }
        })

        val result = coordinator.synchronize(
            session = AccountSession("user", "token", 10),
            records = listOf(SyncRecord("1", SyncEntityType.WATCHLIST, "{}", 1, 1)),
            nowEpochSeconds = 10
        )

        assertTrue(result is SyncResult.SignedOut)
        assertTrue(!called)
    }
}
