package com.pricelens.presentation.dashboard

sealed interface DashboardUiEvent {
    data class SearchQueryChanged(val query: String) : DashboardUiEvent
    data class SubmitSearch(val query: String) : DashboardUiEvent
    data object ClearRecentSearches : DashboardUiEvent
    data class RecentSearchSelected(val query: String) : DashboardUiEvent

    data object OpenPasteUrlSheet : DashboardUiEvent
    data object ClosePasteUrlSheet : DashboardUiEvent
    data class PasteUrlInputChanged(val url: String) : DashboardUiEvent
    data object SubmitPastedUrl : DashboardUiEvent
    data class ConfirmedIdentityChanged(val name: String, val brand: String?, val model: String?) : DashboardUiEvent
    data class ConfirmProductAndNavigate(val identity: com.pricelens.domain.model.ConfirmedProductIdentity) : DashboardUiEvent

    data object RetryLoad : DashboardUiEvent
}
