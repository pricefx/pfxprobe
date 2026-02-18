package Models

import java.security.MessageDigest

abstract class CodeClimateIssue implements Comparable {
    final static String[] severityImportance = [
            "info",
            "minor",
            "major",
            "critical"
    ]

    abstract String getIssueDescription()

    abstract String getIssueSeverity()

    abstract Map<String, Object> getCodeClimateIssueFormat()

    static String generateIssueFingerprint(String path, String name, String description) {
        byte[] b = [path, name, description].bytes.flatten()
        return MessageDigest.getInstance("MD5").digest(b).encodeHex().toString()
    }

    int compareTo(Object o) {
        CodeClimateIssue compared = o as CodeClimateIssue

        int thisSeverityIndex = severityImportance.findIndexOf {
            it == this.getIssueSeverity()
        }
        String thisIndexedDescription = "$thisSeverityIndex${getIssueDescription()}"

        int comparedSeverityIndex = severityImportance.findIndexOf {
            it == compared.getIssueSeverity()
        }
        String comparedIndexedDescription = "$comparedSeverityIndex${compared.getIssueDescription()}"

        return comparedIndexedDescription <=> thisIndexedDescription
    }
}
