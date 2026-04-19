package com.matthematica.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matthematica.data.database.dao.CalculationHistoryDao
import com.matthematica.data.model.CalculationHistory
import com.matthematica.domain.calculator.CalculatorService
import com.matthematica.domain.calculator.GraphPoint
import com.matthematica.domain.calculator.GraphingService
import com.matthematica.domain.chemistry.ChemistryService
import com.matthematica.domain.llm.LLMService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val calculatorService: CalculatorService,
    private val graphingService: GraphingService,
    private val chemistryService: ChemistryService,
    private val llmService: LLMService,
    private val historyDao: CalculationHistoryDao
) : ViewModel() {

    private val _displayValue = MutableStateFlow("0")
    val displayValue: StateFlow<String> = _displayValue.asStateFlow()

    private val _calculationResult = MutableStateFlow<String>("")
    val calculationResult: StateFlow<String> = _calculationResult.asStateFlow()

    private val _calculatorState = MutableStateFlow<CalculatorState>(CalculatorState.Idle)
    val calculatorState: StateFlow<CalculatorState> = _calculatorState.asStateFlow()

    private val _calculationMode = MutableStateFlow(CalculationMode.BASIC)
    val calculationMode: StateFlow<CalculationMode> = _calculationMode.asStateFlow()

    private val _graphPoints = MutableStateFlow<List<GraphPoint>>(emptyList())
    val graphPoints: StateFlow<List<GraphPoint>> = _graphPoints.asStateFlow()

    private val _graphXMin = MutableStateFlow("-10")
    val graphXMin: StateFlow<String> = _graphXMin.asStateFlow()

    private val _graphXMax = MutableStateFlow("10")
    val graphXMax: StateFlow<String> = _graphXMax.asStateFlow()

    fun appendValue(value: String) {
        val current = _displayValue.value
        _displayValue.value = if (current == "0" && value != ".") {
            value
        } else {
            current + value
        }
    }

    fun clear() {
        _displayValue.value = "0"
        _calculationResult.value = ""
        _graphPoints.value = emptyList()
        _calculatorState.value = CalculatorState.Idle
    }

    fun setMode(mode: CalculationMode) {
        _calculationMode.value = mode
        clear()
        if (mode != CalculationMode.BASIC && mode != CalculationMode.TRIGONOMETRY) {
            _displayValue.value = ""
        }
    }

    fun setDisplayValue(value: String) {
        _displayValue.value = value
    }

    fun setGraphRange(xMin: String, xMax: String) {
        _graphXMin.value = xMin
        _graphXMax.value = xMax
    }

    fun calculate() {
        viewModelScope.launch {
            _calculatorState.value = CalculatorState.Loading

            val expression = _displayValue.value
            val result = when (_calculationMode.value) {
                CalculationMode.BASIC -> calculatorService.evaluateExpression(expression)
                CalculationMode.CHEMISTRY -> handleChemistryCalculation(expression)
                CalculationMode.TRIGONOMETRY -> handleTrigonometryCalculation(expression)
                CalculationMode.WORDPROBLEM -> handleWordProblem(expression)
                CalculationMode.GRAPHING -> handleGraphingCalculation(expression)
            }

            result.onSuccess { resultValue ->
                _calculationResult.value = resultValue.toString()
                _calculatorState.value = CalculatorState.Success
                saveToHistory(expression, resultValue.toString(), _calculationMode.value.name)
            }.onFailure { error ->
                _calculationResult.value = error.message ?: "Error"
                _calculatorState.value = CalculatorState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun solveQuadratic(a: Double, b: Double, c: Double) {
        viewModelScope.launch {
            _calculatorState.value = CalculatorState.Loading

            val result = calculatorService.solveQuadratic(a, b, c)
            result.onSuccess { (x1, x2) ->
                val resultText = "x₁ = $x1\nx₂ = $x2"
                _calculationResult.value = resultText
                _calculatorState.value = CalculatorState.Success
                saveToHistory("Quadratic: ${a}x² + ${b}x + $c = 0", resultText, "ALGEBRA")
            }.onFailure { error ->
                _calculationResult.value = error.message ?: "Error"
                _calculatorState.value = CalculatorState.Error(error.message ?: "Unknown error")
            }
        }
    }

    private suspend fun handleChemistryCalculation(formula: String): Result<Double> {
        return if (formula.contains("(") || formula.contains(")")) {
            chemistryService.calculateMolarMass(formula)
        } else {
            chemistryService.calculateMolarMass(formula)
        }
    }

    private suspend fun handleTrigonometryCalculation(expression: String): Result<Double> {
        return calculatorService.evaluateExpression(expression)
    }

    private suspend fun handleWordProblem(problem: String): Result<String> {
        return llmService.solveWordProblem(problem).mapCatching { it }
    }

    private fun handleGraphingCalculation(expression: String): Result<String> {
        val xMin = _graphXMin.value.toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid xMin value"))
        val xMax = _graphXMax.value.toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid xMax value"))

        return graphingService.generateGraphPoints(expression, xMin, xMax)
            .mapCatching { points ->
                _graphPoints.value = points
                "Generated ${points.size} points for x in [$xMin, $xMax]"
            }
    }

    private suspend fun saveToHistory(
        expression: String,
        result: String,
        category: String,
        userId: String = "local"
    ) {
        try {
            val history = CalculationHistory(
                userId = userId,
                expression = expression,
                result = result,
                category = category
            )
            historyDao.insertHistory(history)
        } catch (e: Exception) {
            // Log error but don't fail the calculation
        }
    }

    fun deleteHistory(history: CalculationHistory) {
        viewModelScope.launch {
            historyDao.deleteHistory(history)
        }
    }

    fun toggleFavorite(history: CalculationHistory) {
        viewModelScope.launch {
            val updated = history.copy(isFavorite = !history.isFavorite)
            historyDao.updateHistory(updated)
        }
    }
}

enum class CalculationMode {
    BASIC, CHEMISTRY, TRIGONOMETRY, WORDPROBLEM, GRAPHING
}

sealed class CalculatorState {
    data object Idle : CalculatorState()
    data object Loading : CalculatorState()
    data object Success : CalculatorState()
    data class Error(val message: String) : CalculatorState()
}

