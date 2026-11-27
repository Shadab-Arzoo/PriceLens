package com.pricelens

import com.pricelens.core.designsystem.components.LIQUID_GLASS_AGSL
import com.pricelens.core.designsystem.components.LiquidGlassShaderHelper
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LiquidGlassViewTest {

    @Test
    fun liquidGlassShaderHelper_isInstantiable() {
        assertNotNull(LiquidGlassShaderHelper)
    }

    @Test
    fun liquidGlassAgsl_containsPhysicalOpticsPrimitives() {
        assertTrue(LIQUID_GLASS_AGSL.contains("gradSdRoundedBox"))
        assertTrue(LIQUID_GLASS_AGSL.contains("circleMap"))
        assertTrue(LIQUID_GLASS_AGSL.contains("fresnelIntensity"))
        assertTrue(LIQUID_GLASS_AGSL.contains("touchPoint"))
    }
}
