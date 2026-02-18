# pfxprobe
**pfxprobe** scans Pricefx configuration code and reports common issues and improvement opportunities.
It is based loosely on [CodeClimate](https://github.com/codeclimate/codeclimate).

Reports are written to `.pfxprobe/` in the working directory:
- `.pfxprobe/codeclimate.json`
- `.pfxprobe/codeclimate.sarif.json`
- `.pfxprobe/pfxprobe-quality.md`

`-qualitygate` controls failure behavior only.
The markdown quality report is always written and does not fail builds.

There are two analysis types:
1. **Probe** - Custom regex-based rules for Pricefx patterns
2. **Narc** - Generic Groovy static analysis via [CodeNarc](https://codenarc.org/)
     
## Distribution and Usage

This project is distributed as:
1. [Docker Container](https://gitlab.pricefx.eu/tools/pfxprobe/container_registry)
1. [Maven Package / Executable JAR](https://gitlab.pricefx.eu/tools/pfxprobe/-/packages)
1. [Executable Binaries](https://gitlab.pricefx.eu/tools/pfxprobe/-/jobs/artifacts/master/browse/target/distribution?job=package)

### Gitlab CI Usage
##### Add the following to your `.gitlab-ci.yml` file:

```
stages:
  - analyze

pfxprobe:
  image: pricefx/pfxprobe
  stage: analyze
  only:
    - develop
    - merge_requests
  script:
    - pfxprobe -dir .
  artifacts:
    when: always
    reports:
      codequality: .pfxprobe/codeclimate.json
    paths:
      - ./.pfxprobe/codeclimate.json
      - ./.pfxprobe/codeclimate.sarif.json
      - ./.pfxprobe/pfxprobe-quality.md
```

### Docker Container Usage

On Windows, with cmd:
```
docker run --rm -it --name pfxprobe -v %cd%:/code pricefx/pfxprobe pfxprobe -dir code
```

On Mac / Linux with bash:
```
docker run --rm -it --name pfxprobe -v ${PWD}:/code pricefx/pfxprobe pfxprobe -dir code
```

### JAR Usage

```bash
java -jar pfxprobe.jar -dir <directory> [options]

Options:
  -dir <arg>            Directories to be scanned (CodeNarc runs on first directory only)
  -n                    Execute CodeNarc analysis only
  -p                    Execute pfxprobe analysis only
  -rulefile <arg>       Path to CodeNarc ruleset file (defaults to ./codenarc.ruleset)
  -qualitygate [level]  Enable quality gate mode with optional severity threshold
                         Displays detailed report and fails build if issues found
                         Valid levels: info, minor, major, critical
                         Default: info (fails on any issue)

Examples:
  java -jar pfxprobe.jar -dir .
  java -jar pfxprobe.jar -dir . -qualitygate
  java -jar pfxprobe.jar -dir . -qualitygate major
  java -jar pfxprobe.jar -dir src -rulefile ./custom-rules.ruleset -qualitygate critical
```

### Local Maven Usage

Direct Maven execution with JDK 21 + Maven installed:

```bash
mvn test
mvn package exec:java -Dexec.mainClass=Main -Dexec.args="-dir fixtures"
```

### CLI Usage

```bash
pfxprobe -dir <directory> [options]

Options:
  -dir <arg>            Directories to be scanned (CodeNarc runs on first directory only)
  -n                    Execute CodeNarc analysis only
  -p                    Execute pfxprobe analysis only
  -rulefile <arg>       Path to CodeNarc ruleset file (defaults to ./codenarc.ruleset)
  -qualitygate [level]  Enable quality gate mode with optional severity threshold
                         Displays detailed report and fails build if issues found
                         Valid levels: info, minor, major, critical
                         Default: info (fails on any issue)

Examples:
  pfxprobe -dir .
  pfxprobe -dir . -qualitygate
  pfxprobe -dir . -qualitygate major
  pfxprobe -dir src -rulefile ./custom-rules.ruleset -qualitygate critical
```

### Quality Gate and Reports

`-qualitygate` prints a detailed console report and fails when issues are at or above the selected severity.
Reports are still written regardless of gate usage.

**Severity Levels** (from lowest to highest):
- `info` - Informational issues
- `minor` - Minor code quality issues
- `major` - Significant issues that should be addressed
- `critical` - Critical issues requiring immediate attention

**Usage in CI/CD:**
```yaml
pfxprobe:
  image: pricefx/pfxprobe
  stage: analyze
  script:
    - pfxprobe -dir . -qualitygate major  # Fail on major+ issues
  artifacts:
    when: always
    reports:
      codequality: .pfxprobe/codeclimate.json
    paths:
      - ./.pfxprobe/codeclimate.json
      - ./.pfxprobe/codeclimate.sarif.json
      - ./.pfxprobe/pfxprobe-quality.md
```

**Quality Gate Output:**
- Individual issue details with file location and description
- Summary grouped by severity level
- Summary grouped by check type
- Exit code 1 if threshold exceeded, 0 if passed

**Quality Report Output:**
- Markdown file at `.pfxprobe/pfxprobe-quality.md`
- Includes issue list and severity/rule summaries
- Never changes exit code

## Additional Attributions

* [Original project avatar image rights](https://www.flaticon.com/authors/icongeek26)
