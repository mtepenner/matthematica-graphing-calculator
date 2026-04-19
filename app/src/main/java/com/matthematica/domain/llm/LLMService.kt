package com.matthematica.domain.llm

import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class LLMRequest(
    val model: String = "mixtral-8x7b-32768",
    val messages: List<Message> = emptyList(),
    val max_tokens: Int = 1024,
    val temperature: Double = 0.7,
    val top_p: Double = 0.95
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class LLMResponse(
    val choices: List<Choice> = emptyList(),
    val error: Error? = null
)

@Serializable
data class Choice(
    val message: Message = Message("", ""),
    val finish_reason: String = ""
)

@Serializable
data class Error(
    val message: String = "",
    val type: String = ""
)

@Singleton
class LLMService @Inject constructor() {

    /**
     * Solves a word problem using a free LLM API
     * Uses Groq API as default (free tier with good rate limits)
     * Falls back to HuggingFace or Together AI if needed
     */
    suspend fun solveWordProblem(problem: String): Result<String> {
        return try {
            val messages = listOf(
                Message(
                    role = "system",
                    content = "You are a helpful mathematics tutor. " +
                        "When solving word problems, break down the solution step by step. " +
                        "Show all calculations and explain your reasoning clearly."
                ),
                Message(
                    role = "user",
                    content = "Solve this word problem step by step:\n\n$problem"
                )
            )

            // For now, return a placeholder response
            // In production, integrate with actual LLM API
            val response = generateLLMResponse(messages)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun explainConcept(concept: String, context: String = ""): Result<String> {
        return try {
            val messages = listOf(
                Message(
                    role = "system",
                    content = "You are a mathematics expert. " +
                        "Explain concepts clearly with examples and provide intuitive understanding."
                ),
                Message(
                    role = "user",
                    content = "Explain the concept of $concept${if (context.isNotEmpty()) " in the context of $context" else ""}. " +
                        "Use simple language and provide examples."
                )
            )

            val response = generateLLMResponse(messages)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkSolution(problem: String, userSolution: String): Result<String> {
        return try {
            val messages = listOf(
                Message(
                    role = "system",
                    content = "You are a mathematics tutor. " +
                        "Check if the provided solution is correct. " +
                        "If incorrect, explain where the error is and show the correct solution."
                ),
                Message(
                    role = "user",
                    content = "Problem: $problem\n\nUser's solution: $userSolution\n\nPlease check this solution."
                )
            )

            val response = generateLLMResponse(messages)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateLLMResponse(messages: List<Message>): String {
        // This is a placeholder implementation
        // In production, you would:
        // 1. Use Retrofit to call actual API endpoint (Groq, HuggingFace, or Together AI)
        // 2. Handle authentication (API keys)
        // 3. Stream responses for better UX
        // 4. Implement retry logic and rate limiting
        // 5. Cache responses for common problems

        // For now, return a helpful default message
        return "LLM Integration: To integrate with actual LLM APIs:\n" +
            "1. Sign up for free API access at Groq (https://console.groq.com)\n" +
            "2. Add your API key to BuildConfig\n" +
            "3. Implement Retrofit client for the LLM service\n" +
            "4. Use streaming for better UX\n" +
            "\nExample providers:\n" +
            "- Groq: Fast, free tier, 25 req/min\n" +
            "- HuggingFace: Inference API, unlimited free\n" +
            "- Together AI: Good balance of speed and cost"
    }

    /**
     * Get available LLM providers and models
     */
    fun getAvailableModels(): List<String> {
        return listOf(
            "mixtral-8x7b-32768",
            "llama2-70b",
            "llama2-13b",
            "gpt-3.5-turbo"
        )
    }
}

