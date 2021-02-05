package Models

import java.security.MessageDigest

abstract class CodeClimateIssue implements Comparable {
    static String[] severityImportance = [
            "info",
            "minor",
            "major",
            "critical",
            "blocker"
    ]

    static String failureSeverity = "blocker"

    abstract String getIssueDescription()

    abstract String getIssueSeverity()

    abstract Map<String, Object> getCodeClimateIssueFormat()

    boolean isFailingSeverity() {
        severityImportance.findIndexOf { it == getIssueSeverity() } >= severityImportance.findIndexOf { it == failureSeverity }
    }

    static String generateIssueFingerprint(String path, String name, String description) {
        byte[] b = [path, name, description].bytes.flatten()
        return MessageDigest.getInstance("MD5").digest(b).encodeHex().toString()
    }

    int compareTo(Object o) {
        CodeClimateIssue compared = o as CodeClimateIssue

        int thisIndex = severityImportance.findIndexOf {
            it == this.getIssueSeverity()
        }

        int comparedIndex = severityImportance.findIndexOf {
            it == compared.getIssueSeverity()
        }

        return thisIndex != comparedIndex ?
                comparedIndex <=> thisIndex :
                compared.getIssueDescription() <=> getIssueDescription()
    }
}