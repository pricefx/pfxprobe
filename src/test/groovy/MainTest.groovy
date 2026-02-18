import Models.CodeClimateIssue
import spock.lang.Specification

import java.nio.file.Files

class MainTest extends Specification {
    private String originalUserDir

    def setup() {
        originalUserDir = System.getProperty("user.dir")
    }

    def cleanup() {
        System.setProperty("user.dir", originalUserDir)
    }

    def "Quality Gate With Invalid Severity Level Throws Exception"() {
        given:
        def args = ["-dir", "src/test/resources", "-qualitygate", "invalid"] as String[]

        when:
        Main.main(args)

        then:
        def exception = thrown(Exception)
        exception.message.contains("Invalid quality severity: invalid")
        exception.message.contains("Valid values: info, minor, major, critical")
    }

    def "Always Writes Quality Markdown Report Without Quality Gate"() {
        given:
        File tempDir = Files.createTempDirectory("pfxprobe-main-qualityreport").toFile()
        System.setProperty("user.dir", tempDir.absolutePath)
        String fixtureDir = new File(originalUserDir, "fixtures").absolutePath
        def args = ["-dir", fixtureDir] as String[]

        when:
        Main.main(args)

        then:
        noExceptionThrown()
        File markdownReport = new File(tempDir, ".pfxprobe/pfxprobe-quality.md")
        markdownReport.exists()
        markdownReport.text.contains("# Quality Gate Report")
        markdownReport.text.contains("Threshold: info")
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
        validLevels.size() == 4
    }

    def "Quality Gate Accepts All Valid Severity Levels"() {
        expect:
        // Verify that all documented severity levels are in the array
        CodeClimateIssue.severityImportance.toList() == ["info", "minor", "major", "critical"]
    }
}
