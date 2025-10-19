# Building Buddi with Gradle

## Quick Start

```bash
# Navigate to project directory
cd /Users/mariofuksa/workspaces/idea/Buddi

# Build the project (creates JAR)
./gradlew build

# Or just build the JAR (faster)
./gradlew jar

# Run the application
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew run

# Clean build artifacts
./gradlew clean
```

## What You Get

After building, you'll have:

- **Executable JAR**: `build/libs/Buddi-3.4.1.11.jar`
- **Run directly**: `java -jar build/libs/Buddi-3.4.1.11.jar`

## Common Commands

```bash
# List all available tasks
./gradlew tasks

# Show help
./gradlew help

# Print project information
./gradlew printProjectInfo

# Run tests
./gradlew test

# Run ViewModel tests
./gradlew runViewModelTests

# See detailed build info
./gradlew build --info
```

## System Requirements

- **Java**: 8+ (build uses Java 17, runs on 8+)
- **Internet**: Required for first-time Gradle download
- **Disk Space**: ~500MB for Gradle installation and build cache

## File Structure

- `build.gradle` - Main build configuration
- `gradle.properties` - Build settings
- `gradle/wrapper/` - Gradle wrapper (includes auto-download)
- `src/` - Source code
- `lib/` - Dependencies
- `build/` - Build output (generated)

## Windows Users

Use `gradlew.bat` instead of `gradlew`:

```cmd
gradlew.bat build
gradlew.bat jar
gradlew.bat run
```

## For More Details

See `docs/gradle-migration.md` for comprehensive documentation.

## Migration Status

✅ **Complete** - Buddi now builds with Gradle!

- Original `build.xml` still available for reference
- All functionality preserved
- Builds are faster with incremental compilation
- Better IDE integration

Enjoy!
