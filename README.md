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

```
usage: java -jar pfxprobe.jar -dir <arg> [-n] [-p] [-rulefile <arg>]
By default, when -p or -n parameters are not provided, both analysis types
are executed.
 -dir <arg>        Directories to be scanned. CodeNarc analysis will run
                   only on first one
 -n                Execute CodeNarc analysis
 -p                Execute pfxprobe analysis
 -rulefile <arg>   Path to ruleset file relative to project directory. By
                   default Accelerators team ruleset is used. Custom
                   configurations can be created using codenarc.ruleset
                   file as a template
```

### CLI Usage

```
usage: pfxprobe -dir <arg> [-n] [-p] [-rulefile <arg>]
By default, when -p or -n parameters are not provided, both analysis types
are executed.
 -dir <arg>        Directories to be scanned. CodeNarc analysis will run
                   only on first one
 -n                Execute CodeNarc analysis
 -p                Execute pfxprobe analysis
 -rulefile <arg>   Path to ruleset file relative to project directory. By
                   default Accelerators team ruleset is used. Custom
                   configurations can be created using codenarc.ruleset
                   file as a template
```

### Attributions

* [Original project avatar image rights](https://www.flaticon.com/authors/icongeek26) 
