# GitHub Actions Quick Reference

## 🚀 Quick Start

### View Build Status
```
GitHub → Repository → Actions Tab
```

### Create a Release
```bash
# 1. Update version if needed
# 2. Commit changes
# 3. Create and push tag
git tag v3.4.1.12
git push origin v3.4.1.12
# Release workflow runs automatically
# Check Releases tab for artifacts
```

### Manual Workflow Trigger
```
GitHub → Actions → Release → Run workflow
```

## 📋 Workflows Overview

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `gradle-build.yml` | Push to master/main/develop, PRs | Test on Java 11/17/21 + Ubuntu/macOS/Windows |
| `release.yml` | Tag `v*` or manual | Create GitHub Release with artifacts |
| `code-quality.yml` | Push, PRs, Weekly | Code formatting, tests, dependencies |

## 🔍 Workflow Details

### gradle-build.yml
**Tests on multiple configurations:**
- Java 11, 17, 21
- Ubuntu, macOS, Windows

**Artifacts**: `build/libs/*.jar` (30 days)

**Status**: Must pass before merge (if branch protection enabled)

### release.yml
**Triggered by:**
- Tag matching `v*` (e.g., `v3.4.1.12`)
- Manual trigger via Actions UI

**Creates:**
- GitHub Release
- Uploads JAR + distributions
- Auto-detects version from `version.txt`

### code-quality.yml
**Jobs:**
1. Checkstyle - Code formatting checks
2. Tests - Full test suite
3. Dependency-check - Dependency analysis

**Schedule:** Weekly (Sunday 2 AM UTC)

## 📊 Artifacts

**JAR Files**: `buddi-jar-java{version}` (30 days)
**Distributions**: `.tar`, `.zip` (30 days)
**Reports**: Quality, test, dependency (7 days)

Download from:
- Actions tab → Workflow run → Artifacts section
- Or Releases tab (for tagged releases)

## 🛠️ Common Tasks

### Push Changes & Trigger Build
```bash
git add .
git commit -m "Update Buddi"
git push origin develop
# gradle-build.yml runs automatically
```

### Create Release
```bash
# 1. Ensure version.txt is updated
# 2. Push to master
git push origin master

# 3. Create tag
git tag v3.4.1.12
git push origin v3.4.1.12

# 4. Watch Actions tab
# 5. Release appears in Releases tab
```

### Check Build Status
```
GitHub → Actions → See all workflows
- Green ✅ = Passed
- Red ❌ = Failed
- Yellow ⏳ = In progress
```

## ⚙️ Configuration

### Java Versions Tested
Currently: 11, 17, 21

To add more:
Edit `.github/workflows/gradle-build.yml`:
```yaml
java-version: ['11', '17', '21', '23']
```

### Build Matrix Platforms
Currently: Ubuntu, macOS, Windows

To change:
Edit `.github/workflows/gradle-build.yml`:
```yaml
os: [ubuntu-latest, macos-latest, windows-latest]
```

### Branch Triggers
Currently: `master`, `main`, `develop`

To change:
Edit workflow files - search for `branches:` and update

### Release Tag Pattern
Currently: `v*` (e.g., `v3.4.1.12`)

To change:
Edit `.github/workflows/release.yml` - update `tags:` section

## 🔐 Security

### Permissions
- Uses `GITHUB_TOKEN` (minimal scope)
- Scoped to current repository only
- Auto-revoked after workflow

### Best Practices
1. Require status checks before merge (branch protection)
2. Review code in PRs before merge
3. Tag releases carefully
4. Monitor dependency reports weekly

## 📖 Documentation

Full details: `docs/github-actions.md`

Topics:
- Complete workflow reference
- Customization examples
- Troubleshooting guide
- Best practices
- Security considerations

## ✅ Status Indicators

| Icon | Meaning |
|------|---------|
| ✅ | Workflow passed |
| ❌ | Workflow failed |
| ⏳ | In progress |
| ⏭️ | Skipped |

## 🎯 Next Steps

1. **Push to GitHub** - Trigger first build
2. **Monitor Actions tab** - Watch build status
3. **Create tag for release** - Push v* tag
4. **Configure branch protection** - Require status checks
5. **Review documentation** - See `docs/github-actions.md`

## 💡 Tips

- **Speed up builds**: Gradle caching automatically enabled (60% faster)
- **Reduce storage**: Artifacts auto-cleaned after retention period
- **Automate releases**: Just push a tag to create release
- **Monitor quality**: Weekly checks catch issues early
- **Test everywhere**: Runs on Java 11/17/21 + all platforms

---

For full documentation: See `docs/github-actions.md`
