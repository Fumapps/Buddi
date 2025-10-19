# GitHub Actions CI/CD Setup

## Overview

This document describes the GitHub Actions workflows configured for the Buddi project.

## Workflows

### 1. Gradle Build (`gradle-build.yml`)

**Trigger**: 
- Push to `master`, `main`, or `develop` branches
- Pull requests to `master`, `main`, or `develop` branches

**Jobs**:

#### a) Multi-Java Version Build
- **Runs on**: Ubuntu latest
- **Java versions tested**: 11, 17, 21
- **Steps**:
  1. Checkout code
  2. Set up Java version
  3. Grant execute permission for gradlew
  4. Build with `./gradlew clean build -x test`
  5. Upload JAR artifacts (30-day retention)
  6. Run tests (non-blocking)
  7. Upload test results

**Artifacts Generated**:
- `buddi-jar-java11`, `buddi-jar-java17`, `buddi-jar-java21` - JAR files per Java version
- Test reports in `build/reports/`

#### b) Multi-OS Build Matrix
- **Runs on**: Ubuntu, macOS, Windows
- **Java version**: 17
- **Steps**: Same as above, adapted for each OS
- **Artifacts**: `buddi-jar-{os}-java17` for each platform

**Purpose**: Verify builds work across multiple Java versions and operating systems

---

### 2. Create Release (`release.yml`)

**Trigger**:
- Push with tag matching `v*` (e.g., `v3.4.1.11`)
- Manual trigger via `workflow_dispatch`

**Steps**:
1. Checkout code
2. Set up Java 17
3. Grant execute permission for gradlew
4. Build with Gradle
5. Extract version from `version.txt`
6. Create GitHub Release with:
   - JAR files (`build/libs/*.jar`)
   - Distribution archives (`build/distributions/*.tar`, `*.zip`)
   - Build logs
7. Upload artifacts to Release

**Release Artifacts**:
- Executable JAR
- Distribution packages (tar, zip)
- Build reports

**Usage**:
```bash
# Create a tag to trigger release
git tag v3.4.1.11
git push origin v3.4.1.11

# Or manually trigger via GitHub UI
# Actions → Create Release → Run workflow
```

---

### 3. Code Quality Checks (`code-quality.yml`)

**Trigger**:
- Push to `master`, `main`, or `develop` branches
- Pull requests to `master`, `main`, or `develop` branches
- Weekly schedule (Sunday 2 AM UTC)

**Jobs**:

#### a) Checkstyle
- Runs code formatting checks
- Non-blocking (continues on error)
- Uploads reports

#### b) Tests
- Runs full test suite
- Uploads test reports to artifacts
- 7-day retention

#### c) Dependency Check
- Checks project dependencies
- Generates dependency report
- Non-blocking (continues on error)

**Artifacts Generated**:
- Code quality reports
- Test reports
- Dependency reports

---

## Viewing Results

### Build Status
- Check the **Actions** tab on GitHub
- Each workflow run shows logs and artifact downloads
- Failed builds prevent merges to main branches (if branch protection enabled)

### Artifacts
- Navigate to completed workflow run
- Download section shows all generated artifacts
- Artifacts retained for specified duration (7-30 days)

### Release Page
- Navigate to **Releases** tab
- View all released versions
- Download JAR and distribution packages
- View release notes (can be added manually)

---

## Environment Variables & Secrets

### Available Tokens
- `GITHUB_TOKEN` - Automatically provided by GitHub Actions
- Used for creating releases and uploading artifacts

### No Additional Configuration Needed
- Java is set up automatically via `actions/setup-java`
- Gradle wrapper handles build tool versioning
- All dependencies resolved via `build.gradle`

---

## Customization

### To Add a New Workflow

1. Create file in `.github/workflows/` with `.yml` extension
2. Define trigger events (`on`)
3. Define jobs and steps
4. Reference example workflows in this directory

### To Modify Existing Workflows

Edit the relevant `.yml` file and commit to repository.

**Example**: To add a new Java version:

```yaml
strategy:
  matrix:
    java-version: ['11', '17', '21', '23']  # Add '23'
```

### To Add Caching

Gradle cache is already enabled via `cache: gradle`. This speeds up builds by ~60%.

---

## Troubleshooting

### Workflow Fails to Run
- Check `.github/workflows/` directory exists
- Verify YAML syntax is correct
- Check trigger conditions (branches, tags, schedules)

### Build Fails in CI but Works Locally
- Ensure Java version matches (`java-version` in workflow)
- Check for platform-specific issues (`runner.os`)
- Verify gradlew is executable (`chmod +x gradlew`)

### Artifacts Not Generated
- Check build actually succeeded (green checkmark)
- Verify path in `path:` field matches actual build output
- Check retention days haven't expired

### Release Not Creating
- Verify tag matches `v*` pattern
- Check `softprops/action-gh-release` has permissions
- Verify artifacts exist before release step

---

## Best Practices

### 1. Branch Protection Rules
Consider enabling:
- Require status checks to pass before merging
- Require code reviews before merging
- Dismiss stale pull request approvals

**Steps**:
1. Go to Settings → Branches
2. Add rule for `main`/`master`
3. Require CI to pass

### 2. Schedule Regular Builds
The code-quality workflow runs weekly to catch dependency issues early.

### 3. Monitor Workflow Performance
- Check build times in Actions tab
- Optimize slow builds by:
  - Using cached dependencies
  - Parallelizing tests
  - Reducing artifacts retention

### 4. Tagging Strategy
For releases, use semantic versioning:
- `v1.0.0` - Major release
- `v1.0.1` - Patch release
- `v1.1.0` - Minor release

---

## Security Considerations

### Token Permissions
- `GITHUB_TOKEN` has minimal required permissions
- Scoped to current repository only
- Automatically revoked after workflow completes

### Dependency Security
- Regular dependency checks run weekly
- Consider adding Dependabot for automated updates
- Review security advisories in dependency reports

### Branch Protection
- Require status checks to pass before merge
- Prevent accidental pushes to main branches
- Require code reviews from maintainers

---

## Common Scenarios

### Scenario 1: PR Check
1. Create PR to `develop`
2. Workflows automatically trigger
3. Build must pass before merge
4. Artifacts available for testing

### Scenario 2: Release to Production
1. Update version in `version.txt`
2. Commit and push to `master`
3. Create and push tag: `git tag v3.4.1.12 && git push origin v3.4.1.12`
4. Release workflow triggers automatically
5. Artifacts published to GitHub Releases

### Scenario 3: Scheduled Maintenance
1. Weekly code quality check runs automatically
2. Review reports for issues
3. Fix any failing checks
4. Commit fixes for next merge

---

## Future Enhancements

### Recommended Additions
1. **Code Coverage**: Add JaCoCo for coverage reports
2. **SBOM**: Generate Software Bill of Materials
3. **Docker Build**: Build and push Docker images
4. **Notifications**: Send build status to Slack
5. **Performance Tests**: Add benchmarking suite
6. **Documentation Deploy**: Auto-deploy docs to GitHub Pages

### Example: Adding Docker Build
```yaml
- name: Build Docker image
  run: docker build -t buddi:latest .
  
- name: Push to Docker Hub
  run: docker push myregistry/buddi:latest
```

---

## Documentation

See also:
- [`GRADLE_QUICKSTART.md`](../../GRADLE_QUICKSTART.md) - Building locally
- [`docs/gradle-migration.md`](../../docs/gradle-migration.md) - Gradle details
- [GitHub Actions Docs](https://docs.github.com/en/actions)

---

## Support

For issues or questions about GitHub Actions:
1. Check the Actions tab for error logs
2. Review workflow YAML syntax
3. Consult GitHub Actions documentation
4. Check Gradle build output

---

**Last Updated**: October 19, 2025  
**Gradle Version**: 7.6.1  
**Java Versions Tested**: 11, 17, 21  
**Platforms**: Ubuntu, macOS, Windows
