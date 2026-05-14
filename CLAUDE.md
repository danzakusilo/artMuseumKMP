# Museum Demo — Project Context for Claude

## Purpose
KMP demo app showcasing corporate large-scale developer skills: shared UI and business logic across platforms, strict multi-module Clean Architecture, lean and reusable abstractions.

---

## Target Platforms
| Platform | Target |
|---|---|
| Android | `androidTarget` (minSdk 24) |
| iOS | `iosArm64`, `iosSimulatorArm64` |

---

## Tech Stack
| Library | Purpose |
|---|---|
| Compose Multiplatform | Shared UI across all targets |
| Koin | Dependency injection (no reflection, KMP-native) |
| Ktor Client | HTTP networking |
| kotlinx.serialization | JSON parsing (Ktor plugin) |
| SQLDelight | Local persistence (favorites, user exhibits, artwork cache) |
| Coil 3 | Async image loading (KMP-native) |
| JetBrains Navigation Compose | Multiplatform type-safe navigation |
| kotlinx.coroutines | Async / Flow |

All dependencies managed via `gradle/libs.versions.toml`.

---

## Features

### Museum Artworks Explorer
Core browsing, search, favorites, and exhibits.

### Discover Feed (TikTok-style)
Full-screen vertical pager. Each session picks a random Met department, shuffles its ID pool with a time-seeded `Random`, pages through with an internal cursor. Imageless artworks are skipped with backfill. Heart button to favorite. Lives in `:feature:artworks:ui:feed/`.

---

## Module Graph

```
:composeApp                          (app shell: DI wiring, navigation, entry points)
    ├── :feature:artworks:ui
    ├── :feature:artworks:data       (aggregated here for Koin module registration)
    ├── :feature:homescreen:ui
    ├── :feature:search:ui
    ├── :core:ui
    ├── :core:common
    ├── :core:network
    └── :core:database

:feature:artworks:ui                 (screens, ViewModels, navigation graphs)
    ├── :feature:artworks:domain
    ├── :core:ui
    └── :core:common

:feature:artworks:data               (API service, local data sources, repo impls)
    ├── :feature:artworks:domain
    ├── :core:network
    ├── :core:database
    └── :core:common

:feature:artworks:domain             (entities, repo interfaces, use cases)
    └── :core:common

:feature:homescreen:ui               (home screen, promo cards)
    ├── :core:ui
    └── :core:common

:feature:search:ui                   (search screen, results)
    ├── :feature:artworks:domain
    ├── :core:ui
    └── :core:common

:core:network                        (Ktor HttpClient, profiling plugin)
    └── :core:common

:core:database                       (SQLDelight schema, driver factory, profiling driver)
    └── :core:common

:core:ui                             (theme, shared components)
    └── :core:common

:core:common                         (Result, AppError, dispatchers — no internal deps)
```

**Rules**:
- `:composeApp` wires DI and navigation — it imports feature data modules only for Koin registration, never for direct use.
- Data never imports UI. Domain imports nothing except `:core:common`.
- Feature UI modules may depend on other features' domain layers (e.g., `:feature:search:ui` → `:feature:artworks:domain`) but never on other features' data or UI layers.

---

## Module Details

### `:composeApp`
- Platform entry points: `MainActivity`, `MainViewController`, `main.kt` (desktop), `main.kt` (JS)
- Hosts the `NavHost` and root `App` composable
- Calls `startKoin { }` aggregating all DI modules
- `androidApplication` plugin; all other modules use `androidLibrary` or pure KMP

### `:core:common`
```
commonMain/
  result/
    Result.kt          sealed class Result<out T> { data class Success; data class Error }
    AppError.kt        sealed class AppError (NetworkError, DatabaseError, UnknownError)
  dispatchers/
    AppDispatchers.kt  object exposing io/default/main (Dispatchers.IO/Default/Main)
```

### `:core:network`
```
commonMain/
  HttpClientFactory.kt   creates Ktor HttpClient with ContentNegotiation+Json, logging
  di/
    NetworkModule.kt      Koin module: single<HttpClient>
```

### `:core:database`
```
commonMain/
  DatabaseDriverFactory.kt       expect class, one actual per platform
  di/
    DatabaseModule.kt            Koin module: single<SqlDriver>, single<MuseumDatabase>;
                                 expect fun platformDatabaseModule()
  (androidMain|iosMain)/
    DatabaseDriverFactory.kt     actual class
    di/DatabaseModule.<plat>.kt  actual platformDatabaseModule { single { DatabaseDriverFactory(...) } }
sqldelight/
  dev/danya/museum/core/database/
    Artwork.sq            cached artwork rows + isFavorite flag, upsert/select/setFavorite/deleteIfOrphan
    Exhibit.sq            user-created exhibits, insert/selectAll/selectAllWithCount/rename/delete
    ExhibitArtwork.sq     join table with ON DELETE CASCADE on both sides;
                          removeAllForArtwork (used by unfavorite cascade);
                          exhibitsContainingArtwork (reactive query for detail screen)
```

