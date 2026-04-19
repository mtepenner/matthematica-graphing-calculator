package com.matthematica.domain.calculator

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormulaLibraryService @Inject constructor() {

    private val formulaLibrary = listOf(
        FormulaItem("Quadratic Formula", "x = (-b ± sqrt(b^2 - 4ac)) / 2a", "algebra"),
        FormulaItem("Slope Formula", "m = (y2 - y1) / (x2 - x1)", "algebra"),
        FormulaItem("Distance Formula", "d = sqrt((x2-x1)^2 + (y2-y1)^2)", "geometry"),
        FormulaItem("Circle Area", "A = πr^2", "geometry"),
        FormulaItem("Derivative (Power Rule)", "d/dx[x^n] = n*x^(n-1)", "calculus"),
        FormulaItem("Integral (Power Rule)", "∫x^n dx = x^(n+1)/(n+1) + C", "calculus"),
        FormulaItem("Binomial PMF", "P(X=k)=C(n,k)p^k(1-p)^(n-k)", "probability"),
        FormulaItem("Ideal Gas Law", "PV = nRT", "chemistry")
    )

    fun search(query: String): Result<List<FormulaItem>> {
        return try {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                Result.success(formulaLibrary)
            } else {
                val results = formulaLibrary.filter { item ->
                    item.name.contains(trimmed, ignoreCase = true) ||
                        item.expression.contains(trimmed, ignoreCase = true) ||
                        item.category.contains(trimmed, ignoreCase = true)
                }
                Result.success(results)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class FormulaItem(
    val name: String,
    val expression: String,
    val category: String
)

