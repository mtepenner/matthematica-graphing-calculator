package com.matthematica.domain.calculator

import net.objecthunter.exp4j.ExpressionBuilder
import org.apache.commons.math3.util.FastMath
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Singleton
class GraphingService @Inject constructor() {

    fun generateGraphPoints(
        expression: String,
        xMin: Double,
        xMax: Double,
        sampleCount: Int = 300
    ): Result<List<GraphPoint>> {
        return try {
            require(expression.isNotBlank()) { "Expression cannot be empty" }
            require(xMax > xMin) { "xMax must be greater than xMin" }
            require(sampleCount >= 2) { "sampleCount must be at least 2" }

            val cleanExpression = expression
                .replace("π", PI.toString())
                .replace("e", Math.E.toString())
                .replace("√", "sqrt")
                .replace("²", "^2")
                .replace("³", "^3")

            val compiled = ExpressionBuilder(cleanExpression)
                .variables("x")
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
                .build()

            val points = mutableListOf<GraphPoint>()
            val range = xMax - xMin

            for (index in 0 until sampleCount) {
                val x = xMin + (index.toDouble() / (sampleCount - 1)) * range
                val y = compiled.setVariable("x", x).evaluate()

                // Skip non-finite values to avoid chart rendering errors.
                if (x.isFinite() && y.isFinite()) {
                    points.add(GraphPoint(x.toFloat(), y.toFloat()))
                }
            }

            if (points.isEmpty()) {
                Result.failure(IllegalArgumentException("No plottable points were generated"))
            } else {
                Result.success(points)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class GraphPoint(
    val x: Float,
    val y: Float
)

