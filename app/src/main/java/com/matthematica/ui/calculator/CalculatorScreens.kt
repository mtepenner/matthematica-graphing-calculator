package com.matthematica.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel
) {
    val displayValue by viewModel.displayValue.collectAsState()
    val calculationResult by viewModel.calculationResult.collectAsState()
    val calculatorState by viewModel.calculatorState.collectAsState()
    val calculationMode by viewModel.calculationMode.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Tab selection
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; viewModel.setMode(CalculationMode.BASIC) },
                    text = { Text("Basic") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; viewModel.setMode(CalculationMode.TRIGONOMETRY) },
                    text = { Text("Trig") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; viewModel.setMode(CalculationMode.CHEMISTRY) },
                    text = { Text("Chemistry") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3; viewModel.setMode(CalculationMode.WORDPROBLEM) },
                    text = { Text("AI Solver") }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4; viewModel.setMode(CalculationMode.GRAPHING) },
                    text = { Text("Graph") }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5; viewModel.setMode(CalculationMode.PHASE3) },
                    text = { Text("Phase 3") }
                )
            }

            // Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (calculationResult.isNotEmpty()) {
                        Text(
                            text = "= $calculationResult",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Status messages
            when (calculatorState) {
                is CalculatorState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is CalculatorState.Error -> {
                    Text(
                        text = "Error: ${(calculatorState as CalculatorState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {}
            }

            // Content based on selected mode
            when (selectedTab) {
                0 -> BasicCalculatorUI(viewModel)
                1 -> TrigonometryCalculatorUI(viewModel)
                2 -> ChemistryCalculatorUI(viewModel)
                3 -> WordProblemSolverUI(viewModel)
                4 -> GraphingCalculatorUI(viewModel)
                5 -> Phase3CalculatorUI(viewModel)
            }
        }
    }
}

