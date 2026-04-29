# Design Handoff: RecordEditScreen + CheckpointInfoScreen
## HitchlogMP · Compose Multiplatform

---

## 1. RecordEditScreen

### 1.1 Navigation

Reached via `Screen.EditRecord(logId, recordId = "", recordType)`.
- Empty `recordId` = new record mode → title "Новая запись"
- Non-empty `recordId` = edit mode → title "Редактирование"
- `recordType` is always set by the caller (from QuickActions). The user cannot change it on this screen.

---

### 1.2 Screen layout (top to bottom)

```
TopAppBar
─────────────────────
TypeStrip
─────────────────────
DateRow
─────────────────────
TimeRow
─────────────────────
NoteRow  (fills remaining height)
─────────────────────
SaveBar
```

All rows sit on a single white `Surface`. Background color is `MaterialTheme.colorScheme.background` (`#F8F9FF`). The surface fills the screen edge to edge with no card gaps.

---

### 1.3 TopAppBar

```
[ × ]   "Новая запись"   [ 🗑 ]
──────────────────────────────────
```

- `navigationIcon`: `Icons.Default.Close` — dismisses without saving (back navigation)
- `title`: "Новая запись" (new) or "Редактирование" (edit)
- `actions`: `Icons.Default.Delete` in `MaterialTheme.colorScheme.error` — **only shown in edit mode** (when `recordId` is non-empty). Deletes the record after confirmation.
- Bottom divider: `HorizontalDivider()` below the TopAppBar
- **No Save action in the top bar** — save lives at the bottom only

---

### 1.4 TypeStrip

```
[ colored circle icon ]  TypeName
```

- Background: `MaterialTheme.colorScheme.surfaceVariant` (`#F2F3FA`)
- Bottom divider: `HorizontalDivider()`
- Layout: `Row`, padding `14.dp` vertical, `20.dp` horizontal
- The type chip is **read-only** — type comes from the caller, cannot be changed here
- Chip: `Surface(shape = CircleShape)` with type-specific background color + `Icon` inside, followed by type name text `fontSize = 14.sp, fontWeight = Normal`
- Type chip colors (`chipColors(type: HitchLogRecordType)`):

| Type | Circle background | Icon tint |
|---|---|---|
| START | `primary` (`#1A3A8F`) | `onPrimary` |
| LIFT | `secondary` (`#FFCC00`) | `onSecondary` (`#1A1A00`) |
| GET_OFF | `secondary` (`#FFCC00`) | `onSecondary` |
| WALK | `tertiary` (blue-purple) | `onTertiary` |
| WALK_END | `tertiary` | `onTertiary` |
| CHECKPOINT | `primary` | `onPrimary` |
| MEET | `primaryContainer` | `onPrimaryContainer` |
| REST_ON | `surfaceVariant` | `onSurfaceVariant` |
| REST_OFF | `surfaceVariant` | `onSurfaceVariant` |
| OFFSIDE_ON | `error` | `onError` |
| OFFSIDE_OFF | `surfaceVariant` | `onSurfaceVariant` |
| FINISH | `primary` | `onPrimary` |
| RETIRE | `error` | `onError` |
| FREE_TEXT | `surfaceVariant` | `onSurfaceVariant` |

- Type icons (Material Symbols Outlined):

| Type | Icon |
|---|---|
| START | `directions_run` |
| LIFT | `directions_car` |
| GET_OFF | `exit_to_app` |
| WALK | `directions_walk` |
| WALK_END | `directions_walk` (or `stop`) |
| CHECKPOINT | `location_on` |
| MEET | `group` |
| REST_ON | `hotel` |
| REST_OFF | `hotel` (with check) |
| OFFSIDE_ON | `pause_circle` |
| OFFSIDE_OFF | `play_circle` |
| FINISH | `flag` |
| RETIRE | `cancel` |
| FREE_TEXT | `notes` |

---

### 1.5 DateRow

```
[ calendar_month ]   16.05.2026, сб       [ − ] [ + ]
```

