package com.matthematica.domain.calculator

import net.objecthunter.exp4j.ExpressionBuilder
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.util.FastMath
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Singleton
class CalculatorService @Inject constructor() {

    fun evaluateExpression(expression: String): Result<Double> {
        return try {
            val cleanExpression = expression
                .replace("π", PI.toString())
                .replace("e", Math.E.toString())
                .replace("√", "sqrt")
                .replace("²", "^2")
                .replace("³", "^3")

            val expr = ExpressionBuilder(cleanExpression)
                .variables("x", "y", "z", "π", "e")
                .function("sqrt", 1) { args -> sqrt(args[0]) }
                .function("sin", 1) { args -> sin(args[0]) }
                .function("cos", 1) { args -> cos(args[0]) }
                .function("tan", 1) { args -> tan(args[0]) }
                .function("asin", 1) { args -> FastMath.asin(args[0]) }
                .function("acos", 1) { args -> FastMath.acos(args[0]) }
                .function("atan", 1) { args -> atan(args[0]) }
                .function("sinh", 1) { args -> FastMath.sinh(args[0]) }
                .function("cosh", 1) { args -> FastMath.cosh(args[0]) }
                .function("tanh", 1) { args -> FastMath.tanh(args[0]) }
                .function("log", 1) { args -> FastMath.log(args[0]) }
                .function("log10", 1) { args -> FastMath.log10(args[0]) }
                .function("ln", 1) { args -> FastMath.log(args[0]) }
                .function("exp", 1) { args -> FastMath.exp(args[0]) }
                .function("abs", 1) { args -> FastMath.abs(args[0]) }
                .function("factorial", 1) { args -> factorial(args[0]) }
                .build()

            val result = expr.evaluate()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun solveQuadratic(a: Double, b: Double, c: Double): Result<Pair<Complex, Complex>> {
        return try {
            if (a == 0.0) {
                return Result.failure(IllegalArgumentException("'a' cannot be zero for quadratic equation"))
            }

            val discriminant = b * b - 4 * a * c
            val sqrtDiscriminant = if (discriminant >= 0) {
                Complex(sqrt(discriminant), 0.0)
            } else {
                Complex(0.0, sqrt(-discriminant))
            }

            val x1 = Complex(-b, 0.0).add(sqrtDiscriminant).divide(Complex(2 * a, 0.0))
            val x2 = Complex(-b, 0.0).subtract(sqrtDiscriminant).divide(Complex(2 * a, 0.0))

            Result.success(Pair(x1, x2))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun trigonometricCalculation(function: String, value: Double, isDegrees: Boolean = true): Result<Double> {
        return try {
            val radianValue = if (isDegrees) Math.toRadians(value) else value

            val result = when (function.lowercase()) {
                "sin" -> sin(radianValue)
                "cos" -> cos(radianValue)
                "tan" -> tan(radianValue)
                "asin" -> FastMath.asin(value)
                "acos" -> FastMath.acos(value)
                "atan" -> atan(value)
                "sinh" -> FastMath.sinh(radianValue)
                "cosh" -> FastMath.cosh(radianValue)
                "tanh" -> FastMath.tanh(radianValue)
                "cot" -> 1.0 / tan(radianValue)
                "sec" -> 1.0 / cos(radianValue)
                "csc" -> 1.0 / sin(radianValue)
                else -> throw IllegalArgumentException("Unknown function: $function")
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun solveLinearEquation(a: Double, b: Double): Result<Double> {
        return try {
            if (a == 0.0) {
                return Result.failure(IllegalArgumentException("'a' cannot be zero for linear equation"))
            }
            val x = -b / a
            Result.success(x)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun polynomialFactors(coefficients: List<Double>): Result<String> {
        return try {
            // Simple polynomial description
            val factors = coefficients
                .mapIndexed { index, coeff ->
                    val power = coefficients.size - 1 - index
                    when {
                        coeff == 0.0 -> ""
                        power == 0 -> coeff.toString()
                        power == 1 -> "${coeff}x"
                        else -> "${coeff}x^$power"
                    }
                }
                .filter { it.isNotEmpty() }
                .joinToString(" + ")

            Result.success(factors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != n.toLong().toDouble()) {
            throw IllegalArgumentException("Factorial is only defined for non-negative integers")
        }
        var result = 1.0
        for (i in 2..n.toLong()) {
            result *= i
        }
        return result
    }
}

