package com.matthematica.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matthematica.data.database.dao.CalculationHistoryDao
import com.matthematica.data.model.CalculationHistory
import com.matthematica.data.sync.HistoryCloudSyncService
import com.matthematica.domain.calculator.CalculatorService
import com.matthematica.domain.calculator.CustomFunctionService
import com.matthematica.domain.calculator.FormulaItem
import com.matthematica.domain.calculator.FormulaLibraryService
import com.matthematica.domain.calculator.Graph3DPoint
import com.matthematica.domain.calculator.GraphPoint
import com.matthematica.domain.calculator.GraphPresetService
import com.matthematica.domain.calculator.GraphingService
import com.matthematica.domain.calculator.NamedFunction
import com.matthematica.domain.calculator.SavedGraphPreset
import com.matthematica.domain.calculator.StatisticsSummary
import com.matthematica.domain.chemistry.MoleculeStructure
import com.matthematica.domain.chemistry.ChemistryService
import com.matthematica.domain.collaboration.CollaborationService
import com.matthematica.domain.collaboration.CollaborationSessionState
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
    private val graphPresetService: GraphPresetService,
    private val formulaLibraryService: FormulaLibraryService,
    private val customFunctionService: CustomFunctionService,
    private val chemistryService: ChemistryService,
    private val collaborationService: CollaborationService,
    private val cloudSyncService: HistoryCloudSyncService,
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

    private val _surfacePoints = MutableStateFlow<List<Graph3DPoint>>(emptyList())
    val surfacePoints: StateFlow<List<Graph3DPoint>> = _surfacePoints.asStateFlow()

    private val _savedGraphPresets = MutableStateFlow<List<SavedGraphPreset>>(emptyList())
    val savedGraphPresets: StateFlow<List<SavedGraphPreset>> = _savedGraphPresets.asStateFlow()

    private val _formulaSearchResults = MutableStateFlow<List<FormulaItem>>(emptyList())
    val formulaSearchResults: StateFlow<List<FormulaItem>> = _formulaSearchResults.asStateFlow()

    private val _customFunctions = MutableStateFlow<List<NamedFunction>>(emptyList())
    val customFunctions: StateFlow<List<NamedFunction>> = _customFunctions.asStateFlow()

    private val _moleculeStructure = MutableStateFlow<MoleculeStructure?>(null)
    val moleculeStructure: StateFlow<MoleculeStructure?> = _moleculeStructure.asStateFlow()

    private val _collaborationState = MutableStateFlow(CollaborationSessionState())
    val collaborationState: StateFlow<CollaborationSessionState> = _collaborationState.asStateFlow()

    private val _phase3Operation = MutableStateFlow(Phase3Operation.MATRIX_DETERMINANT)
    val phase3Operation: StateFlow<Phase3Operation> = _phase3Operation.asStateFlow()

    private val _phase3InputA = MutableStateFlow("")
    val phase3InputA: StateFlow<String> = _phase3InputA.asStateFlow()

    private val _phase3InputB = MutableStateFlow("")
    val phase3InputB: StateFlow<String> = _phase3InputB.asStateFlow()

    private val _phase3InputC = MutableStateFlow("")
    val phase3InputC: StateFlow<String> = _phase3InputC.asStateFlow()

    init {
        refreshSavedPresets()
        refreshCustomFunctions()
        _collaborationState.value = collaborationService.sessionState.value
        viewModelScope.launch {
            collaborationService.sessionState.collect {
                _collaborationState.value = it
            }
        }
    }

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
        _phase3InputA.value = ""
        _phase3InputB.value = ""
        _phase3InputC.value = ""
        _surfacePoints.value = emptyList()
        _moleculeStructure.value = null
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

    fun setPhase3Operation(operation: Phase3Operation) {
        _phase3Operation.value = operation
        _phase3InputA.value = ""
        _phase3InputB.value = ""
        _phase3InputC.value = ""
    }

    fun setPhase3Inputs(inputA: String? = null, inputB: String? = null, inputC: String? = null) {
        inputA?.let { _phase3InputA.value = it }
        inputB?.let { _phase3InputB.value = it }
        inputC?.let { _phase3InputC.value = it }
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
                CalculationMode.PHASE3 -> handlePhase3Calculation(expression)
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

    private fun handlePhase3Calculation(expression: String): Result<String> {
        return when (_phase3Operation.value) {
            Phase3Operation.MATRIX_DETERMINANT -> {
                calculatorService.matrixDeterminant(_phase3InputA.value)
                    .mapCatching { determinant -> "det(A) = $determinant" }
            }

            Phase3Operation.STATISTICS_SUMMARY -> {
                calculatorService.calculateStatistics(_phase3InputA.value)
                    .mapCatching(::formatStatistics)
            }

            Phase3Operation.BINOMIAL_PROBABILITY -> {
                val n = _phase3InputA.value.toIntOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid n"))
                val k = _phase3InputB.value.toIntOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid k"))
                val p = _phase3InputC.value.toDoubleOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid p"))

                calculatorService.binomialProbability(n, k, p)
                    .mapCatching { probability -> "P(X=$k) = $probability" }
            }

            Phase3Operation.DERIVATIVE -> {
                val x = _phase3InputA.value.toDoubleOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid x value"))
                val h = _phase3InputB.value.toDoubleOrNull() ?: 1e-5

                calculatorService.numericalDerivative(expression, x, h)
                    .mapCatching { derivative -> "f'($x) ≈ $derivative" }
            }

            Phase3Operation.INTEGRAL -> {
                val lower = _phase3InputA.value.toDoubleOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid lower bound"))
                val upper = _phase3InputB.value.toDoubleOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid upper bound"))
                val intervals = _phase3InputC.value.toIntOrNull() ?: 1000

                calculatorService.numericalIntegral(expression, lower, upper, intervals)
                    .mapCatching { area -> "∫[$lower,$upper] f(x)dx ≈ $area" }
            }

            Phase3Operation.CLOUD_SYNC -> {
                val action = _phase3InputA.value.trim().lowercase()
                if (action == "restore") {
                    Result.success("restore")
                } else {
                    Result.success("sync")
                }.mapCatching { command ->
                    if (command == "restore") {
                        viewModelScope.launch {
                            cloudSyncService.restoreFromCloud("local")
                                .onSuccess { _calculationResult.value = it }
                                .onFailure { _calculationResult.value = it.message ?: "Restore failed" }
                        }
                        "Restore started"
                    } else {
                        viewModelScope.launch {
                            cloudSyncService.syncToCloud("local")
                                .onSuccess { _calculationResult.value = it }
                                .onFailure { _calculationResult.value = it.message ?: "Sync failed" }
                        }
                        "Sync started"
                    }
                }
            }

            Phase3Operation.OFFLINE_GRAPHING -> {
                val name = _phase3InputA.value.ifBlank { "Preset-${System.currentTimeMillis()}" }
                val expressionToSave = _displayValue.value
                val xMin = _graphXMin.value.toDoubleOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid xMin value"))
                val xMax = _graphXMax.value.toDoubleOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid xMax value"))
                graphPresetService.savePreset(
                    SavedGraphPreset(
                        name = name,
                        expression = expressionToSave,
                        xMin = xMin,
                        xMax = xMax
                    )
                ).mapCatching {
                    refreshSavedPresets()
                    it
                }
            }

            Phase3Operation.GRAPH_3D -> {
                val xMin = _phase3InputA.value.toDoubleOrNull() ?: -5.0
                val xMax = _phase3InputB.value.toDoubleOrNull() ?: 5.0
                val yRange = _phase3InputC.value.toDoubleOrNull() ?: 5.0
                graphingService.generateSurfacePoints(
                    expression = if (expression.isBlank()) "sin(x) + cos(y)" else expression,
                    xMin = xMin,
                    xMax = xMax,
                    yMin = -yRange,
                    yMax = yRange
                ).mapCatching { points ->
                    _surfacePoints.value = points
                    "Generated ${points.size} 3D points"
                }
            }

            Phase3Operation.MOLECULE_VISUALIZATION -> {
                chemistryService.getMolecularStructure(_phase3InputA.value)
                    .mapCatching { structure ->
                        _moleculeStructure.value = structure
                        "Loaded structure: ${structure.name}"
                    }
            }

            Phase3Operation.COLLABORATION -> {
                val action = _phase3InputA.value.trim().lowercase()
                val value = _phase3InputB.value.trim()
                when (action) {
                    "create" -> collaborationService.createSession(value, _phase3InputC.value.ifBlank { "local-user" })
                    "join" -> collaborationService.joinSession(value)
                    else -> {
                        collaborationService.updateSharedExpression(value)
                        Result.success("Shared expression updated")
                    }
                }
            }

            Phase3Operation.CUSTOM_FUNCTION -> {
                val functionName = _phase3InputA.value.trim()
                val expressionOrX = _phase3InputB.value.trim()
                if (_phase3InputC.value.trim().equals("define", ignoreCase = true)) {
                    customFunctionService.defineFunction(functionName, expressionOrX)
                        .mapCatching {
                            refreshCustomFunctions()
                            it
                        }
                } else {
                    val x = _phase3InputC.value.toDoubleOrNull()
                        ?: return Result.failure(IllegalArgumentException("Provide x in Input C or set Input C to 'define'"))
                    customFunctionService.evaluateFunction(functionName, x)
                        .mapCatching { value -> "$functionName($x) = $value" }
                }
            }

            Phase3Operation.FORMULA_LIBRARY -> {
                formulaLibraryService.search(_phase3InputA.value)
                    .mapCatching { items ->
                        _formulaSearchResults.value = items
                        "Found ${items.size} formula(s)"
                    }
            }
        }
    }

    private fun formatStatistics(summary: StatisticsSummary): String {
        return "n=${summary.count}, mean=${summary.mean}, median=${summary.median}, sd=${summary.standardDeviation}, min=${summary.min}, max=${summary.max}"
    }

    private fun refreshSavedPresets() {
        graphPresetService.loadPresets()
            .onSuccess { _savedGraphPresets.value = it }
    }

    private fun refreshCustomFunctions() {
        _customFunctions.value = customFunctionService.listFunctions()
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
    BASIC, CHEMISTRY, TRIGONOMETRY, WORDPROBLEM, GRAPHING, PHASE3
}

enum class Phase3Operation {
    MATRIX_DETERMINANT,
    STATISTICS_SUMMARY,
    BINOMIAL_PROBABILITY,
    DERIVATIVE,
    INTEGRAL,
    CLOUD_SYNC,
    OFFLINE_GRAPHING,
    GRAPH_3D,
    MOLECULE_VISUALIZATION,
    COLLABORATION,
    CUSTOM_FUNCTION,
    FORMULA_LIBRARY
}

sealed class CalculatorState {
    data object Idle : CalculatorState()
    data object Loading : CalculatorState()
    data object Success : CalculatorState()
    data class Error(val message: String) : CalculatorState()
}