- Height: `56.dp` minimum
- Bottom divider: `HorizontalDivider()`
- Leading icon: `Icons.Outlined.CalendarMonth`, `24.dp`, tint `onSurfaceVariant`
- Value: `fontSize = 20.sp`, `fontWeight = Normal`, `fontVariantNumeric = TabularNums`
  - Format: `dd.MM.yyyy, EEE` (e.g. "16.05.2026, сб") using Russian locale
- Trailing: two `FilledTonalIconButton(size = 36.dp, shape = CircleShape)`:
  - `−` button: `Icons.Default.Remove` → subtracts 1 day
  - `+` button: `Icons.Default.Add` → adds 1 day
- Tapping the value text opens the OS date picker as a fallback for arbitrary dates

---

### 1.6 TimeRow

```
[ schedule ]   11:12          [ − ] [ + ]
              ─────────────────────────────
              [ −10 ] [ −5 ]  [ 0 ]  [ +5 ] [ +10 ]
```

- Bottom divider: `HorizontalDivider()`
- Leading icon: `Icons.Outlined.Schedule`, `24.dp`, tint `onSurfaceVariant`

**Top line:**
- Time value: `fontSize = 40.sp`, `fontWeight = Medium`, `fontVariantNumeric = TabularNums`, color `onSurface`
- Blue underline on the time value: `Box` with `Modifier.drawBehind { drawLine(primary, ...) }` at bottom — signals it is tappable/editable
- Tapping the time value focuses a hidden `BasicTextField` → keyboard opens for HH:mm manual entry
- Trailing: two `FilledTonalIconButton(size = 40.dp)`:
  - `−`: subtract 1 minute
  - `+`: add 1 minute
  - Long-press on either: continuous stepping (1/sec, accelerating)

**Shortcut rail** (below, indented to align under value column, `padding start = 40.dp`):
- 5 chips in a `Row`, equal weight via `Modifier.weight(1f)`:
  - `−10`, `−5` → `FilledTonalButton`, `height = 34.dp`, `shape = CircleShape`
  - `0` → `FilledButton` (primary fill), `height = 34.dp`, `shape = CircleShape`, `fontSize = 15.sp` — sets time to current device time
  - `+5`, `+10` → `FilledTonalButton`

---

### 1.7 REST_OFF Pair Hint Banner

Shown **only for `REST_OFF`** records, inserted between TypeStrip and DateRow.
Not shown for any other type.

```
[ schedule icon ]  Rest начат в 12:20 · продолжается 34 мин
```

- Background: `#FFF8E1` (amber-50), text color `#854F0B`
- Height: `40.dp`, padding `8.dp` vertical, `16.dp` horizontal
- Content: "Rest начат в HH:mm · продолжается N мин"
- Data: look up the most recent `REST_ON` record in the current log to get start time; compute elapsed as `now - REST_ON.time`
- If no matching `REST_ON` found → don't show the banner

---

### 1.8 NoteRow

```
[ notes icon ]
МАРКА АВТОМОБИЛЯ
[Например: Volvo XC60, Михаил, до Зеленогорска        ]
```

- Fills remaining vertical space (`Modifier.weight(1f)`)
- Leading icon: `Icons.Outlined.Notes`, tint `onSurfaceVariant`, aligned to top
- Label: `10.sp`, `fontWeight = Medium`, `letterSpacing = 0.6.sp`, `UPPERCASE`, color `onSurfaceVariant`
- Below label: `BasicTextField` that fills the remaining space, `fontSize = 18.sp`, `lineHeight = 25.sp`
- Auto-focuses on new record (`LaunchedEffect(Unit) { focusRequester.requestFocus() }`)
- **Type-aware label and placeholder:**

