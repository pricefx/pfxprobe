package Models

class CodeNarcIssue extends CodeClimateIssue {
    CodeNarcIssue(
            String ruleName,
            String filePath,
            int severity,
            int fileLine,
            String message
    ) {
        RuleName = ruleName
        FilePath = filePath
        Severity = parseCodeNarcSeverity(severity)
        FileLine = fileLine
        Message = message
    }

    public String RuleName
    public String FilePath
    public String Severity
    public int FileLine
    public String Message

    String getIssueDescription() {
        return "[$Severity] $Message"
    }

    String getIssueSeverity() {
        return Severity
    }

    Map<String, Object> getCodeClimateIssueFormat() {
        return [
                type       : "issue",
                engine_name: "codenarc",
                check_name : RuleName,
                description: getIssueDescription(),
                severity   : Severity,
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
