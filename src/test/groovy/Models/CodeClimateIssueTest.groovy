package Models

import spock.lang.Specification

class CodeClimateIssueTest extends Specification {
    String testRuleName = "testRule"
    String testFilePath = "elements/someFile.groovy"
    String testMessage = "Some validation message"
    int testFileLine = 10

    PfxProbeIssuePattern criticalTestPattern = new PfxProbeIssuePattern(testRuleName, testMessage, "...", "critical")

    def "CompareTo - the same object is equal"() {
        when:
        CodeClimateIssue myIssue = new CodeNarcIssue("testRule", "elements/someFile.groovy", 1, 10, "myMessage")

        then:
        myIssue <=> myIssue == 0
    }

    def "CompareTo - the same error is equal for different implementations"() {
        when:
        CodeClimateIssue narcIssue = new CodeNarcIssue(testRuleName, testFilePath, 1, testFileLine, testMessage)
        PfxProbeIssue probeIssue = new PfxProbeIssue(criticalTestPattern, testFilePath, testFileLine, testFileLine, 0, 0)

        then:
        narcIssue <=> probeIssue == 0
    }

    def "CompareTo - different priorities gets recognized properly"() {
        given:
        int majorNarcSeverity = 2
        int minorNarcSeverity = 3

        when:
        CodeClimateIssue majorNarcIssue = new CodeNarcIssue(testRuleName, testFilePath, majorNarcSeverity, testFileLine, testMessage)
        CodeClimateIssue minorNarcIssue = new CodeNarcIssue(testRuleName, testFilePath, minorNarcSeverity, testFileLine, testMessage)
        PfxProbeIssue criticalProbeIssue = new PfxProbeIssue(criticalTestPattern, testFilePath, testFileLine, testFileLine, 0, 0)

        then:
        (majorNarcIssue <=> criticalProbeIssue) > 0
        (criticalProbeIssue <=> majorNarcIssue) < 0
        (minorNarcIssue <=> majorNarcIssue) > 0
        (majorNarcIssue <=> minorNarcIssue) < 0
        (minorNarcIssue <=> criticalProbeIssue) > 0
        (criticalProbeIssue <=> minorNarcIssue) < 0
    }

}