| Type | Label | Placeholder |
|---|---|---|
| START | Заметка | Не обязательно |
| LIFT | Марка автомобиля | Например: Volvo XC60, Михаил, до Зеленогорска |
| GET_OFF | Место выгрузки | Развязка у Зеленогорска |
| WALK | Куда идём | Выход к трассе А-181 «Скандинавия» |
| WALK_END | Место | Не обязательно |
| CHECKPOINT | Номер КП | КП-1 / КП Сестрорецк |
| MEET | Номер участника | № 7, команда «Север-2» |
| REST_ON | Заметка | Не обязательно |
| REST_OFF | Заметка | Не обязательно |
| OFFSIDE_ON | Место и причина | Заехали к родственникам водителя |
| OFFSIDE_OFF | Заметка | Не обязательно |
| FINISH | Номер КП | КП финиш / Дворцовая площадь |
| RETIRE | Место и причина | Травма, ст. Удельная |
| FREE_TEXT | Заметка | Свободный текст |

---

### 1.9 CHECKPOINT / FINISH extra row

For `CHECKPOINT` and `FINISH` types only, insert an additional tappable row **below the NoteRow** (before the SaveBar):

```
[ list icon ]   Гоночная информация           ›
                Предыдущие 4 участника · не заполнено
```

- Height: `56.dp`, bottom divider above SaveBar
- Leading icon: `Icons.Outlined.List`
- Title: "Гоночная информация", `fontSize = 14.sp`
- Subtitle: fill state — "не заполнено" when no marks entered, "N из 4 заполнено" when partial, "заполнено" when ≥4 marks entered
- Trailing: `Icons.Default.ChevronRight`
- Tap: navigates to `CheckpointInfoScreen(logId, recordId)`

---

### 1.10 SaveBar

```
─────────────────────────────
[ СОХРАНИТЬ                 ]
```

- `Surface` with `Modifier.fillMaxWidth()`, `border top = HorizontalDivider()`
- Padding: `12.dp` top, `16.dp` horizontal, `14.dp` bottom
- Button: `Button(modifier = Modifier.fillMaxWidth(), shape = CircleShape(24.dp))`
  - Text: "СОХРАНИТЬ", `fontSize = 14.sp`, `letterSpacing = 0.8.sp`
  - Background: `MaterialTheme.colorScheme.primary`
  - Height: `48.dp`
- On tap: calls `RecordViewModel.save()`, then navigates back

---

### 1.11 ViewModel changes needed

The existing `RecordViewModel` needs these additions:

```kotlin
// New methods to add:
fun adjustDate(days: Int)       // add/subtract days from current date
fun adjustTime(minutes: Int)    // add/subtract minutes from current time
fun setTimeToNow()              // sets time to current device time (the "0" button)

// Already exists but verify format:
// updateDate(date: String)  — format "dd.MM.yyyy"
// updateTime(time: String)  — format "HH:mm"
```

Fix the known issue: `RecordViewModel.kt:79` date parse failure is silently swallowed — show an error state.

---

## 2. CheckpointInfoScreen

### 2.1 Overview

A sub-screen opened from `RecordEditScreen` when the record type is `CHECKPOINT` or `FINISH`. Allows the user to transcribe control marks (контрольные марки) found at the checkpoint into the chronicle.

**Data note:** The `HitchLogRecord` model does not yet have a field for checkpoint mark data. Until the data model is extended, store the mark data serialized as JSON in the existing `text` field of the record. The planned structure is described in section 2.8.

---

### 2.2 Navigation

Add to `Screen`:
```kotlin
data class Screen.CheckpointInfo(val logId: String, val recordId: String)
```

Top bar: back arrow (`Icons.AutoMirrored.Default.ArrowBack`) → returns to RecordEditScreen without a separate save step. Data is saved as part of the parent CHECKPOINT record when the user taps "Сохранить" on RecordEditScreen.

Top bar title: the КП name from the parent record's `text` field (e.g. "КП Сестрорецк"), or "Гоночная информация" if not yet set.

Top bar trailing action: "Готово" text button → same as back.

---

### 2.3 Screen layout

```
TopAppBar  ("КП Сестрорецк"  [Готово])
─────────────────────────────
scrollable body:
  MarkCard #1  (filled)
  MarkCard #2  (filled)
  MarkCard #3  (in progress)
  [ + Добавить марку ]
  FreeTextCard
─────────────────────────────
SaveBar  [Готово]
```

