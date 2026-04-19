package com.matthematica.domain.calculator

import net.objecthunter.exp4j.ExpressionBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomFunctionService @Inject constructor() {

    private val customFunctions = linkedMapOf<String, String>()

    fun defineFunction(name: String, expression: String): Result<String> {
        return try {
            val functionName = name.trim()
            require(functionName.matches(Regex("[a-zA-Z][a-zA-Z0-9_]*"))) {
                "Function name must start with a letter and contain only letters, numbers, or _"
            }
            require(expression.isNotBlank()) { "Function expression cannot be empty" }

            // Validate expression with x variable before storing.
            ExpressionBuilder(expression).variables("x").build().setVariable("x", 1.0).evaluate()
            customFunctions[functionName] = expression
            Result.success("Defined $functionName(x) = $expression")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun evaluateFunction(name: String, x: Double): Result<Double> {
        return try {
            val expression = customFunctions[name.trim()]
                ?: return Result.failure(IllegalArgumentException("Unknown function: $name"))
            val value = ExpressionBuilder(expression).variables("x").build().setVariable("x", x).evaluate()
            Result.success(value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listFunctions(): List<NamedFunction> {
        return customFunctions.map { NamedFunction(it.key, it.value) }
    }
}

data class NamedFunction(
    val name: String,
    val expression: String
)

