# Kotlin Explicit Backing Fields

**Status:** Experimental (Kotlin 2.3.0+)  
**Opt-in:** `-Xexplicit-backing-fields` compiler flag  
**Purpose:** Replace traditional backing property pattern with cleaner syntax and automatic smart casting

## Trigger Phrases
- "explicit backing field"
- "EBF"
- "backing property pattern"
- "MutableStateFlow to StateFlow"
- "private mutable public readonly"
- "field =" (in property context)
- "smart cast to backing field"

---

## Quick Syntax Reference

### Before: Traditional Backing Property
```kotlin
private val _state = MutableStateFlow(initial)
val state: StateFlow<T> get() = _state

fun update(value: T) {
    _state.value = value
}
```

### After: Explicit Backing Field
```kotlin
val state: StateFlow<T>
    field = MutableStateFlow(initial)

fun update(value: T) {
    state.value = value // automatic smart cast to MutableStateFlow
}
```

### Three Syntax Variants

**1. Inferred field type (recommended):**
```kotlin
val items: List<Item>
    field = mutableListOf()
```

**2. Explicit field type:**
```kotlin
val state: StateFlow<UiState>
    field: MutableStateFlow<UiState> = MutableStateFlow(UiState())
```

**3. Deferred initialization:**
```kotlin
val data: LiveData<String>
    field: MutableLiveData<String>

init {
    data = MutableLiveData("")
}
```

---

## Key Rules

### Requirements
- ✅ Must be `val` (not `var`)
- ✅ Must be `final` (not `open`, `abstract`, or `override`)
- ✅ No custom getter allowed
- ✅ No delegation allowed
- ✅ Field type must be subtype of property type
- ✅ Field visibility is always `private`
- ✅ Property visibility must be more permissive than field

### Smart Casting Behavior
- **Within declaring scope** (class/file): Automatic smart cast to field type
- **Outside scope**: No smart cast, uses property type
- **Public/internal/protected inline functions**: Smart cast disabled
- **Private inline functions**: Smart cast works normally

### Call-Site Mechanics
```kotlin
class Example {
    val state: StateFlow<Int>
        field = MutableStateFlow(0)
    
    fun update() {
        state.value = 42 // Compiles to: (getState() as MutableStateFlow).setValue(42)
                         // But optimized to direct field access
    }
}

fun outside(ex: Example) {
    ex.state // Type is StateFlow, no smart cast
}
```

---

## Common Patterns

### Android ViewModel (StateFlow)
```kotlin
class MyViewModel : ViewModel() {
    val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())
    
    fun updateState(newState: UiState) {
        uiState.value = newState // smart cast to MutableStateFlow
    }
    
    fun updatePartial(transform: (UiState) -> UiState) {
        uiState.update(transform) // smart cast enables update()
    }
}
```

### Repository (Collections)
```kotlin
class UserRepository {
    val users: List<User>
        field = mutableListOf()
    
    fun addUser(user: User) {
        users.add(user) // smart cast to MutableList
    }
    
    fun removeUser(id: String) {
        users.removeIf { it.id == id } // smart cast
    }
}
```

### LiveData (Android)
```kotlin
class DataViewModel : ViewModel() {
    val data: LiveData<String>
        field = MutableLiveData("")
    
    fun updateData(value: String) {
        data.value = value // smart cast to MutableLiveData
    }
    
    fun postData(value: String) {
        data.postValue(value) // smart cast
    }
}
```

---

## When to Use / Not Use

### ✅ Use Explicit Backing Fields When:
- Simple read-only/mutable type pairs
- Private backing property pattern
- No transformation logic needed
- Common patterns:
  - `MutableStateFlow` → `StateFlow`
  - `MutableSharedFlow` → `SharedFlow`
  - `MutableLiveData` → `LiveData`
  - `MutableList` → `List`
  - `MutableSet` → `Set`
  - `MutableMap` → `Map`

### ❌ Use Traditional Backing Properties When:
- **Transformation needed:**
  ```kotlin
  private val _state = MutableStateFlow(initial)
  val state: StateFlow<T> get() = _state.asStateFlow() // Returns wrapper
  ```

- **Custom getter logic:**
  ```kotlin
  private val _count = AtomicInt(0)
  val count: Int get() = _count.value // Extracts value
  ```

- **Non-private backing property:**
  ```kotlin
  protected val _items = mutableListOf<Item>() // Visible to subclasses
  val items: List<Item> get() = _items
  ```

- **Var properties:**
  ```kotlin
  var mutableProperty: String = "" // EBF only works with val
  ```

- **Delegated properties:**
  ```kotlin
  val lazy: String by lazy { "value" } // Cannot combine with EBF
  ```

- **Complex storage (AtomicInt, WeakReference, ThreadLocal):**
  ```kotlin
  private val _ref = WeakReference(value)
  val value: T? get() = _ref.get()
  ```

---

## Migration Steps

### 1. Enable Compiler Flag
```kotlin
// build.gradle.kts
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
}
```

### 2. Identify Eligible Patterns
Look for:
```kotlin
private val _property = MutableType()
val property: ReadOnlyType get() = _property
```

### 3. Verify Eligibility
- ✅ No transformation in getter
- ✅ Backing property is private
- ✅ Property is val
- ✅ No custom logic

### 4. Replace with EBF Syntax
```kotlin
// Before
private val _items = mutableListOf<Item>()
val items: List<Item> get() = _items

// After
val items: List<Item>
    field = mutableListOf()
```

### 5. Remove Underscore Property
Delete the `private val _property` declaration.

### 6. Update References
References automatically use smart cast within the class:
```kotlin
// Before
_items.add(item)

// After
items.add(item) // smart cast handles this
```

