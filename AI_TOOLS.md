# AI Tools Disclosure

Per the OpenNet take-home brief, this document lists where and how AI assistance was used while building this project.

## Tool

**Claude Code** (Anthropic's CLI, `claude-opus-4-6`) — used as a pair-programmer through the entire build, driven by the `superpowers:brainstorming` and `superpowers:writing-plans` skills.

## What AI produced

- **Design spec** (`docs/superpowers/specs/2026-04-16-weather-forecast-app-design.md`): dialogue-based brainstorm between the developer and Claude Code. All architectural decisions (Open-Meteo over OpenWeatherMap, sealed `UiState` over Orbit-MVI, Typed DataStore over Preferences, Nav 2 over Nav 3, bundled VDs over Coil, `WeatherCondition` as a sealed interface with `Unknown` variant) were debated; the developer owned the final call in every case.
- **Implementation plan** (`docs/superpowers/plans/2026-04-16-weather-forecast-app.md`): task-by-task plan with TDD steps, file paths, and code snippets.
- **Code generation**: domain models, use cases, repositories, Retrofit services, Hilt modules, ViewModels, Compose screens, and unit tests were drafted by Claude Code following the plan, then reviewed and adjusted by the developer.
- **Vector drawables**: Claude Code produced placeholder VD XMLs; real Meteocons SVGs were converted via Android Studio's Vector Asset Studio by the developer.

## What the developer owned

- All judgement calls (API selection, architectural pattern, library choices).
- Running the app on a physical device / emulator.
- Reviewing every diff before committing.
- Verifying tests actually exercise the intended behavior (not just that they pass).
- Ensuring the final submission runs zero-config on a clean clone.

## What AI did NOT do

- No network calls or external services were invoked by Claude Code beyond reading the PDF assignment and the existing codebase.
- No credentials or secrets were shared with AI tools.
