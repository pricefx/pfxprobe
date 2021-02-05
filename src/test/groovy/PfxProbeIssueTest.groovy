import Models.PfxProbeIssue
import Models.PfxProbeIssuePattern
import spock.lang.Specification

class PfxProbeIssueTest extends Specification {

    def "Issue Fingerprint Generator Works Consistently"() {
        expect:
        PfxProbeIssue.generateIssueFingerprint(path, name, description) == hash

        where:
        path                           | name            | description        || hash
        "C:\\windows\\path\\structure" | "windows-issue" | "Issue On Windows" || "4de893e33ac30d3fa08ba21a06e18b89"
        "/var/linux/path/structure"    | "linux-issue"   | "Issue On Linux"   || "34f45ea4f7db4f5bb6fa1cb436f1ef43"
        null                           | null            | null               || "d41d8cd98f00b204e9800998ecf8427e"
    }

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