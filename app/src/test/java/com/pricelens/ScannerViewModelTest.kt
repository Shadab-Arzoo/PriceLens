package com.pricelens

import com.pricelens.domain.service.ProductOcrScanner
import com.pricelens.domain.usecase.ProcessImageOcrUseCase
import com.pricelens.presentation.scanner.ScannerUiEvent
import com.pricelens.presentation.scanner.ScannerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var ocrScanner: ProductOcrScanner
    private lateinit var processImageOcrUseCase: ProcessImageOcrUseCase
    private lateinit var viewModel: ScannerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ocrScanner = ProductOcrScanner()
        processImageOcrUseCase = ProcessImageOcrUseCase(ocrScanner)
        viewModel = ScannerViewModel(processImageOcrUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `camera permission denied updates state to show permission denial guidance`() = runTest {
        viewModel.onEvent(ScannerUiEvent.CameraPermissionDenied)
        val state = viewModel.uiState.value

        assertFalse(state.hasCameraPermission)
        assertTrue(state.isPermissionDenied)
    }

    @Test
    fun `camera permission granted clears denial state`() = runTest {
        viewModel.onEvent(ScannerUiEvent.CameraPermissionDenied)
        assertTrue(viewModel.uiState.value.isPermissionDenied)

        viewModel.onEvent(ScannerUiEvent.CameraPermissionGranted)
        val state = viewModel.uiState.value
        assertTrue(state.hasCameraPermission)
        assertFalse(state.isPermissionDenied)
    }

    @Test
    fun `image selected triggers ocr and yields editable product identification suggestion`() = runTest {
        viewModel.onEvent(ScannerUiEvent.ImageSelected("content://media/external/images/sony_wh1000xm5_box.jpg"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessingOcr)
        assertNotNull(state.ocrSuggestion)

        val suggestion = state.ocrSuggestion!!
        assertEquals("prod_sony_wh1000xm5", suggestion.resolvedProductId)
        assertEquals("Sony", suggestion.suggestedBrand)
        assertFalse("Explicit user confirmation required before comparing", state.isConfirmed)
    }

    @Test
    fun `user can edit ocr suggestion and confirm for comparison flow`() = runTest {
        viewModel.onEvent(ScannerUiEvent.ProcessOcrText("Apple iPhone 15 Model A3090"))
        advanceUntilIdle()

        val originalSuggestion = viewModel.uiState.value.ocrSuggestion
        assertNotNull(originalSuggestion)

        // Edit suggestion
        viewModel.onEvent(
            ScannerUiEvent.OcrSuggestionEdited(
                name = "Apple iPhone 15 256GB Blue",
                brand = "Apple",
                model = "A3090"
            )
        )
        val editedState = viewModel.uiState.value
        assertEquals("Apple iPhone 15 256GB Blue", editedState.ocrSuggestion!!.effectiveTitle)
        assertFalse(editedState.isConfirmed)

        // Confirm identity
        viewModel.onEvent(ScannerUiEvent.ConfirmProduct(editedState.ocrSuggestion!!.confirm()))
        val confirmedState = viewModel.uiState.value
        assertTrue("Identity must be marked confirmed", confirmedState.isConfirmed)
        assertEquals("Apple iPhone 15 256GB Blue", confirmedState.ocrSuggestion!!.effectiveTitle)
    }

    @Test
    fun `reset scan clears ocr suggestion and resets confirmation state`() = runTest {
        viewModel.onEvent(ScannerUiEvent.ProcessOcrText("Samsung Galaxy S24 SM-S921BZKDINS"))
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.ocrSuggestion)

        viewModel.onEvent(ScannerUiEvent.ResetScan)
        val state = viewModel.uiState.value
        assertNull(state.ocrSuggestion)
        assertNull(state.selectedImageUri)
        assertFalse(state.isConfirmed)
    }
}