Background: `MaterialTheme.colorScheme.background` (`#F8F9FF`)
Cards have `8.dp` spacing between them, `10.dp` padding from edges, `12.dp` border radius.

---

### 2.4 MarkCard

Each mark card represents one control mark (контрольная марка) left by another participant at the КП.

```
┌─────────────────────────────────────────────┐
│ [seq]  № 14 · команда «Норд»           [🗑] │  ← header row (editable)
├──────────────────────────┬──────────────────┤
│ [cal]  Дата              │ [clk]  Время     │  ← row 1
│        16.05.2026        │        11:42     │
├──────────────────────────┴──────────────────┤  (focused: date dropdown appears here)
│ [bed]  Рест              │        Частей    │  ← row 2
│        02:30             │        3         │
└─────────────────────────────────────────────┘
```

#### Header row

- Background: `Surface`, color `MaterialTheme.colorScheme.surface`
- `Row`, height `38.dp`, padding `9.dp` vertical, `12.dp` horizontal
- Bottom: `HorizontalDivider()` (unless focused — then `2.dp` primary color divider)
- **Sequence number**: `Box(size = 20.dp, shape = CircleShape, background = primaryContainer)`, text `11.sp`
- **Team field**: `BasicTextField`, `fontSize = 14.sp`, `fontWeight = Medium` when filled, `Normal` when empty
  - Placeholder: "Участник…", color `onSurface.copy(alpha = 0.4f)`
  - When focused: text color `primary`, bottom border `2.dp primary`
  - When focused → show team dropdown (see 2.5)
- **Delete button**: `IconButton`, `Icons.Default.Delete`, tint `error` — removes this mark card

#### Field row 1 — Date + Time (combined, `height = 52.dp`)

Split 50/50 by a vertical `HorizontalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))`.

**Date segment** (left half):
- Leading icon: `Icons.Outlined.CalendarMonth`, `14.dp`
- Label: "ДАТА", `10.sp uppercase`
- Value: `fontSize = 14.sp`, `fontVariantNumeric = TabularNums`
- Empty placeholder: "ДД.ММ.ГГГГ", color `onSurface.copy(alpha = 0.4f)`
- When focused: label and icon turn `primary`, bottom edge of segment gets `2.dp primary` underline
- When focused → show date dropdown (see 2.6)

**Time segment** (right half):
- Leading icon: `Icons.Outlined.Schedule`, `14.dp`
- Label: "ВРЕМЯ", `10.sp uppercase`
- Value: `BasicTextField`, `fontSize = 14.sp`, format `HH:mm`
- Empty placeholder: "ЧЧ:ММ"

#### Field row 2 — Rest + Parts (combined, `height = 52.dp`)

**Rest segment** (left, ~65% width):
- Leading icon: `Icons.Outlined.Coffee` (or similar "rest" icon), `14.dp`
- Label: "РЕСТ", `10.sp uppercase`
- Value: `BasicTextField`, `fontSize = 14.sp`, format `HH:MM` (hours:minutes of rest used)
- Empty placeholder: "ЧЧ:ММ"

**Parts segment** (right, ~35% width, `width = 80.dp` fixed):
- No icon
- Label: "ЧАСТЕЙ", `10.sp uppercase`
- Value: `BasicTextField`, `fontSize = 14.sp`, numeric keyboard (`keyboardType = KeyboardType.Number`)
- Empty placeholder: "—"

---

### 2.5 Team Dropdown

A `DropdownMenu` anchored to the **header row**, appearing when the team `BasicTextField` receives focus.

```
┌─────────────────────────────────────────────┐
│  [ № 3 ]  [ № 7 ]  [ № 11 ] [ № 14 ]      │
│  [ № 22 ] [ № 25 ] [ № 31 ] [ № 38 ]      │
└─────────────────────────────────────────────┘
```

- Width: matches the card width exactly (`Modifier.fillMaxWidth()` on the `DropdownMenu`)
- Layout inside: `LazyVerticalGrid(columns = Fixed(4), gap = 6.dp)`, padding `8.dp`
- Each cell: `FilledTonalButton(height = 36.dp, shape = CircleShape)`, text = "№ N", `fontSize = 13.sp`, `fontVariantNumeric = TabularNums`

