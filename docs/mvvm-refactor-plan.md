# MVVM Refactor Plan

## Goals
- Separate stateful presentation logic from Swing components so views focus on rendering and delegating user intent.
- Introduce ViewModel classes with unit tests to capture behaviour without depending on Swing.
- Enable incremental migration: existing screens keep working while we peel logic into ViewModels.

## Architecture Overview
- Add a new package `org.homeunix.thecave.buddi.viewmodel` for ViewModel classes and supporting abstractions.
- Define a light `ViewModel` contract (e.g., marker interface plus optional `PropertyChangeSupport`) to standardise interaction between views and viewmodels.
- Expose observable state via immutable DTOs / plain getters; push user commands through explicit methods (e.g., `load()`, `selectAccount`, `refreshNetWorth`).
- Use existing model layer (`Document`, `Account`, etc.) inside the ViewModels; no data binding framework required.
- Allow ViewModels to publish events via `PropertyChangeSupport` or callback interfaces so Swing views can stay updated.

## Migration Strategy
1. **Foundation**
   - Create base ViewModel abstractions and shared utility classes (e.g., `ObservableViewModel`).
   - Provide DTOs for data the views need (account rows, totals, selection info).
2. **Per-View Extraction**
   - For each view, identify responsibilities (state queries, command handling, event listeners).
   - Move logic into a corresponding `*ViewModel` class, leaving the view to bind listeners and render.
   - Adapt existing listeners (e.g., `DocumentChangeListener`) to live in the ViewModel; expose callbacks back to the Swing component when visual updates are required.
3. **View Wiring**
   - Instantiate the ViewModel in the view constructor (or inject it), subscribe to its events, and delegate user actions (button clicks, selections) into ViewModel commands.
   - Replace direct model access (`parent.getDocument()` etc.) with ViewModel API calls.
4. **Testing & Cleanup**
   - Add focused unit tests for each ViewModel in `junit/org/homeunix/thecave/buddi/test/viewmodel`.
   - Remove dead code and ensure listeners are registered/unregistered appropriately.

## First Iteration (Pilot)
- Target `MyAccountsPanel` as the pilot view.
  - Extract a new `MyAccountsViewModel` managing account tree data, selection, expansion persistence, and net-worth computation.
  - ViewModel responsibilities:
    - Provide account tree nodes/rows and expose expansion states.
    - React to `DocumentChangeEvent` internally, exposing a simple `refresh()` hook for the view.
    - Compute text for the balance label (net worth) and expose formatted value or raw number.
    - Surface commands for double-click (open transactions) and selection changes.
  - Update `MyAccountsPanel` to consume the ViewModel, keeping only Swing wiring.
  - Add unit tests covering:
    - Net worth formatting / currency logic.
    - Expansion state persistence when Document updates occur.
    - Selection helpers (`getSelectedAccounts`, `getSelectedTypes`).

## Testing Strategy
- Use existing JUnit infrastructure (`junit/org/homeunix/thecave/buddi/test`).
- Create dedicated test packages mirroring the main source tree (e.g., `.../viewmodel`).
- Mock or stub dependencies like `PrefsModel` or `Document` where necessary, using simple fake implementations.
- Ensure each ViewModel test verifies behaviour without instantiating Swing components.

## Next Steps Snapshot
- ✅ Publish plan (this document).
- ☐ Implement ViewModel foundation (`ViewModel` interface + base support).
- ☐ Extract `MyAccountsViewModel` and accompanying tests.
- ☐ Refactor `MyAccountsPanel` to use the new ViewModel.
- ☐ Review and iteratively expand to other views once the pilot stabilises.
