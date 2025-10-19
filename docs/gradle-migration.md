# Ant to Gradle Migration Guide

## Overview

This document describes the migration of the Buddi project from Apache Ant to Gradle build automation.

## Why Gradle?

**Advantages of Gradle over Ant:**

1. **Declarative Configuration**: Gradle uses a DSL (Domain Specific Language) based on Groovy, making builds more readable and maintainable
2. **Dependency Management**: Built-in support for dependency resolution vs. manual JAR management in Ant
3. **Incremental Builds**: Gradle caches build outputs and only rebuilds what changed
4. **Gradle Wrapper**: Ensures all developers use the same Gradle version without manual installation
5. **Plugin Ecosystem**: Rich collection of plugins for Java, packaging, and distribution tasks
6. **IDE Integration**: Better integration with IntelliJ IDEA, Eclipse, and VS Code
7. **Parallel Execution**: Supports parallel task execution for faster builds
8. **Better Error Messages**: More descriptive error reporting and diagnostics

## File Structure

### New Gradle Configuration Files

```
Buddi/
├── build.gradle          # Main build configuration
├── settings.gradle       # Project settings and plugin management
├── gradlew              # Gradle wrapper (Unix/Linux/Mac)
├── gradlew.bat          # Gradle wrapper (Windows) - generated automatically
└── gradle/
    └── wrapper/
        ├── gradle-wrapper.jar         # Wrapper JAR (generated)
        ├── gradle-wrapper.properties  # Wrapper configuration
        └── gradle-wrapper.jar         # Wrapper bootstrap
```

### Gradle Directory Structure

```
Buddi/
├── src/                      # Main source code (maps to sourceSets.main.java)
│   └── org/homeunix/thecave/buddi/
├── junit/                    # Test source code (maps to sourceSets.test.java)
│   └── org/homeunix/thecave/buddi/test/
├── build/                    # Generated build artifacts (ignored by Gradle tasks)
│   ├── classes/             # Compiled classes
│   ├── libs/                # Output JAR files
│   ├── reports/             # Test reports
│   └── tmp/                 # Temporary build files
└── gradle/wrapper/          # Gradle wrapper files
```

## Dependency Management

### Ant Approach (Old)
- Manual JAR files in `lib/` directory
- All JARs had to be downloaded and committed to version control
- No automatic transitive dependency resolution
- Classpath manually specified in `build.xml`

### Gradle Approach (New)

**Dependencies are defined in `build.gradle`:**

```gradle
dependencies {
    // Swing-related dependencies
    implementation name: 'swingx-core-1.6.2'
    implementation name: 'jfreechart-1.0.13'
    implementation name: 'jcommon-1.0.17'
    
    // MOSS framework dependencies
    implementation name: 'moss-application-2.1.0.0'
    // ... more dependencies
}
```

**Configuration types:**
- `implementation` - Compile-time and runtime dependency
- `runtimeOnly` - Only needed at runtime
- `compileOnly` - Only needed at compile time
- `testImplementation` - Test-only dependency

## Building the Project

### Using Gradle Wrapper (Recommended)

The Gradle wrapper (`gradlew` / `gradlew.bat`) automatically downloads and uses the specified Gradle version.

**macOS/Linux:**
```bash
./gradlew build          # Full build
./gradlew jar            # Build JAR only
./gradlew run            # Run the application
./gradlew clean          # Clean build artifacts
```

**Windows:**
```cmd
gradlew.bat build
gradlew.bat jar
gradlew.bat run
gradlew.bat clean
```

### Using Gradle Directly (if installed)

If you have Gradle installed system-wide, you can use `gradle` instead of `./gradlew`:

```bash
gradle build
gradle jar
gradle run
gradle clean
```

### Build Tasks

| Task | Description |
|------|-------------|
| `build` | Compiles, runs tests, and packages the project |
| `clean` | Removes all build artifacts |
| `compile` | Compiles Java source code only |
| `jar` | Creates executable JAR file (includes all dependencies) |
| `run` | Compiles and runs the application |
| `test` | Runs all test classes from `junit/` directory |
| `runViewModelTests` | Runs ViewModel-specific tests |
| `classes` | Compiles main source code |
| `testClasses` | Compiles test source code |

