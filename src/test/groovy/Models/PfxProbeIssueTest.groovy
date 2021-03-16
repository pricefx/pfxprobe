package Models

import spock.lang.Specification

class PfxProbeIssueTest extends Specification {

    def "Code Issue Can Generate Correct CodeClimate Issue Map Structure"() {
        given:
        PfxProbeIssuePattern issuePattern = new PfxProbeIssuePattern(
                "Some Issue Pattern",
                "Pattern Description",
                /.*abc.*/,
                "blocker"
        )
        PfxProbeIssue codeIssue = new PfxProbeIssue(
                issuePattern,
                "/var/some/path",
                0,
                10,
                15,
                40
        )
        Map expectedIssueFormat = [type: "issue", engine_name: "pfxprobe", check_name: "Some Issue Pattern", description: "[blocker] Pattern Description", severity: "blocker", categories: [], location: [path: "/var/some/path", lines: [begin: 0, end: 10], chars: [begin: 15, end: 40]], fingerprint: "b35193b423e3f3635638665377767678"]

        expect:
        codeIssue.getCodeClimateIssueFormat() == expectedIssueFormat
    }
}