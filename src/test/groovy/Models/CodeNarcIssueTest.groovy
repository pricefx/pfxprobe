package Models

import spock.lang.Specification

class CodeNarcIssueTest extends Specification {
    def "Code Issue Can Generate Correct CodeClimate Issue Map Structure"() {
        given:
        CodeNarcIssue codeIssue = new CodeNarcIssue(
                'Some Issue Name',
                '/var/some/path',
                issueSeverity,
                10,
                "Some Description")

        expect:
        codeIssue.getCodeClimateIssueFormat() == expected

        where:
        issueSeverity | expected
        1             | [type: "issue", engine_name: "codenarc", check_name: "Some Issue Name", description: "[critical] Some Description", severity: "critical", categories: null, location: [path: "/var/some/path", lines: [begin: 10, end: 10], chars: [begin: null, end: null]], fingerprint: "5627a639e970827f0881af32d74e78f8"]
        2             | [type: "issue", engine_name: "codenarc", check_name: "Some Issue Name", description: "[major] Some Description", severity: "major", categories: null, location: [path: "/var/some/path", lines: [begin: 10, end: 10], chars: [begin: null, end: null]], fingerprint: "5627a639e970827f0881af32d74e78f8"]
        3             | [type: "issue", engine_name: "codenarc", check_name: "Some Issue Name", description: "[minor] Some Description", severity: "minor", categories: null, location: [path: "/var/some/path", lines: [begin: 10, end: 10], chars: [begin: null, end: null]], fingerprint: "5627a639e970827f0881af32d74e78f8"]
        4             | [type: "issue", engine_name: "codenarc", check_name: "Some Issue Name", description: "[] Some Description", severity: "", categories: null, location: [path: "/var/some/path", lines: [begin: 10, end: 10], chars: [begin: null, end: null]], fingerprint: "5627a639e970827f0881af32d74e78f8"]
    }
}
