package com.pricelens

import androidx.compose.foundation.shape.RoundedCornerShape
import com.kyant.backdrop.isRenderEffectSupported
import com.kyant.backdrop.isRuntimeShaderSupported
import com.kyant.shapes.Capsule
import com.pricelens.core.designsystem.components.DockTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class KyantLiquidGlassTest {

    @Test
    fun capsule_isRoundedCornerShapeWithFiftyPercent() {
        val capsule = Capsule()
        assertNotNull(capsule)
        assertEquals(RoundedCornerShape(percent = 50), capsule)
    }

    @Test
    fun dockTabs_hasExpectedCountForLiquidBottomTabs() {
        val count = DockTab.entries.size
        assertEquals(5, count)
        assertEquals(DockTab.EXPLORE, DockTab.entries[0])
        assertEquals(DockTab.SEARCH, DockTab.entries[1])
        assertEquals(DockTab.DEALS, DockTab.entries[2])
        assertEquals(DockTab.WATCHLIST, DockTab.entries[3])
        assertEquals(DockTab.TOOLS, DockTab.entries[4])
    }

    @Test
    fun compatibilityChecks_executeWithoutException() {
        // Runs on JVM test runner; verifies method can be evaluated safely
        val renderEffectSupported = isRenderEffectSupported()
        val runtimeShaderSupported = isRuntimeShaderSupported()
        assertNotNull(renderEffectSupported)
        assertNotNull(runtimeShaderSupported)
    }
}
