package Models

class PfxProbeIssue extends CodeClimateIssue {
    PfxProbeIssue(
            PfxProbeIssuePattern pattern,
            String filePath,
            int fileLineBegin,
            int fileLineEnd,
            int fileLineCharBegin,
            int fileLineCharEnd
    ) {
        IssuePattern = pattern
        FilePath = filePath
        FileLineBegin = fileLineBegin
        FileLineEnd = fileLineEnd
        FileLineCharBegin = fileLineCharBegin
        FileLineCharEnd = fileLineCharEnd
    }

    public PfxProbeIssuePattern IssuePattern
    public String FilePath
    public int FileLineBegin
    public int FileLineEnd
    public int FileLineCharBegin
    public int FileLineCharEnd

    String getIssueDescription() {
        return "[$IssuePattern.Severity] $IssuePattern.Description"
    }

    String getIssueSeverity() {
        IssuePattern.Severity
    }

    Map<String, Object> getCodeClimateIssueFormat() {
        return [
                type       : "issue",
                engine_name: "pfxprobe",
                check_name : IssuePattern.Name,
                description: getIssueDescription(),
                severity   : IssuePattern.Severity,
                categories : IssuePattern.Categories,
                location   : [
                        path : FilePath,
                        lines: [begin: FileLineBegin, end: FileLineEnd],
                        chars: [begin: FileLineCharBegin, end: FileLineCharEnd]
                ],
                fingerprint: generateIssueFingerprint(FilePath, IssuePattern.Name, IssuePattern.Description)
        ]
    }
}
