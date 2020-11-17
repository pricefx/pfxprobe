To use, add the following to your *.gilab-ci.yml* file:

```
pfxnarc:
  image: cregistry.pricefx.eu/tools/pfxnarc
  stage: analyze
  only:
    - develop
    - merge_requests
  script:
    - pfxnarc -from .
  artifacts:
    when: always
    reports:
      codequality: codeclimate.json
    paths:
      - ./codeclimate.json
```