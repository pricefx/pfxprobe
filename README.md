# PfxNarc
**pfxnarc** is a command line tool that is designed to scan a PriceFx configuration codebase and report
commonly known malpractices recommended improvements. It is based loosely on [CodeClimate](https://github.com/codeclimate/codeclimate).

This tool will search for issues in the provided directories and generate a *codeclimate.json* report which can then
be used in other tools, such as Gitlab CI for tracking Resolved / Newly Introduced issues between commits.  

## Distribution and Usage

This project is distributed as:
1. A [Docker Container](https://gitlab.pricefx.eu/tools/pfxnarc/container_registry)
1. A [Maven Package / Executable JAR](https://gitlab.pricefx.eu/tools/pfxnarc/-/packages)

### Gitlab CI Usage
##### Add the following to your *.gilab-ci.yml* file:

```
stages:
  - analyze

pfxnarc:
  image: cregistry.pricefx.eu/tools/pfxnarc
  stage: analyze
  only:
    - develop
    - merge_requests
  script:
    - pfxnarc -dir .
  artifacts:
    when: always
    reports:
      codequality: codeclimate.json
    paths:
      - ./codeclimate.json
```

### JAR Usage

```
usage: java -jar pfxnarc.jar -dir <arg>
 -dir <arg>   Directories to be scanned
```

### CLI Usage

```
usage: pfxnarc -dir <arg>
 -dir <arg>   Directories to be scanned
```