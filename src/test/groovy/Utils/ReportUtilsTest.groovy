package Utils

import Models.CodeNarcIssue
import Models.PfxProbeIssue
import spock.lang.Specification

class ReportUtilsTest extends Specification {

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
}
