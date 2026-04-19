# Matthematica - Advanced Graphing Calculator

## Project Overview

Matthematica is a comprehensive Android graphing calculator application built with Kotlin and Jetpack Compose. It provides advanced mathematical capabilities including polynomial equation solving, chemistry formula calculations, trigonometry functions, and AI-powered word problem solving.

## Architecture

### Project Structure

```
com.matthematica/
├── data/
│   ├── database/
│   │   ├── MatthematicaDatabase.kt      # Room database configuration
│   │   └── dao/
│   │       ├── UserDao.kt               # User data access
│   │       └── CalculationHistoryDao.kt # History data access
│   └── model/
│       └── Models.kt                    # Data models (User, CalculationHistory)
├── domain/
│   ├── auth/
│   │   └── AuthRepository.kt            # Authentication logic
│   ├── calculator/
│   │   ├── CalculatorService.kt         # Math calculations
│   │   └── GraphingService.kt           # Graph point generation
│   ├── chemistry/
│   │   └── ChemistryService.kt          # Chemistry utilities
│   └── llm/
│       └── LLMService.kt                # AI integration
├── ui/
│   ├── auth/
│   │   ├── AuthScreens.kt               # Login/Signup UI
│   │   └── AuthViewModel.kt             # Auth state management
│   ├── calculator/
│   │   ├── CalculatorScreens.kt         # Calculator UI
│   │   └── CalculatorViewModel.kt       # Calculator state
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── di/
│   └── DataModule.kt                    # Dependency injection
└── MainActivity.kt
```

## Key Features

### 1. Authentication System
- **Local Authentication**: Email/password-based signup and login
- **Session Management**: Persistent user sessions using DataStore
- **User Data Storage**: SQLite database with Room ORM

### 2. Calculator Engine
**Basic Operations**:
- Arithmetic operations (+, -, *, /)
- Exponential notation
- Parentheses support

**Advanced Math**:
- Polynomial equation solving
- Quadratic equation solver (with complex numbers)
- Linear equation solver
- Trigonometric functions (sin, cos, tan, asin, acos, atan)
- Hyperbolic functions (sinh, cosh, tanh)
- Logarithmic functions (log, ln, log10)
- Factorial, absolute value

### 3. Chemistry Tools
- **Molar Mass Calculation**: Parse molecular formulas and calculate molar mass
- **Percent Composition**: Determine element composition percentages
- **Concentration Calculations**: Molarity calculations
- **pH Calculations**: Calculate pH from concentration
- **Equation Balancing**: Framework for chemical equation balancing

### 4. Trigonometry Functions
- Degree/Radian conversion support
- All standard trig functions
- Inverse trig functions
- Hyperbolic functions

### 5. LLM Integration
- **Word Problem Solver**: Use free LLM APIs to solve complex problems step-by-step
- **Concept Explanation**: Detailed explanations of mathematical concepts
- **Solution Verification**: Check user solutions
- **Supported Providers**:
  - Groq (Fastest, free tier with rate limits)
  - HuggingFace Inference API
  - Together AI

### 6. History & Persistence
- **Calculation History**: All calculations saved with timestamp
- **Search & Filter**: Find previous calculations by expression, result, or category
- **Favorites**: Mark important calculations
- **Offline Access**: History stored locally in database

### 7. Graphing
- **Function Plotting**: Plot `f(x)` expressions from the calculator screen
- **Custom Domain**: Configure `xMin` and `xMax` ranges before plotting
- **Interactive Chart**: Pan/zoom with MPAndroidChart gestures
- **Robust Sampling**: Invalid/non-finite points are filtered before rendering

## Technical Stack

### Dependencies
- **Jetpack Compose**: UI framework
- **Room**: Local database
- **DataStore**: Preferences and session management
- **Hilt**: Dependency injection
- **Retrofit**: HTTP client (for future API integration)
- **exp4j**: Expression evaluation
- **Apache Commons Math**: Advanced mathematics
- **MPAndroidChart**: Graphing and visualization
- **Kotlinx Serialization**: JSON serialization

