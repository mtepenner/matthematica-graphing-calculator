package com.matthematica.domain.calculator

import org.junit.Assert.assertTrue
import org.junit.Test

class GraphingServiceTest {

    private val graphingService = GraphingService()

    @Test
    fun generateGraphPoints_returnsPointsForValidExpression() {
        val result = graphingService.generateGraphPoints("x^2", -2.0, 2.0, sampleCount = 25)

        assertTrue(result.isSuccess)
        val points = result.getOrThrow()
        assertTrue(points.size >= 20)
        assertTrue(points.any { kotlin.math.abs(it.x) < 0.1f && kotlin.math.abs(it.y) < 0.2f })
    }

    @Test
    fun generateGraphPoints_failsForInvalidRange() {
        val result = graphingService.generateGraphPoints("x^2", 5.0, -5.0)

        assertTrue(result.isFailure)
    }
}