@Composable
private fun BasicCalculatorUI(viewModel: CalculatorViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        val buttons = listOf(
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("0", ".", "=", "+")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { label ->
                    Button(
                        onClick = {
                            when (label) {
                                "=" -> viewModel.calculate()
                                "C" -> viewModel.clear()
                                else -> viewModel.appendValue(label)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.clear() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Clear")
        }
    }
}

@Composable
private fun TrigonometryCalculatorUI(viewModel: CalculatorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Trigonometry Functions",
            style = MaterialTheme.typography.titleMedium
        )

        val trigFunctions = listOf("sin", "cos", "tan", "asin", "acos", "atan")
        trigFunctions.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { func ->
                    Button(
                        onClick = {
                            viewModel.appendValue("$func(")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(func)
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.calculate() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }
    }
}

@Composable
private fun ChemistryCalculatorUI(viewModel: CalculatorViewModel) {
    val displayValue by viewModel.displayValue.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Chemistry Tools",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = displayValue,
            onValueChange = viewModel::setDisplayValue,
            label = { Text("Molecular Formula") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.calculate() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate Molar Mass")
        }
    }
}

@Composable
private fun WordProblemSolverUI(viewModel: CalculatorViewModel) {
    val displayValue by viewModel.displayValue.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "AI-Powered Problem Solver",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = displayValue,
            onValueChange = viewModel::setDisplayValue,
            label = { Text("Word Problem") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            maxLines = 5
        )

        Button(
            onClick = { viewModel.calculate() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Solve with AI")
        }
    }
}

@Composable
private fun GraphingCalculatorUI(viewModel: CalculatorViewModel) {
    val expression by viewModel.displayValue.collectAsState()
    val xMin by viewModel.graphXMin.collectAsState()
    val xMax by viewModel.graphXMax.collectAsState()
    val graphPoints by viewModel.graphPoints.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Graphing", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = expression,
            onValueChange = viewModel::setDisplayValue,
            label = { Text("f(x)") },
            placeholder = { Text("Example: sin(x) + x^2") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = xMin,
                onValueChange = { newValue -> viewModel.setGraphRange(newValue, xMax) },
                label = { Text("xMin") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = xMax,
                onValueChange = { newValue -> viewModel.setGraphRange(xMin, newValue) },
                label = { Text("xMax") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = { viewModel.calculate() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Plot")
        }

        if (graphPoints.isNotEmpty()) {
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setTouchEnabled(true)
                        setPinchZoom(true)
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    val entries = graphPoints.map { point -> Entry(point.x, point.y) }
                    val dataSet = LineDataSet(entries, "f(x)").apply {
                        lineWidth = 2f
                        color = android.graphics.Color.BLUE
                        setDrawCircles(false)
                        setDrawValues(false)
                    }
                    chart.data = LineData(dataSet)
                    chart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }
    }
}

@Composable
private fun Phase3CalculatorUI(viewModel: CalculatorViewModel) {
    val operation by viewModel.phase3Operation.collectAsState()
    val inputA by viewModel.phase3InputA.collectAsState()
    val inputB by viewModel.phase3InputB.collectAsState()
    val inputC by viewModel.phase3InputC.collectAsState()
    val expression by viewModel.displayValue.collectAsState()
    val presets by viewModel.savedGraphPresets.collectAsState()
    val formulaResults by viewModel.formulaSearchResults.collectAsState()
    val customFunctions by viewModel.customFunctions.collectAsState()
    val surfacePoints by viewModel.surfacePoints.collectAsState()
    val moleculeStructure by viewModel.moleculeStructure.collectAsState()
    val collaborationState by viewModel.collaborationState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Phase 3 - Advanced Math", style = MaterialTheme.typography.titleMedium)

        val operationButtons = listOf(
            Phase3Operation.MATRIX_DETERMINANT to "Matrix det",
            Phase3Operation.STATISTICS_SUMMARY to "Statistics",
            Phase3Operation.BINOMIAL_PROBABILITY to "Binomial",
            Phase3Operation.DERIVATIVE to "Derivative",
            Phase3Operation.INTEGRAL to "Integral",
            Phase3Operation.CLOUD_SYNC to "Cloud sync",
            Phase3Operation.OFFLINE_GRAPHING to "Offline graph",
            Phase3Operation.GRAPH_3D to "3D graph",
            Phase3Operation.MOLECULE_VISUALIZATION to "Molecule",
            Phase3Operation.COLLABORATION to "Collab",
            Phase3Operation.CUSTOM_FUNCTION to "Custom fn",
            Phase3Operation.FORMULA_LIBRARY to "Formula search"
        )

        operationButtons.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (target, label) ->
                    Button(
                        onClick = { viewModel.setPhase3Operation(target) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (operation == target) "* $label" else label)
                    }
                }
            }
        }

        when (operation) {
            Phase3Operation.MATRIX_DETERMINANT -> {
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("Matrix A") },
                    placeholder = { Text("Example: 1,2;3,4") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Phase3Operation.STATISTICS_SUMMARY -> {
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("Values") },
                    placeholder = { Text("Example: 2,4,4,4,5,5,7,9") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Phase3Operation.BINOMIAL_PROBABILITY -> {
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("n (trials)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputB,
                    onValueChange = { viewModel.setPhase3Inputs(inputB = it) },
                    label = { Text("k (successes)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputC,
                    onValueChange = { viewModel.setPhase3Inputs(inputC = it) },
                    label = { Text("p (success probability)") },
                    placeholder = { Text("Example: 0.5") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Phase3Operation.DERIVATIVE -> {
                OutlinedTextField(
                    value = expression,
                    onValueChange = viewModel::setDisplayValue,
                    label = { Text("f(x)") },
                    placeholder = { Text("Example: x^3 + sin(x)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("x") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputB,
                    onValueChange = { viewModel.setPhase3Inputs(inputB = it) },
                    label = { Text("h (optional)") },
                    placeholder = { Text("Defaults to 1e-5") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Phase3Operation.INTEGRAL -> {
                OutlinedTextField(
                    value = expression,
                    onValueChange = viewModel::setDisplayValue,
                    label = { Text("f(x)") },
                    placeholder = { Text("Example: x^2") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("Lower bound a") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputB,
                    onValueChange = { viewModel.setPhase3Inputs(inputB = it) },
                    label = { Text("Upper bound b") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputC,
                    onValueChange = { viewModel.setPhase3Inputs(inputC = it) },
                    label = { Text("Intervals (optional)") },
                    placeholder = { Text("Defaults to 1000") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Phase3Operation.CLOUD_SYNC -> {
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("Action") },
                    placeholder = { Text("Type 'sync' or 'restore'") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Phase3Operation.OFFLINE_GRAPHING -> {
                OutlinedTextField(
                    value = expression,
                    onValueChange = viewModel::setDisplayValue,
                    label = { Text("f(x)") },
                    placeholder = { Text("Example: sin(x)+x^2") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("Preset name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Saved presets: ${presets.size}")
                presets.take(3).forEach { preset ->
                    Text("- ${preset.name}: ${preset.expression} on [${preset.xMin}, ${preset.xMax}]")
                }
            }

            Phase3Operation.GRAPH_3D -> {
                OutlinedTextField(
                    value = expression,
                    onValueChange = viewModel::setDisplayValue,
                    label = { Text("z = f(x,y)") },
                    placeholder = { Text("Example: sin(x) + cos(y)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("xMin (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputB,
                    onValueChange = { viewModel.setPhase3Inputs(inputB = it) },
                    label = { Text("xMax (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputC,
                    onValueChange = { viewModel.setPhase3Inputs(inputC = it) },
                    label = { Text("|y| range (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (surfacePoints.isNotEmpty()) {
                    Text("3D points: ${surfacePoints.size}")
                    Text("Preview point: (${surfacePoints.first().x}, ${surfacePoints.first().y}, ${surfacePoints.first().z})")
                }
            }

            Phase3Operation.MOLECULE_VISUALIZATION -> {
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("Molecule formula") },
                    placeholder = { Text("Try H2O, CO2, CH4, NH3") },
                    modifier = Modifier.fillMaxWidth()
                )
                moleculeStructure?.let { structure ->
                    Text("Structure: ${structure.name}")
                    Text("Atoms: ${structure.atoms.joinToString { it.symbol }}")
                    Text("Bonds: ${structure.bonds.size}")
                }
            }

            Phase3Operation.COLLABORATION -> {
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("Action") },
                    placeholder = { Text("create | join | update") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputB,
                    onValueChange = { viewModel.setPhase3Inputs(inputB = it) },
                    label = { Text("Value") },
                    placeholder = { Text("Session ID / Display Name / Expression") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputC,
                    onValueChange = { viewModel.setPhase3Inputs(inputC = it) },
                    label = { Text("Name (for create)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Session: ${collaborationState.sessionId.ifBlank { "(none)" }}")
                Text("Participants: ${collaborationState.participants.joinToString().ifBlank { "(none)" }}")
                Text("Shared: ${collaborationState.sharedExpression.ifBlank { "(empty)" }}")
            }

            Phase3Operation.CUSTOM_FUNCTION -> {
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("Function name") },
                    placeholder = { Text("Example: f1") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputB,
                    onValueChange = { viewModel.setPhase3Inputs(inputB = it) },
                    label = { Text("Expression or value") },
                    placeholder = { Text("Define: x^2+1 | Evaluate: ignored") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputC,
                    onValueChange = { viewModel.setPhase3Inputs(inputC = it) },
                    label = { Text("Mode") },
                    placeholder = { Text("Type 'define' or x value") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (customFunctions.isNotEmpty()) {
                    Text("Defined functions:")
                    customFunctions.forEach { fn -> Text("- ${fn.name}(x) = ${fn.expression}") }
                }
            }

            Phase3Operation.FORMULA_LIBRARY -> {
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { viewModel.setPhase3Inputs(inputA = it) },
                    label = { Text("Search query") },
                    placeholder = { Text("Try algebra, integral, chemistry") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (formulaResults.isNotEmpty()) {
                    Text("Matches:")
                    formulaResults.take(5).forEach { item ->
                        Text("- ${item.name}: ${item.expression}")
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.calculate() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Run Phase 3 Calculation")
        }
    }
}

