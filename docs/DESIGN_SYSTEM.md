---

## UI Design System

HitchLogMP uses **Material 3 (Material You)** with a custom color scheme inspired by the Guild of Hitchhiking Masters (ГМА) identity: deep blue + amber yellow, evoking road signs, asphalt, and movement.

The design is **minimalist and functional** — a field app used in real time during a race. No decorative clutter. Every element must serve a purpose.

---

### Color Tokens (Material 3)

Define these in a `Theme.kt` file in `commonMain`:

```kotlin
// Light Theme
val Primary = Color(0xFF1A3A8F)          // Deep road-sign blue
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFD9E2FF)
val OnPrimaryContainer = Color(0xFF001258)

val Secondary = Color(0xFFFFCC00)        // ГМА amber yellow
val OnSecondary = Color(0xFF1A1A00)
val SecondaryContainer = Color(0xFFFFF0A0)
val OnSecondaryContainer = Color(0xFF1A1400)

val Tertiary = Color(0xFF5C6BC0)         // Softer blue for supporting elements
val OnTertiary = Color(0xFFFFFFFF)

val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)

val Background = Color(0xFFF8F9FF)       // Very slightly blue-tinted white
val OnBackground = Color(0xFF1A1B20)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1A1B20)
val SurfaceVariant = Color(0xFFE4E5F0)
val OnSurfaceVariant = Color(0xFF44464F)
val Outline = Color(0xFF74757F)
val OutlineVariant = Color(0xFFC4C5D0)
```

---

### Typography

Use **default Material 3 typography** (no custom fonts needed — keep it simple and fast to load). Apply these roles consistently:

| Role | Usage |
|---|---|
| `headlineLarge` | Screen titles (rare) |
| `headlineMedium` | Log name, section headers |
| `titleLarge` | List item primary text, card titles |
| `titleMedium` | Record type label, dialog titles |
| `bodyLarge` | Record text/note content |
| `bodyMedium` | Secondary info, timestamps |
| `labelLarge` | Button labels |
| `labelMedium` | Chips, badges, status labels |
| `labelSmall` | Meta info (record count, date subtitles) |

---

### Shape

Use Material 3 defaults:
- `ExtraSmall` (4dp) — chips, small badges
- `Small` (8dp) — input fields
- `Medium` (12dp) — cards, list items
- `Large` (16dp) — bottom sheets, dialogs
- `ExtraLarge` (28dp) — FAB, large buttons

---

### Spacing

Consistent padding grid (multiples of 4dp):
- Screen edge padding: `16.dp`
- Section spacing: `24.dp`
- Item internal padding: `12.dp` vertical, `16.dp` horizontal
- Between related items: `8.dp`
- Between unrelated sections: `16.dp`

---

### Record Type Chips

Each `HitchLogRecordType` has a visual identity. Use `SuggestionChip` or colored badges:

| Type | Icon | Color role |
|---|---|---|
| `START` | `🚩` DirectionsRun | Primary |
| `LIFT` | `🚗` DirectionsCar | Secondary (amber) |
| `GET_OFF` | `⬇` ExitToApp | Secondary (amber) |
| `WALK` | `🚶` DirectionsWalk | Tertiary |
| `WALK_END` | `⬛` Stop | Tertiary |
| `CHECKPOINT` | `📍` Place | Primary |
| `MEET` | `👋` People | Outline/Surface |
| `REST_ON` | `💤` Hotel | SurfaceVariant |
| `REST_OFF` | `▶` PlayArrow | SurfaceVariant |
| `OFFSIDE_ON` | `⏸` Pause | Error container |
| `OFFSIDE_OFF` | `▶` PlayArrow | Error container |
| `FINISH` | `🏁` Flag | Primary |
| `RETIRE` | `✖` Cancel | Error |
| `FREE_TEXT` | `📝` Notes | Surface |

---

### Screen Specifications

#### AuthScreen

**Layout:** centered column, full screen, `Background` color.

- Top area (40% height): App logo/icon — a road triangle shape (can be a simple `Canvas` drawing of the ГМА triangle using Primary + Secondary colors), app name "HitchLog" in `headlineMedium`, subtitle "Гильдия Мастеров Автостопа" in `bodyMedium` / `OnSurfaceVariant`
- Middle: `Spacer(Modifier.weight(1f))`
- Bottom area: two buttons stacked, full width with `16.dp` horizontal padding:
  1. `Button` (filled) — "Войти через Google", leading `Icon(Google)`
  2. `OutlinedButton` — "Войти анонимно"
- Bottom padding: `32.dp`

**No decorative elements.** Clean, focused.

---

#### LogListScreen

**Layout:** `Scaffold` with `TopAppBar` + `FloatingActionButton` + `LazyColumn`.

- `TopAppBar`: title "Мои хроники", actions: `IconButton` with person icon → sign out confirmation
- `FAB`: `ExtendedFloatingActionButton`, icon `Add`, text "Новая хроника", color `Primary`
- List items: `ListItem` component with:
  - `headlineContent`: log name in `titleLarge`
  - `supportingContent`: race/team info in `bodyMedium` / `OnSurfaceVariant`
  - `trailingContent`: `IconButton` edit (pencil icon)
  - Click → navigate to `Screen.Log`
  - Divider between items: `HorizontalDivider` with `OutlineVariant`
- Empty state: centered column, icon `DirectionsWalk` large in `OutlineVariant`, text "Нет хроник" in `bodyLarge`, subtext "Создайте первую хронику" in `bodyMedium`
- Loading: `LinearProgressIndicator` at top
- Error: `Snackbar` via `SnackbarHost`

