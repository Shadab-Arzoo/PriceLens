package com.pricelens.domain.account

enum class SyncEntityType {
    WATCHLIST,
    SAVED_SEARCH,
    PREFERENCES,
    ALERT,
    NOTIFICATION
}

data class SyncRecord(
    val id: String,
    val type: SyncEntityType,
    val payload: String,
    val updatedAtEpochMillis: Long,
    val revision: Long
)

sealed interface SyncResult {
    data class Synchronized(val records: List<SyncRecord>) : SyncResult
    data class Offline(val pendingRecords: List<SyncRecord>) : SyncResult
    data class Conflict(
        val local: SyncRecord,
        val remote: SyncRecord
    ) : SyncResult
    data object SignedOut : SyncResult
}

interface AccountSyncGateway {
    suspend fun synchronize(
        session: AccountSession,
        records: List<SyncRecord>
    ): SyncResult
}
