package com.matthematica.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorServicePhase3Test {

    private val calculatorService = CalculatorService()

    @Test
    fun matrixDeterminant_returnsCorrectValue() {
        val result = calculatorService.matrixDeterminant("1,2;3,4")

        assertTrue(result.isSuccess)
        assertEquals(-2.0, result.getOrThrow(), 1e-9)
    }

    @Test
    fun statisticsSummary_returnsExpectedMeanMedian() {
        val result = calculatorService.calculateStatistics("2,4,4,4,5,5,7,9")

        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(5.0, summary.mean, 1e-9)
        assertEquals(4.5, summary.median, 1e-9)
    }

    @Test
    fun binomialProbability_matchesKnownPmf() {
        val result = calculatorService.binomialProbability(n = 10, k = 3, p = 0.5)

        assertTrue(result.isSuccess)
        assertEquals(0.1171875, result.getOrThrow(), 1e-9)
    }

    @Test
    fun numericalDerivative_estimatesSlope() {
        val result = calculatorService.numericalDerivative("x^2", x = 3.0)

        assertTrue(result.isSuccess)
        assertEquals(6.0, result.getOrThrow(), 1e-3)
    }

    @Test
    fun numericalIntegral_estimatesArea() {
        val result = calculatorService.numericalIntegral("x", a = 0.0, b = 1.0, intervals = 2000)

        assertTrue(result.isSuccess)
        assertEquals(0.5, result.getOrThrow(), 1e-3)
    }
}

