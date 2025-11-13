import Models.CodeClimateIssue
import spock.lang.Specification

class MainTest extends Specification {

    def "Quality Gate With Invalid Severity Level Throws Exception"() {
        given:
        def args = ["-dir", "src/test/resources", "-qualitygate", "invalid"] as String[]

        when:
        Main.main(args)

        then:
        def exception = thrown(Exception)
        exception.message.contains("Invalid quality gate severity: invalid")
        exception.message.contains("Valid values: info, minor, major, critical, blocker")
    }

    def "Quality Gate Validates Severity Levels Are Valid"() {
        when:
        // This test verifies that severity levels match CodeClimateIssue.severityImportance
        def validLevels = CodeClimateIssue.severityImportance

        then:
        validLevels.contains("info")
        validLevels.contains("minor")
        validLevels.contains("major")
        validLevels.contains("critical")
        validLevels.contains("blocker")
        validLevels.size() == 5
    }

    def "Quality Gate Accepts All Valid Severity Levels"() {
        expect:
        // Verify that all documented severity levels are in the array
        CodeClimateIssue.severityImportance.toList() == ["info", "minor", "major", "critical", "blocker"]
    }
}