### `:core:ui`
```
commonMain/
  theme/
    MuseumTheme.kt               MaterialTheme wrapper, picks light/dark, provides extended colors
    Color.kt                     MuseumPalette (raw brand constants — escape-hatch only)
                                 + light/dark M3 ColorScheme
    MuseumExtendedColors.kt      app-specific semantic colors (favorite, exhibit, tag) +
                                 MaterialTheme.extendedColors accessor
    Typography.kt
  component/
    LoadingView.kt
    ErrorView.kt
    ArtworkCard.kt               reusable card (image via Coil, title, date)
  composeResources/values/
    strings.xml                  department_* string resources (all 18 Met departments)
```

### `:feature:artworks:domain`
```
commonMain/
  entity/
    Artwork.kt            data class (id: Int, title, primaryImageUrl?, artistName?,
                          objectDate?, culture?, period?, dynasty?, medium?, dimensions?,
                          department, classification?, repository?)
    ArtworkSummary.kt     lightweight list item (id, title, primaryImageUrl?, artistName?, objectDate?)
    Department.kt         enum class Department(id: Int, resourceKey: String) — all 18 Met departments;
                          companion: fromId(Int)
    Exhibit.kt            data class (id: Long, name, createdAt, artworkCount)
  repository/
    ArtworkRepository.kt  interface (suspend + Flow only, no platform types);
                          includes getArtworkFeedPage(limit) — cursor-based, no offset;
                          isFavorite(artworkId) — single-shot check
    ExhibitRepository.kt  interface (CRUD + add/remove artwork);
                          getExhibitIdsForArtwork(artworkId) — reactive Flow<Set<Long>>
  usecase/
    SearchArtworksUseCase.kt
    GetArtworkDetailUseCase.kt
    GetArtworkFeedUseCase.kt   wraps getArtworkFeedPage; default page size 10
    GetFavoritesUseCase.kt
    IsFavoriteUseCase.kt       single-shot favorite check (used by detail VM on load)
    ToggleFavoriteUseCase.kt
    GetExhibitsUseCase.kt
    GetExhibitIdsForArtworkUseCase.kt   reactive set of exhibit IDs containing an artwork
    CreateExhibitUseCase.kt
    AddArtworkToExhibitUseCase.kt
    RemoveArtworkFromExhibitUseCase.kt
```

### `:feature:artworks:data`
```
commonMain/
  remote/
    dto/
      ArtworkDetailDto.kt    @Serializable, maps from API JSON
      SearchResultDto.kt
    ArtworkApiService.kt     Ktor calls; key methods:
                              search(query), getObjectSingle(id),
                              fetchArtworkDetails(ids) — parallel fan-out, 5 concurrent, per-item resilient,
                              fetchDepartmentObjectIds(departmentId),
                              searchAndFetch(query), fetchRecentArtworks(metadataDate)
  local/
    ArtworkLocalDataSource.kt   SQLDelight-backed artwork cache + favorites
    ExhibitLocalDataSource.kt   SQLDelight-backed exhibits + join-table CRUD;
                                getExhibitIdsForArtwork (reactive), removeAllForArtwork (cascade)
  mapper/
    ArtworkMapper.kt          DTO → domain entity (toDomain, toSummary for both DTO and DB entity)
  repository/
    ArtworkRepositoryImpl.kt  implements domain repository;
                              feed: holds feedIdPool + feedCursor, picks random Department,
                              shuffles with session seed, skips imageless, pre-upserts to local DB;
                              upsertDto() shared helper for detail + feed paths;
                              toggleFavorite: unfavoriting cascades to removeAllForArtwork
                              (via injected ExhibitLocalDataSource) then deleteIfOrphan
  di/
    ArtworksDataModule.kt     Koin module
```

