import spock.lang.Specification
import spock.lang.Unroll

class PatternDictionaryTest extends Specification {

    @Unroll
    def "Pattern #pattern.Name Has a Test Case"() {
        expect:
        assert testDictionary.containsKey(pattern.Name)

        where:
        pattern << PatternDictionary.getPatternDictionary()
    }

    @Unroll
    def "Pattern #pattern.Name Detects Its Test Case"() {
        when:
        File tempFile = new File("patternDetectTest.txt")

        then:
        tempFile.setText(testDictionary[pattern.Name])
        assert pattern.findOccurrencesInFile(tempFile).size() > 0

        cleanup:
        tempFile.delete()

        where:
        pattern << PatternDictionary.getPatternDictionary()
    }

    static Map<String, String> testDictionary = [
            "trace-in-repo"                 : /api.trace("Some Message")/,
            "logging-in-repo"               : /api.logInfo("Some Message")/,
            "lib-explicit-returns"          : /def someUnTypedMethod(){ return 1 }/,
            "use-stream-util"               : /api.stream("P")/,
            "harcoded-attribute"            : /def t = "key1"/,
            "file-not-formatted"            : /def someMethod(){}/,
            "deprecated-getelement"         : /api.getElement("SomeElement")/,
    ]
}
