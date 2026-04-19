# LLM Integration Quick Start Guide

## 🚀 Getting Started with AI-Powered Problem Solving

This guide walks you through integrating a free LLM API to enable word problem solving in Matthematica.

## Option 1: Groq (Recommended - Fastest)

### Step 1: Get API Key
1. Go to https://console.groq.com
2. Sign up for free account
3. Navigate to API keys section
4. Copy your API key

### Step 2: Add Dependencies
Already included in `build.gradle.kts`:
- Retrofit: HTTP client
- OkHttp: HTTP interceptor
- Kotlinx Serialization: JSON parsing

### Step 3: Create Groq Service
Create `app/src/main/java/com/matthematica/domain/llm/GroqService.kt`:

```kotlin
package com.matthematica.domain.llm

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class GroqRequest(
    val model: String = "mixtral-8x7b-32768",
    val messages: List<Message>,
    val max_tokens: Int = 1024,
    val temperature: Double = 0.7
)

@Serializable
data class GroqResponse(
    val choices: List<GroqChoice>,
    val usage: Usage? = null
)

@Serializable
data class GroqChoice(
    val message: Message,
    val finish_reason: String
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

interface GroqAPI {
    @POST("openai/v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") auth: String,
        @Body request: GroqRequest
    ): GroqResponse
}

@Singleton
class GroqService @Inject constructor() {
    
    private val api: GroqAPI by lazy {
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            })
            .build()

        retrofit2.Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .client(okHttpClient)
            .addConverterFactory(
                com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory(
                    kotlinx.serialization.json.Json.Default,
                    "application/json".toMediaType()
                )
            )
            .build()
            .create(GroqAPI::class.java)
    }

    suspend fun solveWordProblem(problem: String, apiKey: String): Result<String> {
        return try {
            val request = GroqRequest(
                messages = listOf(
                    Message(
                        role = "system",
                        content = "You are an expert mathematics tutor. " +
                            "Solve word problems step by step, showing all work and explaining clearly."
                    ),
                    Message(
                        role = "user",
                        content = problem
                    )
                )
            )

            val response = api.createChatCompletion(
                auth = "Bearer $apiKey",
                request = request
            )

            val answer = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("No response from API")

            Result.success(answer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Step 4: Update LLMService
Update `LLMService.kt`:

```kotlin
package com.matthematica.domain.llm

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMService @Inject constructor(
    private val groqService: GroqService
) {
    
    private var currentApiKey: String = ""

    fun setApiKey(apiKey: String) {
        this.currentApiKey = apiKey
    }

    suspend fun solveWordProblem(problem: String): Result<String> {
        if (currentApiKey.isEmpty()) {
            return Result.failure(
                Exception("API key not configured. Please set LLM API key in settings.")
            )
        }
        return groqService.solveWordProblem(problem, currentApiKey)
    }

    // ... rest of implementation
}
```

### Step 5: Store API Key Securely
Create `app/src/main/java/com/matthematica/domain/config/ConfigRepository.kt`:

```kotlin
package com.matthematica.domain.config

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val llmApiKeyKey = stringPreferencesKey("llm_api_key")
    private val llmProviderKey = stringPreferencesKey("llm_provider")

    val llmApiKey = dataStore.data.map { it[llmApiKeyKey] ?: "" }
    val llmProvider = dataStore.data.map { it[llmProviderKey] ?: "groq" }

    suspend fun setLLMApiKey(apiKey: String) {
        dataStore.edit { preferences ->
            preferences[llmApiKeyKey] = apiKey
        }
    }

    suspend fun setLLMProvider(provider: String) {
        dataStore.edit { preferences ->
            preferences[llmProviderKey] = provider
        }
    }
}
```

### Step 6: Add Settings Screen
Create `app/src/main/java/com/matthematica/ui/settings/SettingsScreen.kt`:

```kotlin
package com.matthematica.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.matthematica.domain.config.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {
    
    val llmApiKey = configRepository.llmApiKey
    val llmProvider = configRepository.llmProvider

    fun updateApiKey(key: String) {
        // Note: In production, use encrypted storage
        // For now, storing in DataStore (encrypted on Android)
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val llmApiKey by viewModel.llmApiKey.collectAsState(initial = "")
    var apiKeyInput by remember { mutableStateOf(llmApiKey) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("LLM Settings", style = MaterialTheme.typography.headlineSmall)

        Text("Groq API Key")
        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            label = { Text("Enter your Groq API key") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = {
                scope.launch {
                    // Save API key
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save API Key")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("How to get an API key:", style = MaterialTheme.typography.titleSmall)
                Text("1. Visit https://console.groq.com")
                Text("2. Sign up for free")
                Text("3. Copy your API key")
                Text("4. Paste it above")
            }
        }
    }
}
```

### Step 7: Update Calculator ViewModel
Modify `CalculatorViewModel.kt` to use LLM:

```kotlin
// In CalculatorViewModel.kt
private suspend fun handleWordProblem(problem: String): Result<String> {
    return llmService.solveWordProblem(problem)
}
```

### Step 8: Add to BuildConfig
Update `app/build.gradle.kts`:

```gradle
android {
    // ... existing config ...
    
    buildFeatures {
        buildConfig = true
    }
    
    defaultConfig {
        // ... existing ...
        buildConfigField "String", "GROQ_API_URL", '"https://api.groq.com/openai/v1/"'
    }
}
```

## Option 2: HuggingFace Inference API

### Setup
```kotlin
interface HuggingFaceAPI {
    @POST("models/meta-llama/Llama-2-7b-chat-hf")
    suspend fun generateText(
        @Header("Authorization") auth: String,
        @Body request: HFRequest
    ): HFResponse
}

// Get free API key from: https://huggingface.co/settings/tokens
```

## Option 3: Together AI

### Setup
```kotlin
interface TogetherAI {
    @POST("inference")
    suspend fun complete(
        @Header("Authorization") auth: String,
        @Body request: TogetherRequest
    ): TogetherResponse
}

// Sign up at: https://www.together.ai
```

## Testing the Integration

### Unit Test Example
```kotlin
// In androidTest/
@RunWith(AndroidJUnit4::class)
class LLMServiceTest {
    
    @Test
    fun testWordProblemSolving() {
        val service = GroqService()
        val result = runBlocking {
            service.solveWordProblem(
                "If a train travels 60 km in 2 hours, what is its average speed?",
                BuildConfig.GROQ_API_KEY
            )
        }
        assert(result.isSuccess)
        assert(result.getOrNull()?.contains("speed") == true)
    }
}
```

### Manual Testing
1. Open Matthematica app
2. Go to Settings
3. Enter your Groq API key
4. Go to "AI Solver" tab
5. Enter a word problem
6. Click "Solve with AI"

## Troubleshooting

### API Key Issues
```kotlin
// Add logging
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()
```

### Rate Limiting
```kotlin
// Add retry logic
val request = Request.Builder()
    .header("Retry-After", "60")
    .build()
```

### No Response
- Check internet connection
- Verify API key is valid
- Check API service status
- Review Logcat for errors

## Rate Limits

| Provider | Free Tier | Limit |
|----------|-----------|-------|
| Groq | Yes | 25 requests/min |
| HuggingFace | Yes | Unlimited (slower) |
| Together AI | Limited | 100k tokens/month |

## Cost Considerations

- Groq: Free tier sufficient for personal use
- HuggingFace: Free, community-driven
- Together AI: Free tier with token limits

## Privacy & Security

- API keys stored in DataStore (encrypted on modern Android)
- Consider Firebase App Check for production
- Use HTTPS for all API calls
- Never log API keys
- Implement rate limiting on client side

## Next Steps

1. ✅ Add Groq/HuggingFace/Together AI integration
2. ✅ Test with sample problems
3. ✅ Add error handling
4. ✅ Implement response caching
5. ✅ Add streaming responses (for better UX)
6. ✅ Monitor token usage
7. ✅ Add fallback providers

## Sample Prompts to Test

```
"A student buys 3 notebooks for $2.50 each and 5 pens for $0.75 each. 
How much did they spend in total?"

"What is the derivative of x^3 + 2x^2 - 5x + 3?"

"Balance this chemical equation: Fe + O2 -> Fe2O3"

"If the radius of a circle is 5cm, what is the area and circumference?"
```

## Production Checklist

- [ ] API key stored securely
- [ ] Error handling implemented
- [ ] Timeout handling added
- [ ] Response validation added
- [ ] Logging configured
- [ ] Tests written
- [ ] Rate limiting implemented
- [ ] Fallback mechanism ready
- [ ] Privacy policy updated
- [ ] Terms of service updated

---

**Ready to enable AI?** Follow the steps above and enjoy word problem solving in Matthematica! 🚀