### `:feature:artworks:ui`
```
commonMain/
  feed/
    SwipeFeedScreen.kt       VerticalPager, full-screen artwork pages, gradient overlay,
                              heart FAB; Coil AsyncImage with ContentScale.Crop
    SwipeFeedViewModel.kt    cursor-based paging, prefetch threshold = 3,
                              in-memory favorites set, retry support
    SwipeFeedState.kt        sealed: Loading, Content(artworks, favorites, isLoadingMore), Error
  list/
    ArtworkListScreen.kt
    ArtworkListViewModel.kt
    ArtworkListState.kt      sealed UI state
  detail/
    ArtworkDetailScreen.kt   two FABs in Column: exhibit (add/manage) + favorite (heart);
                              bottom sheet for exhibit toggle-checkmarks
    ArtworkDetailViewModel.kt  loads initial isFavorite; observes artworkExhibitIds;
                               onToggleExhibit add/remove; unfavorite cascades via data layer
    ArtworkDetailState.kt     Content includes artworkExhibitIds: Set<Long>
  search/
    SearchScreen.kt
    SearchViewModel.kt
    SearchState.kt
  exhibitions/
    ExhibitDetailScreen.kt   inline rename: pencil icon → edit mode (OutlinedTextField +
                              FocusRequester + confirm/cancel); manages displayName locally
    ExhibitDetailViewModel.kt  observes artworks + onRename
    ExhibitionsTab.kt        list of exhibits with previews, create/rename/delete
  favorites/
    FavoritesScreen.kt
    FavoritesViewModel.kt
  component/
    ExhibitBottomSheet.kt    reusable sheet: lists all exhibits with Checkbox per row;
                              checked = artwork is in exhibit; tap row to toggle add/remove;
                              also supports bulk-add mode (pass empty artworkExhibitIds)
  nav/
    FeedRoute.kt             @Serializable data object — bottom nav "Discover" tab
    FeedNavGraph.kt           NavGraphBuilder.feedGraph()
    ArtworksNavGraph.kt      nested NavGraph with typed routes
  di/
    ArtworksPresentationModule.kt   Koin module (viewModel { })
```

---

## Architecture Conventions

### Result type
All repository and use-case public APIs return either:
- `suspend fun → Result<T>` for single-shot operations
- `Flow<Result<T>>` for reactive streams

Never throw across layer boundaries. Map exceptions to `AppError` in the data layer.

### Use Cases
- One public `operator fun invoke(...)` entry point
- Thin: validate input → call repository → map result
- No UI logic, no platform types

### ViewModels
- Extend `ViewModel` from `lifecycle-viewmodel`
- Expose a single `StateFlow<State>` (sealed class) per screen
- One ViewModel per screen; no shared ViewModels across screens
- Inject use cases via constructor (Koin `viewModel { }`)

### Koin
- Module defined in the layer that owns the binding
  - `ArtworksDataModule` binds `ArtworkRepository` → `ArtworkRepositoryImpl`
  - `ArtworksPresentationModule` registers ViewModels
  - `NetworkModule`, `DatabaseModule` in respective `:core:*` modules
- `:composeApp` aggregates all modules: `startKoin { modules(coreModules + featureModules) }`
- Use `koinViewModel()` in Composables

### Navigation
- JetBrains Navigation Compose (multiplatform)
- Typed routes via `@Serializable` data objects/classes
- Each feature owns its `NavGraph` extension; `:composeApp` composes them

### Theme
- Pick colors via `MaterialTheme.colorScheme.*` (M3 roles) or `MaterialTheme.extendedColors.*` (favorite, exhibit, tag).
- `MuseumPalette` constants are escape-hatch only — use them in logos / splash / illustrations where theming is intentionally bypassed. If you keep wanting one in a screen, add a slot to `MuseumExtendedColors` instead.
- `error` is generic Material red. Terracotta belongs to `favorite` only.
- No Material You dynamic color — brand identity overrides system tinting.

### SQLDelight
- Scope: user-curated cache — favorites, user-created exhibits, and the artwork rows referenced by either. Not a full network response cache.
- One database: `MuseumDatabase`. Schema split across `Artwork.sq` / `Exhibit.sq` / `ExhibitArtwork.sq` (one file per aggregate).
- `packageName = "dev.danya.museum.core.database"` set in `core/database/build.gradle.kts`; schema path mirrors it.
- Driver created via `expect class DatabaseDriverFactory` with platform `actual` implementations.
- Enable `PRAGMA foreign_keys = ON` in the driver setup so `ON DELETE CASCADE` on the join table actually fires.
- Cache hygiene: after un-favoriting or removing from an exhibit, call `Artwork.deleteIfOrphan` to drop rows no longer referenced.
- Unfavorite cascade: un-favoriting removes the artwork from **all** exhibits first (`ExhibitArtwork.removeAllForArtwork`), then clears the flag, then `deleteIfOrphan`. Removing from an exhibit does **not** affect favorite status.
- Reactive queries: when a Flow must react to changes in a joined table, the base SQL query must reference that table (e.g. `selectAllWithCount` joins `ExhibitArtwork` so the exhibits list updates when artworks are added/removed). A plain `selectAll` on `Exhibit` alone would miss `ExhibitArtwork` changes.