### Build Configuration
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **AGP**: 8.6.1
- **Kotlin**: 2.0.21
- **Gradle**: 8.10.2

## Getting Started

### Prerequisites
- Android Studio Giraffe or newer
- JDK 11+
- Android SDK 36

### Setup Instructions

1. **Clone and Open Project**
   ```bash
   git clone https://github.com/mtepenner/matthematica-graphing-calculator.git
   cd Matthematica
   ```

2. **Build the Project**
   ```bash
   ./gradlew build
   ```

3. **Run on Emulator/Device**
   ```bash
   ./gradlew installDebug
   ```

### Configuration

#### LLM API Integration
To enable AI-powered problem solving:

1. Sign up for free API access:
   - **Groq**: https://console.groq.com (25 requests/min free)
   - **HuggingFace**: https://huggingface.co/inference-api (Unlimited free)
   - **Together AI**: https://www.together.ai

2. Add API key to `local.properties`:
   ```properties
   LLM_API_KEY=your_api_key_here
   LLM_API_PROVIDER=groq
   ```

3. Update `LLMService.kt` with actual API endpoint

#### Database
- Room database automatically created on first run
- Located in app's private directory
- Automatic schema validation

#### Authentication
- Passwords hashed before storage
- Consider upgrading to BCrypt for production
- Session tokens stored in encrypted DataStore

## Usage Examples

### Basic Calculation
```kotlin
val result = calculatorService.evaluateExpression("2 + 3 * 4")
// Result: 14.0
```

### Quadratic Solver
```kotlin
val roots = calculatorService.solveQuadratic(1.0, -5.0, 6.0)
// Result: x₁ = 3.0, x₂ = 2.0
```

### Chemistry
```kotlin
val mass = chemistryService.calculateMolarMass("H2O")
// Result: 18.015
```

### Word Problem (AI)
```kotlin
val solution = llmService.solveWordProblem(
    "A train travels 60 km in 2 hours. What is its average speed?"
)
// Result: Detailed step-by-step solution
```

## Roadmap

### Phase 1 (Completed)
- ✅ Basic calculator
- ✅ Authentication
- ✅ History tracking
- ✅ Trig functions
- ✅ Chemistry basics
- ✅ LLM integration framework

### Phase 2 (Completed)
- ✅ Function graphing with `f(x)` input
- ✅ Configurable graph domain (`xMin`, `xMax`)
- ✅ Interactive plotting with MPAndroidChart (pan/zoom)
- ✅ Graph point sampling + filtering for non-finite values
- ✅ Unit tests for graph generation paths

### Phase 3 (In Progress)
- ✅ Matrix determinant operations
- ✅ Statistics summary (mean/median/std-dev/min/max)
- ✅ Probability (binomial PMF)
- ✅ Numerical calculus (derivative and integral)
- ✅ Cloud sync for history
- ✅ Offline graphing
- ✅ 3D graphing
- ✅ Advanced chemistry (molecular structure visualization)
- ✅ Real-time collaboration
- ✅ Custom function definition
- ✅ Formula library/search

## Best Practices

### Code Organization
- Separation of concerns (UI, Domain, Data)
- Dependency injection for testability
- ViewModel for state management
- Sealed classes for type-safe results

### Performance
- Lazy database queries
- Efficient math library usage
- Memory-efficient history pagination
- Cached LLM responses

### Security
- No hardcoded API keys
- Encrypted local storage
- Input validation for all calculations
- Safe exception handling

## Testing

Run tests with:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## License

This project is licensed under the MIT License - see LICENSE file for details

## Support

For issues and questions:
- Open an issue on GitHub
- Check existing documentation
- Review implementation examples

## Acknowledgments

- Jetpack Compose team for excellent UI framework
- exp4j for expression evaluation
- Apache Commons Math for mathematical algorithms
- Free LLM providers (Groq, HuggingFace, Together AI)