---

#### EditLogScreen (create/edit log)

**Layout:** `Scaffold` with `TopAppBar` + bottom save button.

- `TopAppBar`: title "Новая хроника" or "Редактировать", `NavigationIcon` back arrow
- Body: `Column` with `16.dp` padding:
  - `OutlinedTextField`, full width, label "Название хроники", `singleLine = true`
  - `Spacer(16.dp)`
  - Race ID field (if applicable): `OutlinedTextField`, label "Соревнование"
  - `Spacer(16.dp)`
  - Team ID field: `OutlinedTextField`, label "Команда (необязательно)"
- Bottom: `Button` full width, "Сохранить", inside `16.dp` padding from bottom
- If editing existing: show `TextButton` "Удалить хронику" in `Error` color below save button, with confirmation `AlertDialog` before deletion

---

#### HitchLogScreen (main chronicle view)

**Layout:** `Scaffold` with `TopAppBar` + `FAB` + `LazyColumn`.

- `TopAppBar`: log name as title, back navigation
- `FAB`: `FloatingActionButton` with `Add` icon, `Primary` color → navigate to `Screen.EditRecord`
- List: records grouped by date (sticky date headers):
  - Date header: `Text` in `labelMedium` / `OnSurfaceVariant`, `SurfaceVariant` background, full width, `8.dp` vertical / `16.dp` horizontal padding
  - Record item: `ListItem`:
    - `leadingContent`: record type chip/icon (see Record Type Chips table)
    - `headlineContent`: record type name in `titleMedium`
    - `supportingContent`: `text` field in `bodyMedium` if non-empty
    - `trailingContent`: time in `labelMedium` / `OnSurfaceVariant` (format `HH:mm`)
    - Click → navigate to `Screen.EditRecord`
- Records are ordered chronologically (ASC by time)
- Loading / error states same as LogListScreen

---

#### EditRecordScreen (add/edit record)

**Layout:** `Scaffold` with `TopAppBar`.

- `TopAppBar`: "Новая запись" or record type name, back navigation, trailing `IconButton` delete (trash icon, `Error` color) if editing
- Body: `Column`, `16.dp` padding, vertically scrollable:
  1. **Type selector** (if new record): `LazyRow` of `FilterChip`s for each `HitchLogRecordType`, scrollable horizontally. Selected chip uses `Primary` colors. Group chips: movement types first (LIFT, GET_OFF, WALK, WALK_END), then race events (START, FINISH, CHECKPOINT, REST_ON, REST_OFF, OFFSIDE_ON, OFFSIDE_OFF, MEET, RETIRE), then FREE_TEXT last.
  2. `Spacer(16.dp)`
  3. **Date + Time row**: two `OutlinedTextField`s side by side (weight 1f each), gap `8.dp`. Date label "Дата" format `dd.MM.yyyy`, Time label "Время" format `HH:mm`.
  4. `Spacer(16.dp)`
  5. **Text field**: `OutlinedTextField`, full width, label context-aware:
     - LIFT → "Марка автомобиля"
     - CHECKPOINT → "Номер КП и информация"
     - MEET → "Участник"
     - others → "Заметка (необязательно)"
     - `minLines = 3` for FREE_TEXT, else `singleLine = true`
- Bottom: `Button` full width "Сохранить", `16.dp` padding

---

### Component Patterns

**Loading state:**
```kotlin
Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
}
```

**Error state (Snackbar):**
Use `SnackbarHostState` in Scaffold. Show error via `LaunchedEffect`.

**Empty state:**
```kotlin
Column(
    Modifier.fillMaxSize().padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Icon(Icons.Outlined.X, contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.outlineVariant)
    Spacer(Modifier.height(16.dp))
    Text("Primary message", style = MaterialTheme.typography.bodyLarge)
    Text("Secondary message", style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}
```

**Confirmation dialog (delete):**
Use `AlertDialog` with `confirmButton` in `Error` color and `dismissButton` as `TextButton`.

---

### Theme Setup

Create `composeApp/src/commonMain/kotlin/org/gmautostop/hitchlogmp/ui/Theme.kt`:

```kotlin
@Composable
fun HitchLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary,
            onPrimary = OnPrimary,
            primaryContainer = PrimaryContainer,
            onPrimaryContainer = OnPrimaryContainer,
            secondary = Secondary,
            onSecondary = OnSecondary,
            secondaryContainer = SecondaryContainer,
            onSecondaryContainer = OnSecondaryContainer,
            // ... rest of tokens
            background = Background,
            onBackground = OnBackground,
            surface = Surface,
            onSurface = OnSurface,
            surfaceVariant = SurfaceVariant,
            onSurfaceVariant = OnSurfaceVariant,
            outline = Outline,
            outlineVariant = OutlineVariant,
            error = Error,
            onError = OnError,
            errorContainer = ErrorContainer,
        ),
        content = content
    )
}
```

Wrap the root composable in `HitchLogApp.kt` with `HitchLogTheme { ... }`.

---

### Design Rules (for Claude Code)

1. **Never use hardcoded colors** — always `MaterialTheme.colorScheme.X`
2. **Never use hardcoded text styles** — always `MaterialTheme.typography.X`
3. **No decorative illustrations** — icons only (Material Icons)
4. **Consistent padding** — use the spacing grid (multiples of 4dp)
5. **Loading always shown** — never show empty list while loading
6. **Error always shown** — always surface errors via Snackbar or inline text
7. **Touch targets minimum 48dp** — all interactive elements
8. **Screen edge padding always 16.dp** — no content touching screen edge