### No-go rules
- Domain must not depend on Ktor, SQLDelight, Coil, or any platform API
- Data must not depend on Compose or UI types
- No `lateinit` backing fields on ViewModel state — use `MutableStateFlow`
- No `GlobalScope` — always use `viewModelScope` or injected scope

---

## Planned: Discovery Features

### FeedSource — Parameterized Feed

All discovery features route through the existing feed screen, parameterized by source.

```kotlin
// :feature:artworks:domain:entity
sealed class FeedSource {
    data object Discover : FeedSource()           // random dept, current behavior
    data class ByDepartment(val department: Department) : FeedSource()
    data class ByArtist(val artistName: String) : FeedSource()
}
```

**Repository change**: widen `getArtworkFeedPage(limit)` → `getArtworkFeedPage(source: FeedSource, limit: Int)`. Internally, store `activeSource` alongside `feedIdPool`/`feedCursor`; reset pool+cursor when source changes.

**ID pool strategy per source**:
| Source | Pool init | Notes |
|---|---|---|
| `Discover` | `fetchDepartmentObjectIds(randomDept)` | Current behavior |
| `ByDepartment` | `fetchDepartmentObjectIds(dept.id)` | Reuses existing API method |
| `ByArtist` | `search(name, artistOrCulture=true, hasImages=true)` | New API method: `searchByArtist` |

**Nav route**: `FeedRoute` gains flat string params (`sourceType`, `sourceValue?`). VM maps back to `FeedSource` on init. Each filtered feed is a new back-stack entry — navigating, not reloading.

### Save Department

**SQLDelight** — `SavedDepartment.sq` in `:core:database`:
```sql
CREATE TABLE SavedDepartment (departmentId INTEGER NOT NULL PRIMARY KEY);
insert / delete / selectAll
```

**Layer stack**: `SavedDepartmentLocalDataSource` → `DepartmentRepository` (interface in domain, impl in data) → `SaveDepartmentUseCase` + `GetSavedDepartmentsUseCase`.

**UI**: bookmark toggle on department-filtered feed. Saved departments surface as a `LazyRow` of chips on the home screen, each navigating to `FeedRoute("department", id)`.

### Artist search note

Met API `artistOrCulture=true` does fuzzy matching — "Rembrandt" returns Rembrandt + "School of Rembrandt" + culture hits. For v1 this is a feature (more feed content). If noisy, filter client-side by exact `artistDisplayName` match on fetched DTOs.

### Implementation order

1. `FeedSource` sealed class + widen repo interface (domain)
2. `searchByArtist` in API service (data)
3. Generalize `ArtworkRepositoryImpl` pool logic with `activeSource` tracking (data)
4. `SavedDepartment.sq` + local data source + `DepartmentRepository` (data/database)
5. Update `FeedRoute` to accept source params, update VM to parse (UI)
6. Add department/artist chips to feed page overlay (UI)
7. Add save-department toggle to feed header (UI)
8. Add saved-departments row to home screen (UI)

Steps 1–3 land together (no breaking change, `Discover` is default). Step 4 + 7–8 are the "save department" vertical. Steps 5–6 are navigation wiring.

---

## Build Notes
- Version catalog: `gradle/libs.versions.toml` — all deps declared here, no inline versions
- `TYPESAFE_PROJECT_ACCESSORS` enabled — use `projects.core.common` syntax in `build.gradle.kts`
- All library modules: `kotlinMultiplatform` + `androidLibrary` plugins
- Configuration cache: enabled (`org.gradle.configuration-cache=true`)

---

## Implementation Order
1. Scaffold all modules (empty `build.gradle.kts` + package stubs) and wire `settings.gradle.kts`
2. `:core:common` — Result, AppError, dispatchers
3. `:core:network` — HttpClient factory + Koin module
4. `:core:database` — SQLDelight setup, driver factories, schema
5. `:core:ui` — theme + shared components
6. `:feature:artworks:domain` — entities, repository interface, use cases
7. `:feature:artworks:data` — DTOs, API service, local source, repo impl, DI
8. `:feature:artworks:ui` — ViewModels, states, screens, navigation, DI
9. `:composeApp` — wire navigation, aggregate Koin, clean up entry points