### 7. Test
- Verify compilation succeeds
- Check smart casts work in class scope
- Verify external access uses property type

---

## Common Errors & Fixes

### "Smart cast is impossible"
**Cause:** Property doesn't meet smart cast requirements  
**Fix:** Ensure property is `val`, `final`, and has no custom getter

```kotlin
// ❌ Wrong
open val state: StateFlow<Int>
    field = MutableStateFlow(0)

// ✅ Correct
val state: StateFlow<Int>
    field = MutableStateFlow(0)
```

### "Type mismatch: inferred type is X but Y was expected"
**Cause:** Field type is not a subtype of property type  
**Fix:** Ensure type hierarchy is correct

```kotlin
// ❌ Wrong
val state: MutableStateFlow<Int>
    field = StateFlow(0) // StateFlow is not subtype of MutableStateFlow

// ✅ Correct
val state: StateFlow<Int>
    field = MutableStateFlow(0) // MutableStateFlow is subtype of StateFlow
```

### "Explicit backing field is not allowed here"
**Cause:** Property violates EBF restrictions  
**Fix:** Check for `var`, `open`, delegation, or custom getter

```kotlin
// ❌ Wrong
var state: StateFlow<Int>
    field = MutableStateFlow(0)

// ✅ Correct
val state: StateFlow<Int>
    field = MutableStateFlow(0)
```

### "Cannot access from inline function"
**Cause:** Smart cast disabled in public/internal/protected inline functions  
**Fix:** Make inline function private or use traditional backing property

```kotlin
class Example {
    val state: StateFlow<Int>
        field = MutableStateFlow(0)
    
    // ❌ Wrong
    inline fun update(value: Int) {
        state.value = value // Smart cast not available
    }
    
    // ✅ Correct
    private inline fun update(value: Int) {
        state.value = value // Smart cast works in private inline
    }
}
```

---

## Multiplatform Considerations

### Expect-Actual Matching
```kotlin
// commonMain
expect class Repository {
    val items: List<Item> // Cannot have EBF in expect
}

// androidMain
actual class Repository {
    val items: List<Item>
        field = mutableListOf() // Can have EBF in actual
}
```

**Rules:**
- `expect` declarations cannot have explicit backing fields (implementation detail)
- `actual` declarations can have explicit backing fields
- Only property types are considered during expect-actual matching
- Field types play no role in matching

---

## Why Migrate to EBF

### Benefits
✅ **Less boilerplate** — No separate underscore property  
✅ **Cleaner code** — Single property declaration  
✅ **Type safety** — Automatic smart cast within scope  
✅ **Modern idiom** — Aligns with Kotlin 2.3+ best practices  
✅ **Better readability** — No underscore naming convention needed  
✅ **Reduced errors** — Compiler enforces smart cast safety

### Before vs After Comparison
```kotlin
// Before: 3 declarations, underscore naming, manual access
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> get() = _uiState

fun update(state: UiState) {
    _uiState.value = state
}

// After: 1 declaration, natural naming, automatic smart cast
val uiState: StateFlow<UiState>
    field = MutableStateFlow(UiState())

fun update(state: UiState) {
    uiState.value = state // cleaner!
}
```

---

## Best Practices

### Naming
- ✅ Use natural property names (no underscores)
- ✅ Name reflects the public API, not implementation

```kotlin
// ✅ Good
val users: List<User>
    field = mutableListOf()

// ❌ Avoid (no need for underscore)
val _users: List<User>
    field = mutableListOf()
```

### Type Inference
- ✅ Let compiler infer field type when obvious
- ✅ Specify explicitly for clarity in complex cases

```kotlin
// ✅ Good (obvious)
val items: List<Item>
    field = mutableListOf()

// ✅ Good (explicit for clarity)
val state: StateFlow<ComplexType>
    field: MutableStateFlow<ComplexType> = MutableStateFlow(initial)
```

### Initialization
- ✅ Initialize inline when possible
- ✅ Use init block for complex initialization

```kotlin
// ✅ Good (simple)
val state: StateFlow<Int>
    field = MutableStateFlow(0)

// ✅ Good (complex)
val data: LiveData<Result>
    field: MutableLiveData<Result>

init {
    data = MutableLiveData()
    loadInitialData()
}
```

### Migration Strategy
- ✅ Migrate simple cases first
- ✅ Keep complex cases as backing properties
- ✅ Prefer EBF for new code
- ✅ Migrate existing code incrementally

---

## References

- **KEEP Proposal:** [Explicit Backing Fields](https://github.com/Kotlin/KEEP/blob/explicit-backing-fields/proposals/explicit-backing-fields.md)
- **YouTrack Issue:** [KT-14663](https://youtrack.jetbrains.com/issue/KT-14663)
- **Kotlin Docs:** [What's New in Kotlin 2.3.0](https://kotlinlang.org/docs/whatsnew23.html#explicit-backing-fields)

### Related Skills
- `android-presentation-mvi` — ViewModel patterns with state management
- `kotlin-project-state-management` — State holder patterns in KMP
- `kotlin-data-kmp-data-layer` — Repository patterns with EBF

---

## Summary

Explicit Backing Fields simplify the common pattern of exposing read-only types backed by mutable implementations. When you have a simple `private val _x` + `val x get() = _x` pattern with no transformation logic, migrate to EBF syntax for cleaner, more maintainable code. Enable with `-Xexplicit-backing-fields` and enjoy automatic smart casting within your class scope.

**Remember:** EBF is for simple type exposure. Use traditional backing properties when you need transformation, custom logic, or non-private visibility.
