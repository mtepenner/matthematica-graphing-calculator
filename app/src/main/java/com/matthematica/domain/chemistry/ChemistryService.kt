package com.matthematica.domain.chemistry

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChemistryService @Inject constructor() {

    private val elementMasses = mapOf(
        "H" to 1.008,
        "C" to 12.011,
        "N" to 14.007,
        "O" to 15.999,
        "P" to 30.974,
        "S" to 32.065,
        "Cl" to 35.453,
        "Br" to 79.904,
        "I" to 126.904,
        "Na" to 22.990,
        "K" to 39.098,
        "Ca" to 40.078,
        "Mg" to 24.305,
        "Fe" to 55.845,
        "Cu" to 63.546,
        "Zn" to 65.409,
        "Al" to 26.982,
        "Si" to 28.086,
        "B" to 10.811,
        "F" to 18.998
    )

    fun calculateMolarMass(formula: String): Result<Double> {
        return try {
            val mass = parseMolecularFormula(formula)
            Result.success(mass)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun balanceEquation(reactants: String, products: String): Result<String> {
        return try {
            // Simplified approach - in production would use more sophisticated algorithms
            val message = "Equation balancing requires advanced algorithms. For now, please use standard chemistry notation."
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun calculatePercentComposition(formula: String): Result<Map<String, Double>> {
        return try {
            val totalMass = parseMolecularFormula(formula)
            val elementCounts = extractElementCounts(formula)
            val composition = mutableMapOf<String, Double>()

            for ((element, count) in elementCounts) {
                val elementMass = (elementMasses[element] ?: 0.0) * count
                val percentage = (elementMass / totalMass) * 100
                composition[element] = percentage
            }

            Result.success(composition)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun calculateConcentration(moles: Double, volumeLiters: Double): Result<Double> {
        return try {
            if (volumeLiters <= 0) {
                return Result.failure(IllegalArgumentException("Volume must be positive"))
            }
            val molarity = moles / volumeLiters
            Result.success(molarity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun calculatepH(concentration: Double): Result<Double> {
        return try {
            if (concentration <= 0) {
                return Result.failure(IllegalArgumentException("Concentration must be positive"))
            }
            val pH = -kotlin.math.log10(concentration)
            Result.success(pH)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseMolecularFormula(formula: String): Double {
        var mass = 0.0
        var i = 0

        while (i < formula.length) {
            if (formula[i].isUpperCase()) {
                val element = StringBuilder()
                element.append(formula[i])
                i++

                while (i < formula.length && formula[i].isLowerCase()) {
                    element.append(formula[i])
                    i++
                }

                val elementSymbol = element.toString()
                val count = extractNumber(formula, i)

                mass += (elementMasses[elementSymbol] ?: 0.0) * count.first
                i = count.second
            } else {
                i++
            }
        }

        return mass
    }

    private fun extractElementCounts(formula: String): Map<String, Double> {
        val counts = mutableMapOf<String, Double>()
        var i = 0

        while (i < formula.length) {
            if (formula[i].isUpperCase()) {
                val element = StringBuilder()
                element.append(formula[i])
                i++

                while (i < formula.length && formula[i].isLowerCase()) {
                    element.append(formula[i])
                    i++
                }

                val elementSymbol = element.toString()
                val count = extractNumber(formula, i)

                counts[elementSymbol] = (counts[elementSymbol] ?: 0.0) + count.first
                i = count.second
            } else {
                i++
            }
        }

        return counts
    }

    private fun extractNumber(formula: String, startIndex: Int): Pair<Double, Int> {
        var index = startIndex
        var number = ""

        while (index < formula.length && formula[index].isDigit()) {
            number += formula[index]
            index++
        }

        val count = if (number.isEmpty()) 1.0 else number.toDouble()
        return Pair(count, index)
    }
}

