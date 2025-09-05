## Code Style and Quality Configuration

This project enforces a consistent Java code style with automated formatting and lightweight linting. Below is what was configured, why it was chosen, and what each part does.

### Spotless (google-java-format)
- **What**
  - Maven plugin: `com.diffplug.spotless:spotless-maven-plugin:2.43.0`.
  - Formatter: `google-java-format:1.18.1`.
  - Options: removes unused imports, trims trailing whitespace, ensures final newline.
  - Bound to Maven `verify` phase via `spotless:check`. Auto-fix with `spotless:apply`.
- **Why**
  - Deterministic, automated formatting matching Google’s Java Style; keeps diffs small and consistent across IDEs/OS.
- **What it does**
  - `spotless:check` fails the build if files are not correctly formatted.
  - `spotless:apply` rewrites files to the correct format.

### Checkstyle (Google-leaning rules)
- **What**
  - Maven plugin: `org.apache.maven.plugins:maven-checkstyle-plugin:3.6.0`.
  - Checkstyle engine: `toolVersion` set to `10.17.0`.
  - Config file: `config/checkstyle/checkstyle.xml` (in repo).
  - Suppressions file: `config/checkstyle/suppressions.xml` (optional, present in repo).
  - Runs during `verify` phase with goal `check`.
- **Why**
  - Lints code for import hygiene, basic whitespace, and naming; complements the formatter without duplicating it.
- **What it does**
  - Fails the build on style violations; prints violations to the console.

### Checkstyle configuration file: `config/checkstyle/checkstyle.xml`
- **What**
  - Minimal Google-style baseline that is compatible with the formatter, plus indentation checks so IDEs show violations:
    - Imports: `AvoidStarImport`, `UnusedImports`.
    - Whitespace: `WhitespaceAfter`, `WhitespaceAround`, `MethodParamPad`, `ParenPad`.
    - Naming: `LocalVariableName`, `MemberName`, `ParameterName`, `TypeName`, `MethodName`.
  - Indentation checks are enabled (2-space) to surface formatting issues in the editor.
  - Suppressions supported via `config/checkstyle/suppressions.xml`.
- **Why**
  - Keeps signal-to-noise high and avoids fighting the formatter while still catching common issues.
- **What it does**
  - Ensures import usage and basic naming/spacing standards; build fails if violated.

### EditorConfig: `.editorconfig`
- **What**
  - Root-level file setting: `utf-8`, `lf` line endings, 2-space indentation, trim trailing whitespace, insert final newline. Markdown files skip trimming.
- **Why**
  - Normalizes editor defaults across tools and OS, reducing churn and accidental diffs.
- **What it does**
  - Many editors auto-apply these settings on save, keeping the codebase consistent at the source.

### Continuous Integration (GitHub Actions)
- **What**
  - Workflow: `.github/workflows/style-check.yml`.
  - Triggers on pushes and PRs (branches: `master`, `main`).
  - Sets up Temurin JDK 17 and executes `mvn verify` (which runs both Spotless check and Checkstyle).
- **Why**
  - Enforces style rules in CI; prevents unformatted or noncompliant code from merging.
- **What it does**
  - Fails the workflow if formatting or Checkstyle rules are broken.

### Removed or avoided tools
- Removed overlapping or out-of-scope tools from `pom.xml` to keep setup minimal and non-conflicting:
  - Spotify `fmt-maven-plugin` (duplicate formatter).
  - PMD and SpotBugs (useful, but not requested in this phase).

### Developer workflow
- **Check style locally**
  - Format check and lint:
    ```bash
    mvn verify
    ```
- **Auto-fix formatting locally**
  - Apply google-java-format via Spotless:
    ```bash
    mvn spotless:apply
    ```
- **IDE integration**
  - IntelliJ IDEA:
    - Enable “Reformat code” on save.
    - Install Checkstyle-IDEA plugin; point it to `config/checkstyle/checkstyle.xml`.
    - Optional: install the Google Java Format plugin for on-save/google-accurate formatting. The build remains the source of truth.
  - VS Code:
    - Enable “Format on Save”.
    - Install “Checkstyle for Java” and set configuration to `config/checkstyle/checkstyle.xml`.

### Where to change rules
- Update formatter version/options in `pom.xml` under the Spotless plugin.
- Adjust lint rules in `config/checkstyle/checkstyle.xml`; add exceptions in `config/checkstyle/suppressions.xml`.
- Update IDE behavior via `.editorconfig` for cross-editor defaults.
