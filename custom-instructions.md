# Buddi Rewrite - Custom Instructions

## Core Philosophy
We are rewriting Buddi, a personal finance application, from Swing to JavaFX using a strict MVVM (Model-View-ViewModel) "PresentationReady" architecture.

## Architecture: MVVM (PresentationReady Style)

### 1. The View (JavaFX)
- **Passive View**: The View should contain NO business logic. It only handles UI logic (animations, layout).
- **Code-based UI**: Prefer code-based UI construction for type safety and refactoring ease.
- **Data Binding**: Use JavaFX Properties extensively. Bind View properties to ViewModel properties.
- **CSS**: Use external CSS files for styling. Avoid inline styles.

### 2. The ViewModel
- **Presentation Logic**: The ViewModel holds the state of the View. It transforms Domain Models into a format ready for presentation.
- **No JavaFX Nodes**: The ViewModel must NOT reference any JavaFX UI nodes (Node, Button, etc.). It should only use JavaFX Properties (StringProperty, BooleanProperty, etc.) and Collections (ObservableList).
- **Commands**: Expose actions as `Runnable` or `Consumer` interfaces.
- **Testability**: ViewModels must be unit-testable without a running JavaFX toolkit.

### 3. The Model
- **Domain Objects**: Pure Java objects (POJOs) representing the business data (Account, Transaction, Budget).
- **Services/Repositories**: Classes that handle data fetching, saving, and business rules. These are injected into ViewModels.

## Coding Standards
- **Java 17+**: Use modern Java features (Records, Switch expressions, var).
- **Dependency Injection**: Use Constructor Injection. Avoid static access to singletons where possible.
- **Testing**:
    - **JUnit 5**: For all unit tests.
    - **TestFX**: For UI integration tests.
    - **Mockito**: For mocking dependencies in ViewModel tests.

## "PresentationReady" Specifics
- **State Encapsulation**: The ViewModel should expose the *entire* state required by the View.
- **One-Way vs Two-Way**: Prefer One-Way binding (ViewModel -> View) where possible.
