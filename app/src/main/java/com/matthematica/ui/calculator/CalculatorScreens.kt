package com.matthematica.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp

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
            value = "Enter molecular formula (e.g., H2O, NaCl)",
            onValueChange = { viewModel.appendValue(it) },
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
            value = "Enter your word problem here...",
            onValueChange = { viewModel.appendValue(it) },
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

