package Utils

import spock.lang.Specification

class FileUtilsTest extends Specification {
    static File groovyFileOne = new File("src/test/resources/GroovyTestFiles/FileOne.groovy")
    static File groovyFileTwo = new File("src/test/resources/GroovyTestFiles/FileTwo.groovy")
    static File groovyFileThree = new File("src/test/resources/GroovyTestFiles/NestedFolder/FileThree.groovy")

    def "File Line Reading and Caching Works Consistently"() {
        given:
        File file = new File("fileLinesTest.txt")
        file.setText("""
            This is Line 1
            This is line two
            this Is lInE tHrE3
        """)

        expect:
        FileUtils.getFileLines(file) == [1: 0..0, 2: 0..26, 3: 27..55, 4: 56..86, 5: 87..95]

        cleanup:
        file.delete()
    }

    def "File Path Matching Regular Expressions"() {
        given:
        File fileThree = groovyFileThree

        expect:
        FileUtils.matchesSearchExclusions(fileThree, [".*NestedFolder.*"])
        FileUtils.matchesSearchExclusions(fileThree, [/.*NestedFolder.*/])
    }

    def "Can Find Groovy Files in Given Path and Doesn't Find Non Groovy Files"() {
        given:
        String searchPath = "src/test/resources/GroovyTestFiles"
        String fileThreePathEscaped = groovyFileThree.path.replace("\\", "/")

        expect:
        FileUtils.getGroovyFilesInPath(searchPath).containsAll([groovyFileOne, groovyFileTwo, groovyFileThree]) // no exclusion
        FileUtils.getGroovyFilesInPath(searchPath, [/.*NestedFolder.*/]).containsAll([groovyFileOne, groovyFileTwo]) // regexp exclusion
        FileUtils.getGroovyFilesInPath(searchPath, [fileThreePathEscaped]).containsAll([groovyFileOne, groovyFileTwo]) // exact path exclusion
    }

    def "Can Convert Windows File Paths to Unix Style Strings for Matching"() {
        expect:
        FileUtils.getUnixStyleFilePath(windowsStyle) == unixStyle
        FileUtils.getUnixStyleFilePath(unixStyle) == unixStyle

        where:
        windowsStyle                                                             || unixStyle
        ""                                                                       || ""
        /C:${File.separator}Windows${File.separator}Folder${File.separator}Path/ || "C:/Windows/Folder/Path"
        "some${File.separator}var${File.separator}folder"                        || "some/var/folder"
    }

    def "Can Detect Groovy Files, Does Not False Detect Other Files"() {
        expect:
        FileUtils.isGroovyFile(new File(filePath)) == result

        where:
        filePath                                            || result
        "src/test/resources/SomeTextFile.txt"               || false
        "src/test/resources/GroovyTestFiles/FileOne.groovy" || true
    }
}
