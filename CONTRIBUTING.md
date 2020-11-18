# Contributing

This repository is written primarily in [Groovy](https://groovy-lang.org/) so that anybody who is familiar with 
PriceFx configuration can contribute in a familiar language.

### Issue Patterns

If you wish to contribute a new issue pattern (Thank You!!) all you need to do is add it to the dictionary, 
and add a test case for it to ensure it's working as expected.

- [Add to the Pattern Dictionary Here](https://gitlab.pricefx.eu/tools/pfxnarc/-/blob/master/src/main/groovy/PatternDictionary.groovy)
- [Add to the Pattern Test Cases Here](https://gitlab.pricefx.eu/tools/pfxnarc/-/blob/master/src/test/groovy/PatternDictionaryTest.groovy)

## Merge Requests

Once you've finished making your desired modifications, please submit a merge request to the [**master**](https://gitlab.pricefx.eu/tools/pfxnarc/-/tree/master) branch.
If you correctly added your test case, the pipeline should automatically test your contribution and upon success a repo maintainer will accept and merge your request.

## Build, Run & Debug

### Maven

This is a Maven project. Project dependencies need to be imported and set up for the project to work in an IDE.
IntelliJ should automatically recognise the *pom.xml* file and prompt to import the project.
If not automatically recognised, find the *pom.xml* file in the root directory, right click it and choose "Import as Maven Project".
IntelliJ will take some time to download and index the dependencies. Once finished, you should be able to build & run the project.

### Debug In IDE

To test / debug the program in your local dev environment, all you need to do is execute the "void main()" method in the Main.groovy class at the /src/main/groovy/ directory.
You will need to provide required *args* in order for it to work. If you don't provide the required args, the program will throw an exception and tell you which args are missing.
To provide *args* in IntelliJ, you need to supply them in the [Run Configuration > Program Arguments](https://www.jetbrains.com/help/objc/add-environment-variables-and-program-arguments.html)

You can find the required program arguments in the root [README on the Project Overview](https://gitlab.pricefx.eu/tools/pfxnarc)

## Packaging and Distribution

### Packaging

The Maven configuration is set up to generate an executable jar into the /target/ folder, and executable binaries into the /target/distribution/bin folder.
To generate these, simply execute the "package" Maven command in the Maven toolbar in IntelliJ.
Run Maven "clean" to purge and delete the /target/ build folder.

### Distribution

This project does distribution inside Gitlab CI pipeline for the **master** branch only.
Inside that pipeline, Gitlab first runs *package* to generate the artifacts.
From here, the pipeline will run a *publish-maven* stage which publishes the Maven package and executable jar to the [Project Package Registry](https://gitlab.pricefx.eu/tools/pfxnarc/-/packages)
The master pipeline will also run a *publish-docker* stage which builds a Docker image containing the executable binaries and publishes the image to the [Project Container Registry](https://gitlab.pricefx.eu/tools/pfxnarc/container_registry)



