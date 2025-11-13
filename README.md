# pfxprobe
**pfxprobe** is a command line tool that is designed to scan a PriceFx configuration codebase and report
commonly known malpractices recommended improvements. It is based loosely on [CodeClimate](https://github.com/codeclimate/codeclimate).

This tool will search for issues in the provided directories and generate a *codeclimate.json* report which can then
be used in other tools, such as Gitlab CI for tracking Resolved / Newly Introduced issues between commits.

There are two types of analysis available:
1. **Probe** - Uses custom regex based rules created to handle PriceFX specific rules
2. **Narc** - Uses generic Groovy static code analysis engine [CodeNarc](https://codenarc.org/). Default ruleset is based on the Accelerators team and can be modified with job configuration if necessary
     
## Distribution and Usage

This project is distributed as:
1. [Docker Container](https://gitlab.pricefx.eu/tools/pfxprobe/container_registry)
1. [Maven Package / Executable JAR](https://gitlab.pricefx.eu/tools/pfxprobe/-/packages)
1. [Executable Binaries](https://gitlab.pricefx.eu/tools/pfxprobe/-/jobs/artifacts/master/browse/target/distribution?job=package)

### Gitlab CI Usage
##### Add the following to your *.gilab-ci.yml* file:

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
      codequality: codeclimate.json
    paths:
      - ./codeclimate.json
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
                        Valid levels: info, minor, major, critical, blocker
                        Default: info (fails on any issue)

Examples:
  java -jar pfxprobe.jar -dir .
  java -jar pfxprobe.jar -dir . -qualitygate
  java -jar pfxprobe.jar -dir . -qualitygate major
  java -jar pfxprobe.jar -dir src -rulefile ./custom-rules.ruleset -qualitygate blocker
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
                        Valid levels: info, minor, major, critical, blocker
                        Default: info (fails on any issue)

Examples:
  pfxprobe -dir .
  pfxprobe -dir . -qualitygate
  pfxprobe -dir . -qualitygate major
  pfxprobe -dir src -rulefile ./custom-rules.ruleset -qualitygate blocker
```

### Quality Gate Feature

The quality gate feature provides enhanced reporting and enforces build failures based on code quality issues:

**Severity Levels** (from lowest to highest):
- `info` - Informational issues
- `minor` - Minor code quality issues
- `major` - Significant issues that should be addressed
- `critical` - Critical issues requiring immediate attention
- `blocker` - Blocking issues that must be fixed

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
      codequality: codeclimate.json
```

**Quality Gate Output:**
- Individual issue details with file location and description
- Summary grouped by severity level
- Summary grouped by check type
- Exit code 1 if threshold exceeded, 0 if passed

### Attributions

* [Original project avatar image rights](https://www.flaticon.com/authors/icongeek26) 
