package com.pricelens

import com.pricelens.core.designsystem.components.DockGestureHelper
import com.pricelens.core.designsystem.components.DockTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DockGestureHelperTest {

    private val tabWidthPx = 100f
    private val tabCount = DockTab.entries.size // 5 tabs: EXPLORE (0), SEARCH (1), DEALS (2), WATCHLIST (3), TOOLS (4)

    @Test
    fun `test no drag returns base index`() {
        val result = DockGestureHelper.computeSlideIndices(
            baseIndex = 0,
            dragDeltaX = 0f,
            tabWidthPx = tabWidthPx,
            tabCount = tabCount
        )
        assertEquals(0f, result.displayIndex, 0.001f)
        assertEquals(0, result.hoveredIndex)
        assertEquals(0f, result.rawIndex, 0.001f)
    }

    @Test
    fun `test drag right from explore to search`() {
        // Dragging right by 100px from tab 0 should reach tab 1 (Search)
        val result = DockGestureHelper.computeSlideIndices(
            baseIndex = 0,
            dragDeltaX = 100f,
            tabWidthPx = tabWidthPx,
            tabCount = tabCount
        )
        assertEquals(1.0f, result.displayIndex, 0.001f)
        assertEquals(1, result.hoveredIndex)
        assertEquals(DockTab.SEARCH, DockTab.entries[result.hoveredIndex])
    }

    @Test
    fun `test drag right halfway switches hover at midpoint`() {
        // Dragging right by 49px: still explore (0)
        val resultBelow = DockGestureHelper.computeSlideIndices(
            baseIndex = 0,
            dragDeltaX = 49f,
            tabWidthPx = tabWidthPx,
            tabCount = tabCount
        )
        assertEquals(0, resultBelow.hoveredIndex)

        // Dragging right by 51px: rounds up to search (1)
        val resultAbove = DockGestureHelper.computeSlideIndices(
            baseIndex = 0,
            dragDeltaX = 51f,
            tabWidthPx = tabWidthPx,
            tabCount = tabCount
        )
        assertEquals(1, resultAbove.hoveredIndex)
    }

    @Test
    fun `test drag across full dock from explore to tools`() {
        // Dragging right by 400px from tab 0 reaches tab 4 (Tools)
        val result = DockGestureHelper.computeSlideIndices(
            baseIndex = 0,
            dragDeltaX = 400f,
            tabWidthPx = tabWidthPx,
            tabCount = tabCount
        )
        assertEquals(4.0f, result.displayIndex, 0.001f)
        assertEquals(4, result.hoveredIndex)
        assertEquals(DockTab.TOOLS, DockTab.entries[result.hoveredIndex])
    }

    @Test
    fun `test drag left from deals to explore`() {
        // Deals is at index 2. Dragging left by 200px reaches Explore (index 0)
        val result = DockGestureHelper.computeSlideIndices(
            baseIndex = 2,
            dragDeltaX = -200f,
            tabWidthPx = tabWidthPx,
            tabCount = tabCount
        )
        assertEquals(0.0f, result.displayIndex, 0.001f)
        assertEquals(0, result.hoveredIndex)
        assertEquals(DockTab.EXPLORE, DockTab.entries[result.hoveredIndex])
    }

    @Test
    fun `test rubber band resistance past left edge`() {
        // Dragging left beyond 0 produces damped rubber band offset (0.25x factor)
        val result = DockGestureHelper.computeSlideIndices(
            baseIndex = 0,
            dragDeltaX = -100f,
            tabWidthPx = tabWidthPx,
            tabCount = tabCount
        )
        // rawIndex = -1.0f, displayIndex = -1.0 * 0.25 = -0.25f
        assertEquals(-0.25f, result.displayIndex, 0.001f)
        assertEquals(0, result.hoveredIndex)
    }

    @Test
    fun `test rubber band resistance past right edge`() {
        // Tools is at index 4. Dragging right by 100px beyond 4 produces 4.0 + 1.0 * 0.25 = 4.25
        val result = DockGestureHelper.computeSlideIndices(
            baseIndex = 4,
            dragDeltaX = 100f,
            tabWidthPx = tabWidthPx,
            tabCount = tabCount
        )
        assertEquals(4.25f, result.displayIndex, 0.001f)
        assertEquals(4, result.hoveredIndex)
    }

    @Test
    fun `test zero width guard does not crash and preserves clamped base`() {
        val result = DockGestureHelper.computeSlideIndices(
            baseIndex = 2,
            dragDeltaX = 50f,
            tabWidthPx = 0f,
            tabCount = tabCount
        )
        assertEquals(2.0f, result.displayIndex, 0.001f)
        assertEquals(2, result.hoveredIndex)
    }
}
