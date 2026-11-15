# Local setup and verification

## Required and optional tools

| Tool | Purpose | Required? |
| --- | --- | --- |
| JDK 11 | Compile/run original Java 11 project | Yes for reproduction |
| Git | Clone, review and publish | Yes for Git workflow |
| Docker Desktop + Compose | Run configured MariaDB 10.5 | Recommended; matching local MariaDB is an alternative |
| IntelliJ IDEA | Read/debug Gradle project | Optional |
| Node.js + npm | Existing Prettier Java formatting | Optional; not needed to run Java |
| Scene Builder | Visual FXML editing | Optional |
| DBeaver | Inspect database | Optional |

Use JDK 11 as the initial reproduction baseline. The original report mentions Java 17, but the checked-in build and development guide specify Java 11. Resolve the discrepancy through an actual run before advertising support for other versions. Do not upgrade the framework or Gradle wrapper during initial reproduction.

Set `JAVA_HOME` to the JDK directory and ensure its `bin` directory is on PATH. Restart the terminal and confirm `java -version`. In IntelliJ, open `xtasks/build.gradle`, select JDK 11 for both Project SDK and Gradle JVM, and reload Gradle.

## Dependency synchronization

The checked-in wrapper downloads Gradle 7.6, then resolves libraries from the configured repositories. First use needs internet access. Run `.\gradlew.bat --version` and `.\gradlew.bat compileJava` from `xtasks`. JavaFX dependencies are already declared in the Gradle plugin. Do not install Maven or a second global Gradle just for this project.

If formatting is needed and Node/npm are installed, run `npm ci`, then `npm run ci-prettier-check` from `xtasks`. `npm run autoformat` rewrites Java files: review its changes separately. Existing formatter versions are historical, so compatibility with your installed Node version still needs checking.

## Database and application

Follow the root README. Database creation uses `MYSQL_*` values in `infrastructure/database/.env`; the Java app uses the datasource in `src/main/resources/application.yml`. Both must match. Port 3306 must be free. Docker runs the database, while JavaFX runs on your desktop.

If credentials are changed after first startup, an existing named database volume may retain the previous account configuration. Inspect the database before changing accounts; do not delete its volume casually.

Stop the database from the repository root with `docker compose down`. This retains the named volume. Do not add `-v` unless you intend to erase local database data.

## Tests

From `xtasks`:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test --tests 'xtasks.backend.services.*'
.\gradlew.bat test
.\gradlew.bat coverageReport
```

See [verification](VERIFICATION.md) for recorded results: backend tests pass, while the full suite retains four original GUI failures. Tests use an H2 configuration in `src/test/resources/application.yml`. Backend tests should be attempted before the full suite; TestFX/JavaFX UI tests need a graphical environment. H2 does not prove identical MariaDB behavior.

## Demonstration data

`doc/grocery_purchases_1_until_september-26_with_categories.csv` contains historical sample data. Import expects semicolon separators and headers `name;date;quantity;unit;category`, with dates in `yyyy-MM-dd` format. Historical dates can make many replenishment suggestions overdue today. Do not alter the original dataset just to claim current-date results.

## Official references

- [Gradle Java compatibility](https://docs.gradle.org/current/userguide/compatibility.html)
- [Docker Desktop / Compose installation](https://docs.docker.com/compose/install/)
- [Docker Desktop on Windows](https://docs.docker.com/desktop/setup/install/windows-install/)
