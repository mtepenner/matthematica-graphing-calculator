package com.matthematica.domain.calculator

import com.matthematica.domain.chemistry.ChemistryService
import com.matthematica.domain.collaboration.CollaborationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase3ExtendedFeaturesTest {

    @Test
    fun generateSurfacePoints_returns3dPoints() {
        val service = GraphingService()
        val result = service.generateSurfacePoints("sin(x)+cos(y)", -1.0, 1.0, -1.0, 1.0, gridSize = 8)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isNotEmpty())
    }

    @Test
    fun chemistryStructure_returnsKnownMolecule() {
        val service = ChemistryService()
        val result = service.getMolecularStructure("H2O")

        assertTrue(result.isSuccess)
        val structure = result.getOrThrow()
        assertEquals("Water", structure.name)
        assertEquals(3, structure.atoms.size)
    }

    @Test
    fun customFunction_defineAndEvaluate_works() {
        val service = CustomFunctionService()
        val defineResult = service.defineFunction("f1", "x^2 + 1")
        val evalResult = service.evaluateFunction("f1", 3.0)

        assertTrue(defineResult.isSuccess)
        assertTrue(evalResult.isSuccess)
        assertEquals(10.0, evalResult.getOrThrow(), 1e-9)
    }

    @Test
    fun formulaSearch_returnsMatches() {
        val service = FormulaLibraryService()
        val result = service.search("integral")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isNotEmpty())
    }

    @Test
    fun collaboration_createJoinAndUpdate_works() {
        val service = CollaborationService()
        assertTrue(service.createSession("s1", "alice").isSuccess)
        assertTrue(service.joinSession("bob").isSuccess)
        service.updateSharedExpression("x^2 + 2x + 1")

        val state = service.sessionState.value
        assertEquals("s1", state.sessionId)
        assertTrue(state.participants.contains("alice"))
        assertTrue(state.participants.contains("bob"))
        assertEquals("x^2 + 2x + 1", state.sharedExpression)
    }
}