### Platform-Specific Packaging Tasks

For packaging to platform-specific installers, use:

```bash
./gradlew packageWindows      # Windows (requires Launch4J)
./gradlew packageOSX          # macOS (requires JarBundler)
./gradlew packageLinuxDebian  # Debian/Ubuntu (requires jdeb)
./gradlew packageLinuxGeneric # Generic Linux package
```

**Note:** These tasks currently display information about required external tools. Full integration would require Gradle plugins for each platform.

## Configuration Details

### Java Version

The project is configured for Java 1.8 (Java 8) source and target:

```gradle
sourceCompatibility = '1.8'
targetCompatibility = '1.8'
```

To upgrade to a newer Java version, modify these values:

```gradle
sourceCompatibility = '11'  // or '17', '21', etc.
targetCompatibility = '11'
```

### Application Configuration

The `application` plugin provides:

```gradle
application {
    mainClass = 'org.homeunix.thecave.buddi.Buddi'
    applicationName = 'Buddi'
}
```

This configures:
- The main entry point for `gradle run`
- Script generation for distribution
- Default startup scripts

### Source Sets

Custom source set configuration maps directories:

```gradle
sourceSets {
    main {
        java {
            srcDirs = ['src']
        }
    }
    test {
        java {
            srcDirs = ['junit']
        }
    }
}
```

### JAR Configuration

The custom `jar` task:

1. **Includes Dependencies**: All runtime classpath JARs are unpacked and re-packed into the final JAR
2. **Adds Manifest**: Sets `Main-Class`, version, timestamp
3. **Includes Resources**:
   - Language files from `etc/Languages/`
   - CSS files from `etc/css/`
   - Images from `img/` (PNG, JPG, GIF)
   - Documentation from `docs/`
   - Version file

**Result**: A single, fat executable JAR containing all dependencies

## Migration from Ant

### Command Mapping

| Ant Command | Gradle Equivalent | Purpose |
|-------------|------------------|---------|
| `ant compile` | `./gradlew classes` or `./gradlew compileJava` | Compile source code |
| `ant jar` | `./gradlew jar` | Build JAR |
| `ant clean` | `./gradlew clean` | Clean build artifacts |
| `ant run` | `./gradlew run` | Run application |
| `ant windows` | `./gradlew packageWindows` | Windows packaging |
| `ant osx` | `./gradlew packageOSX` | OSX packaging |
| `ant debian` | `./gradlew packageLinuxDebian` | Debian packaging |

### Ant Properties → Gradle Equivalents

**Ant `build.xml` properties:**
```xml
<property name="FULL_NAME" value="Buddi"/>
<property name="VERSION" value="3.4.1.11"/>
<property name="MAIN_CLASS" value="org.homeunix.thecave.buddi.Buddi"/>
```

**Gradle equivalents in `build.gradle`:**
```gradle
group = 'org.homeunix.thecave'
version = projectVersion  // Read from version.txt
application { mainClass = 'org.homeunix.thecave.buddi.Buddi' }
```

## Gradle Concepts

### Tasks

A task is a unit of work that Gradle performs. Examples:
- `compileJava` - Compiles Java code
- `jar` - Creates a JAR file
- `run` - Executes the application
- `clean` - Removes build artifacts

**Listing all tasks:**
```bash
./gradlew tasks
```

**Listing tasks with descriptions:**
```bash
./gradlew tasks --all
```

### Plugins

Plugins extend Gradle's functionality. This project uses:

- **`java`** - Java compilation, testing, and packaging
- **`application`** - Application distribution and execution

### Properties

Gradle can read and use properties in various ways:

```gradle
// From version.txt file
def properties = new Properties()
properties.load(new FileInputStream('version.txt'))
String projectVersion = properties.getProperty('VERSION')
version = projectVersion

// System properties
systemProperties = ['os.name': 'Linux']

// Project properties
project.version = '3.4.1.11'
```