**Already-used marking:**
- A team number is "used" if it already appears as the header value in another mark card on this screen
- Used buttons: `opacity = 0.45f` (same background and color, just dimmed)
- Used buttons additionally show a `✓` badge: a `Box(size = 14.dp, shape = CircleShape, background = primary)` containing "✓" in `onPrimary`, positioned `offset(-2.dp, -4.dp)` from the button's top-right corner using `Box(contentAlignment = TopEnd)`
- Used buttons **remain tappable** (a participant may appear on multiple marks, e.g. joint marks)

**Manual entry:**
- The team `BasicTextField` in the header accepts free typing for participants not in the predefined list
- The dropdown dismisses on first keypress (if the user types instead of tapping a button)

**Participant list source:**
- The predefined list comes from the race configuration associated with `HitchLog.raceId`
- Until race participant data is available in the data model, hardcode a sample list or load from a new `race.participants` field

**Dismiss:**
- `expanded = teamFieldFocused`
- On item tap: set header text to "№ N", call `focusManager.clearFocus()`
- On focus loss: dismiss

---

### 2.6 Date Dropdown (inside MarkCard)

A `DropdownMenu` anchored to the **date segment**, appearing when the date `BasicTextField` receives focus.

```
┌─────────────────────────────────────────────┐
│  [ 16.05.2026 ]        [ 15.05.2026 ]      │
└─────────────────────────────────────────────┘
```

- Width: matches the **full card width** (not just the date segment)
- Layout: `Row(padding = 6.dp, gap = 6.dp)`
- Left button ("today"): `Button(modifier = Modifier.weight(1f), shape = CircleShape(20.dp))` — primary fill
  - Text: today's date in `dd.MM.yyyy` format
- Right button ("yesterday"): `FilledTonalButton(modifier = Modifier.weight(1f), shape = CircleShape(20.dp))`
  - Text: yesterday's date in `dd.MM.yyyy` format
- Button height: `40.dp`, `fontSize = 15.sp`, `fontVariantNumeric = TabularNums`
- **No labels** — date only, no words like "Сегодня" or "Вчера"

**Behaviour:**
- `expanded = dateFieldFocused`
- Keyboard opens simultaneously (date field is a `BasicTextField`)
- Tapping a button: fills the date field, calls `focusManager.clearFocus()` → dismisses dropdown and keyboard
- Typing in the field: dropdown remains visible until field loses focus or date is complete
- On focus loss: dismiss

**Date is not pre-filled.** Empty by default — no automatic date injection.

---

### 2.7 Add Mark Button

```
[ + ]  Добавить марку
```

- Style: `OutlinedButton` with `border = BorderStroke(1.5.dp, outline)`, `shape = RoundedCornerShape(12.dp)`, `height = 44.dp`, `Modifier.fillMaxWidth()`
- Icon: `Icons.Default.Add`
- Text: "Добавить марку", `fontSize = 14.sp`
- On tap: appends a new empty `MarkCard` to the list and scrolls to it

---

### 2.8 FreeTextCard

```
┌─────────────────────────────────────────────┐
│ ЗАМЕТКА                                     │
│ Например: судья Иванов…                     │
│                                             │
└─────────────────────────────────────────────┘
```

- `Surface`, `shape = RoundedCornerShape(12.dp)`, padding `10.dp` vertical, `12.dp` horizontal
- Label: "ЗАМЕТКА", `10.sp uppercase`, color `onSurfaceVariant`
- `BasicTextField`, `fontSize = 14.sp`, `minHeight = 80.dp`
- Placeholder: "Например: судья Иванов, марки в плохом состоянии…"
- Maps to the existing `HitchLogRecord.text` field until the data model is extended

---

### 2.9 Planned data model extension

When the data model is extended (future task), add to `HitchLogRecord`:

