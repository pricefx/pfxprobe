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
  image: cregistry.pricefx.eu/tools/pfxprobe
  stage: analyze
  only:
    - develop
    - merge_requests
  script:
    - pfxprobe -dir . -ruleset /codenarc.ruleset
  artifacts:
    when: always
    reports:
      codequality: codeclimate.json
    paths:
      - ./codeclimate.json
```

### Docker Container Usage

Step 1. Log In your Docker client to the private pfx gitlab registry:
- Substitute **EMAILADDRESS** with your pricefx email address
- Substitute **ACCESSTOKEN** below with a [personal access token](https://gitlab.pricefx.eu/profile/personal_access_tokens)
    - Access token must be granted "read_registry" privilege
        
This step only needs to be done once
```
docker login https://cregistry.pricefx.eu --username EMAILADDRESS --password ACCESSTOKEN
```

Step 2. Run the following command from your source code parent folder (with Bash or PowerShell - NOT CMD)

```
docker run --rm -it --name pfxprobe -v ${PWD}:/code cregistry.pricefx.eu/tools/pfxprobe bash pfxprobe -dir code -ruleset /codenarc.ruleset
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
 -rulefile <arg>   Path to ruleset file relative to project directory. To
                   use default Accelerator ruleset, use
                   `/codenarc.ruleset`
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
 -rulefile <arg>   Path to ruleset file relative to project directory. To
                   use default Accelerator ruleset, use
                   `/codenarc.ruleset`
```

### Attributions

* [Original project avatar image rights](https://www.flaticon.com/authors/icongeek26) 
