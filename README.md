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

### Docker Container Usage

Step 1. Log In your Docker client to the private pfx gitlab registry:
- Substitute **EMAILADDRESS** with your pricefx email address
- Substitute **ACCESSTOKEN** below with a [personal access token](https://gitlab.pricefx.eu/profile/personal_access_tokens)
    - Access token must be granted "read_registry" privilege
        
This step only needs to be done once
```
docker login https://cregistry.pricefx.eu --username EMAILADDRESS --password ACCESSTOKEN
```

 Step 2. Run the following command from your source code parent folder
 ```
docker run --rm -it --name pfxnarc -v ${PWD}:/code cregistry.pricefx.eu/tools/pfxnarc -dir code
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