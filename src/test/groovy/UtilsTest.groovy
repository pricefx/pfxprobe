import spock.lang.Specification

class UtilsTest extends Specification {

    def "Issue Fingerprint Generator Works Consistently"() {
        expect:
        Utils.generateIssueFingerprint(path, name, description) == hash

        where:
        path                           | name            | description        || hash
        "C:\\windows\\path\\structure" | "windows-issue" | "Issue On Windows" || "4de893e33ac30d3fa08ba21a06e18b89"
        "/var/linux/path/structure"    | "linux-issue"   | "Issue On Linux"   || "34f45ea4f7db4f5bb6fa1cb436f1ef43"
        null                           | null            | null               || "d41d8cd98f00b204e9800998ecf8427e"
    }
}