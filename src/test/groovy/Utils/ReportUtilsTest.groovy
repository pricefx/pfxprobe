package Utils

import Models.CodeNarcIssue
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import spock.lang.Specification

import java.nio.file.Files

class ReportUtilsTest extends Specification {
    private String originalUserDir
    private PrintStream originalOut

    def setup() {
        originalUserDir = System.getProperty("user.dir")
        originalOut = System.out
    }

    def cleanup() {
        System.setOut(originalOut)
        System.setProperty("user.dir", originalUserDir)
    }

    def "Quality Gate Report Shows All Issues When Empty"() {
        given:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)

        when:
        ReportUtils.printQualityGateReport([])

        then:
        def output = buffer.toString()
        output.contains("✅ No code quality issues found!")
    }

    def "Quality Gate Report Shows Issue Count"() {
        given:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)
        def issues = [
            new CodeNarcIssue("TestRule1", "file1.groovy", 2, 10, "Test message 1"),
            new CodeNarcIssue("TestRule2", "file2.groovy", 3, 20, "Test message 2"),
        ]

        when:
        ReportUtils.printQualityGateReport(issues)

        then:
        def output = buffer.toString()
        output.contains("❌ Found 2 code quality issue(s)")
    }

    def "Quality Gate Report Shows Threshold When Not Info"() {
        given:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)
        def issues = [
            new CodeNarcIssue("TestRule", "file.groovy", 3, 10, "Test message"),
        ]

        when:
        ReportUtils.printQualityGateReport(issues, "major")

        then:
        def output = buffer.toString()
        output.contains("Quality gate threshold: MAJOR and above")
    }

    def "Quality Gate Report Does Not Show Threshold When Info"() {
        given:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)
        def issues = [
            new CodeNarcIssue("TestRule", "file.groovy", 3, 10, "Test message"),
        ]

        when:
        ReportUtils.printQualityGateReport(issues, "info")

        then:
        def output = buffer.toString()
        !output.contains("Quality gate threshold")
    }

    def "Quality Gate Report Shows Severity Summary"() {
        given:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)
        def issues = [
            new CodeNarcIssue("TestRule1", "file1.groovy", 2, 10, "Test message 1"),  // minor
            new CodeNarcIssue("TestRule2", "file2.groovy", 3, 20, "Test message 2"),  // major
            new CodeNarcIssue("TestRule3", "file3.groovy", 3, 30, "Test message 3"),  // major
        ]

        when:
        ReportUtils.printQualityGateReport(issues)

        then:
        def output = buffer.toString()
        output.contains("📊 Summary by severity:")
        output.contains("MINOR:")
        output.contains("MAJOR:")
    }

    def "Quality Gate Report Shows Check Summary"() {
        given:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)
        def issues = [
            new CodeNarcIssue("TestRule1", "file1.groovy", 2, 10, "Test message 1"),
            new CodeNarcIssue("TestRule1", "file2.groovy", 2, 20, "Test message 2"),
            new CodeNarcIssue("TestRule2", "file3.groovy", 3, 30, "Test message 3"),
        ]

        when:
        ReportUtils.printQualityGateReport(issues)

        then:
        def output = buffer.toString()
        output.contains("📋 Summary by rule:")
        output.contains("TestRule1:")
        output.contains("TestRule2:")
    }

    def "Quality Gate Report Shows Individual Issue Details"() {
        given:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)
        def issues = [
            new CodeNarcIssue("TestRule", "path/to/file.groovy", 2, 42, "This is a test issue"),
        ]

        when:
        ReportUtils.printQualityGateReport(issues)

        then:
        def output = buffer.toString()
        output.contains("🔴 TestRule: [major]")
        output.contains("📄 path/to/file.groovy:42")
    }

    def "Quality Markdown Report Is Generated Without Gate Failure"() {
        given:
        File tempDir = Files.createTempDirectory("pfxprobe-quality-markdown").toFile()
        System.setProperty("user.dir", tempDir.absolutePath)

        def issues = [
            new CodeNarcIssue("TestRule", "path/to/file.groovy", 2, 42, "This is a test issue"),
        ]

        when:
        ReportUtils.writeQualityMarkdownReport(issues, "info")

        then:
        File markdownReport = new File(tempDir, "${ReportUtils.reportsDirectoryName}/${ReportUtils.qualityReportFileName}")
        markdownReport.exists()
        markdownReport.text.contains("# Quality Gate Report")
        markdownReport.text.contains("Threshold: info")
        markdownReport.text.contains("TestRule: [major] This is a test issue (path/to/file.groovy:42)")
    }

    def "Quality Markdown Report Handles Empty Issues"() {
        given:
        File tempDir = Files.createTempDirectory("pfxprobe-quality-markdown-empty").toFile()
        System.setProperty("user.dir", tempDir.absolutePath)

        when:
        ReportUtils.writeQualityMarkdownReport([], "major")

        then:
        File markdownReport = new File(tempDir, "${ReportUtils.reportsDirectoryName}/${ReportUtils.qualityReportFileName}")
        markdownReport.exists()
        markdownReport.text.contains("No code quality issues found.")
        markdownReport.text.contains("Threshold: major")
    }

    def "Write CodeClimate Report Also Writes SARIF Report"() {
        given:
        File tempDir = Files.createTempDirectory("pfxprobe-report-test").toFile()
        System.setProperty("user.dir", tempDir.absolutePath)

        def issues = [
            new CodeNarcIssue("TestRuleOne", "src/FileOne.groovy", 2, 10, "First issue"),
            new CodeNarcIssue("TestRuleTwo", "src/FileTwo.groovy", 1, 20, "Second issue"),
        ]

        when:
        ReportUtils.writeCodeClimateReport(issues)

        then:
        File sarifReport = new File(tempDir, "${ReportUtils.reportsDirectoryName}/${ReportUtils.sarifReportFileName}")
        sarifReport.exists()

        Map sarif = new JsonSlurper().parse(sarifReport) as Map
        sarif.version == "2.1.0"
        sarif.runs.size() == 1
        sarif.runs[0].tool.driver.name == ApplicationMetadata.artifactId
        sarif.runs[0].tool.driver.informationUri == ApplicationMetadata.informationUri
        sarif.runs[0].tool.driver.version == ApplicationMetadata.version
        sarif.runs[0].tool.driver.rules.every { it.helpUri == ApplicationMetadata.sarifRuleHelpUri }
        sarif.runs[0].results.size() == 2
        sarif.runs[0].results[0].ruleId == "codenarc/TestRuleOne"
        sarif.runs[0].results[0].level == "warning"
        sarif.runs[0].results[1].ruleId == "codenarc/TestRuleTwo"
        sarif.runs[0].results[1].level == "error"
    }

    def "SARIF Report Includes Existing CodeClimate Entries"() {
        given:
        File tempDir = Files.createTempDirectory("pfxprobe-existing-report-test").toFile()
        System.setProperty("user.dir", tempDir.absolutePath)

        File codeClimateReport = new File(tempDir, "${ReportUtils.reportsDirectoryName}/${ReportUtils.codeClimateReportFileName}")
        codeClimateReport.parentFile.mkdirs()
        codeClimateReport.text = new JsonBuilder([
            [
                type       : "issue",
                engine_name: "pfxprobe",
                check_name : "LegacyRule",
                description: "Legacy issue",
                severity   : "minor",
                location   : [path: "src/Legacy.groovy", lines: [begin: 0, end: 0]],
                fingerprint: "legacy-fingerprint"
            ]
        ]).toString()

        def issues = [
            new CodeNarcIssue("CurrentRule", "src/Current.groovy", 2, 12, "Current issue"),
        ]

        when:
        ReportUtils.writeCodeClimateReport(issues)

        then:
        File sarifReport = new File(tempDir, "${ReportUtils.reportsDirectoryName}/${ReportUtils.sarifReportFileName}")
        Map sarif = new JsonSlurper().parse(sarifReport) as Map
        sarif.runs[0].results.size() == 2
        sarif.runs[0].results*.ruleId.contains("pfxprobe/LegacyRule")
        sarif.runs[0].results*.ruleId.contains("codenarc/CurrentRule")
        sarif.runs[0].results.find { it.ruleId == "pfxprobe/LegacyRule" }.locations[0].physicalLocation.region.startLine == 1
    }
}
