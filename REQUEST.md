## Constraints (Do NOT violate)

- Do NOT modify the build flavor structure — the `playstore`, `fdroid`, and `universal` flavors, their source sets, and their BuildConfig flags must stay as-is.
- Do NOT add new third-party library dependencies — fix issues using existing dependencies and standard Android/Kotlin APIs only.
- Do NOT copy code from external open-source projects for core logic fixes.
- Preserve all existing comments and docstrings unrelated to changed code.
- All user-facing text must go in `res/values/strings.xml` — never hardcode English strings.