```kotlin
data class CheckpointMark(
    val participantId: String,    // e.g. "№ 14" or "команда «Норд»"
    val date: LocalDate?,
    val time: LocalTime?,         // time on the mark (not the current user's arrival)
    val restUsed: Duration?,      // HH:MM format from the mark
    val restParts: Int?
)

// Add to HitchLogRecord:
val checkpointMarks: List<CheckpointMark> = emptyList()
```

Until this is added: serialize the mark list as JSON in `HitchLogRecord.text`. The `FreeTextCard` note goes into a separate field or is appended after the JSON.

---

## 3. Colours & tokens (for reference)

These map to M3 `ColorScheme` tokens already used in the app:

| Token | Hex | Usage |
|---|---|---|
| `primary` | `#1A3A8F` | Active fields, filled buttons, type circles |
| `onPrimary` | `#FFFFFF` | Text/icons on primary |
| `primaryContainer` | `#D9E2FF` | Sequence number backgrounds |
| `onPrimaryContainer` | `#001258` | Text on primaryContainer |
| `secondary` | `#FFCC00` | LIFT/GET_OFF type circles |
| `onSecondary` | `#1A1A00` | Text on secondary |
| `surfaceVariant` | `#F2F3FA` | TypeStrip background, tonal button fills |
| `onSurfaceVariant` | `#44464F` | Labels, icons |
| `outline` | `#C4C5D0` | Dividers, borders |
| `error` | `#BA1A1A` | Delete button, OFFSIDE/RETIRE type circles |
| `background` | `#F8F9FF` | Screen background |
| `surface` | `#FFFFFF` | Card backgrounds |

---

## 4. Typography (M3 scale used)

| Use | M3 token | Size | Weight |
|---|---|---|---|
| Screen title | `titleLarge` | `20.sp` | `Medium` |
| Type name in chip | `bodyMedium` | `14.sp` | `Normal` |
| Field value (date/time) | `bodyLarge` | `20.sp` | `Normal` |
| Hero time value | `headlineMedium` | `40.sp` | `Medium` |
| Field label (UPPERCASE) | `labelSmall` | `10–11.sp` | `Medium` |
| Note text / placeholder | `bodyLarge` | `18.sp` | `Normal` |
| Save button | `labelLarge` | `14.sp` | `Medium` |
| Shortcut chip | `labelMedium` | `13.sp` | `Medium` |
| Mark card team name | `bodyMedium` | `14.sp` | `Medium` |
| Mark card field value | `bodyMedium` | `14.sp` | `Normal` |
| Mark card field label | `labelSmall` | `10.sp` | `Medium` |

---

## 5. Key implementation notes

1. **`DropdownMenu` width**: Compose's `DropdownMenu` clips to its content by default. To make it full card width, use `Modifier.width(IntrinsicSize.Max)` on the anchor `Box` and apply `Modifier.fillMaxWidth()` inside the `DropdownMenu` content — or measure the card width with `onGloballyPositioned` and pass it to the menu.

2. **Continuous stepping on long-press**: Use `Modifier.pointerInput` with a coroutine loop for the time ±1 buttons. Start at 1/sec, accelerate after 2 seconds.

3. **Date field in MarkCard**: Use `BasicTextField` with `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)`. Parse input as `dd.MM.yyyy`. Show red underline if parse fails.

4. **Focus management**: Use `FocusRequester` and `LocalFocusManager`. On new record: auto-focus the note field via `LaunchedEffect(Unit)`. On team/date dropdown item tap: `focusManager.clearFocus()`.

5. **`animateContentSize()`**: Not needed for the dropdowns (they float). Apply it to the mark card only if any expand/collapse animation is added later.

6. **Scroll + keyboard**: Wrap the `CheckpointInfoScreen` body in `verticalScroll` + `imePadding()` so the keyboard doesn't cover the focused field.

7. **Used teams**: Compute the set of used participant IDs by scanning the current in-memory mark list. Recompute on every recomposition (it's cheap).

8. **REST_OFF hint**: The `RecordViewModel` (or a new `CheckpointViewModel`) needs to load all records for the log and find the most recent `REST_ON`. This can be a `Flow` derived from the record list already loaded on the `HitchLogScreen`.
