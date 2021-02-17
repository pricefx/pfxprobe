package Models

class CodeNarcIssue extends CodeClimateIssue {
    CodeNarcIssue(
            String ruleName,
            String filePath,
            int issueSeverity,
            int fileLine,
            String message
    ) {
        RuleName = ruleName
        FilePath = filePath
        IssueSeverity = parseCodeNarcSeverity(issueSeverity)
        FileLine = fileLine
        Message = message
    }

    String RuleName
    String FilePath
    String IssueSeverity
    int FileLine
    String Message

    @Override
    String getIssueDescription() {
        return "[$IssueSeverity] $Message"
    }

    @Override
    String getIssueSeverity() {
        return IssueSeverity
    }

    @Override
    Map<String, Object> getCodeClimateIssueFormat() {
        return [
                type       : "issue",
                engine_name: "codenarc",
                check_name : RuleName,
                description: getIssueDescription(),
                severity   : IssueSeverity,
                categories : null,
                location   : [
                        path : FilePath,
                        lines: [begin: FileLine, end: FileLine],
                        chars: [begin: null, end: null]
                ],
                fingerprint: generateIssueFingerprint(FilePath, RuleName, Message)
        ]
    }

    private String parseCodeNarcSeverity(int level) {
        String severity = ""
        switch (level) {
            case 1:
                severity = severityImportance[3]
                break
            case 2:
                severity = severityImportance[2]
                break
            case 3:
                severity = severityImportance[1]
                break
        }

        return severity
    }
}