### Configurations

Configurations define how dependencies are used:

- `implementation` - Used by main code and tests
- `runtimeOnly` - Only at runtime
- `compileOnly` - Only at compile time
- `testImplementation` - Only for tests

## Gradle Wrapper

The Gradle Wrapper (`gradlew` / `gradlew.bat` scripts and `gradle/wrapper/` directory) ensures:

1. **Consistency**: All developers use the same Gradle version
2. **Convenience**: No need to download/install Gradle separately
3. **Reproducibility**: Builds are identical regardless of system Gradle installation

**The wrapper is configured via:**
```properties
# gradle/wrapper/gradle-wrapper.properties
distributionUrl=https://services.gradle.org/distributions/gradle-8.5-bin.zip
```

To update the Gradle version used by the wrapper, run:
```bash
./gradlew wrapper --gradle-version 8.10
```

## Troubleshooting

### Build Fails to Find Classes

**Problem**: `error: cannot find symbol`

**Solution**: Ensure all dependencies are correctly declared in `build.gradle`. Rebuild with:
```bash
./gradlew clean build
```

### Quaqua Library Issues

**Problem**: `IllegalAccessError` with Quaqua on Java 23+

**Solution**: The `build.gradle` already includes VM options to handle this:
```gradle
run {
    systemProperties = ['os.name': 'Linux']
    jvmArgs = ['--lnf', 'none']
}
```

### Memory Issues During Build

**Problem**: Out of memory during compilation

**Solution**: Increase Gradle heap in `gradle/wrapper/gradle-wrapper.properties` or environment:
```bash
export GRADLE_OPTS=-Xmx2g
./gradlew build
```

### Wrapper Certificate Issues

**Problem**: `javax.net.ssl.SSLHandshakeException` downloading wrapper

**Solution**: Update certificate store or use HTTP:
```bash
./gradlew wrapper --gradle-version 8.5 --gradle-distribution-url http://...
```

## Project Information

**Print project information:**
```bash
./gradlew printProjectInfo
```

Output:
```
========================================
Buddi Build Information
========================================
Project Name: Buddi
Version: 3.4.1.11
Group: org.homeunix.thecave
Source Compatibility: 1.8
Target Compatibility: 1.8
Main Class: org.homeunix.thecave.buddi.Buddi
...
========================================
```

## Next Steps

### Optional Future Enhancements

1. **Maven Central Publishing**: Configure Gradle to publish artifacts to Maven Central
2. **Automated Testing**: Integrate JUnit 5 and add more comprehensive tests
3. **Platform Plugins**: Add `gradle-osxpackager`, `gradle-nsis`, `gradle-deb` plugins for automated packaging
4. **CI/CD Integration**: Configure GitHub Actions, Jenkins, or GitLab CI to automatically build
5. **Dependency Management**: Move from flat JAR directory to Maven Central or JCenter for dependency resolution
6. **Code Quality**: Add `jacoco` (code coverage), `sonarqube` (static analysis)
7. **Documentation**: Add `asciidoctor` or `sphinx` plugins for automated documentation generation

### Keeping `build.xml`

The original `build.xml` is still in the repository for reference. If you need to use Ant again, the setup remains unchanged. However, Gradle should be the primary build tool going forward.

## Summary

Buddi has been successfully migrated from Ant to Gradle. Key benefits realized:

✅ **Cleaner build configuration** - Gradle DSL is more readable than XML  
✅ **Better dependency management** - Centralized, versioned dependencies  
✅ **Faster builds** - Incremental compilation and caching  
✅ **Easier IDE integration** - Better support in modern IDEs  
✅ **Wrapper included** - Consistent builds across all environments  
✅ **Extensible** - Easy to add new tasks and plugins  

**Start building:**
```bash
cd /Users/mariofuksa/workspaces/idea/Buddi
./gradlew build      # Build the project
./gradlew run        # Run the application
```
