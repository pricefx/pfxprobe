class PfxIssuePattern {

    public PfxIssuePattern(
            String name,
            String description,
            String pattern,
            String severity,
            String... categories
    ) {
        Name = name
        Description = description
        Pattern = pattern
        Severity = severity
        Categories = categories
    }

    public String Name
    public String Description
    public String Pattern
    public String Severity
    public List<String> Categories

}

class PfxCodeIssue {

    PfxCodeIssue(
            PfxIssuePattern pattern,
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

    public PfxIssuePattern IssuePattern
    public String FilePath
    public int FileLineBegin
    public int FileLineEnd
    public int FileLineCharBegin
    public int FileLineCharEnd

    public String getDescription(){
        return "[$IssuePattern.Severity] $IssuePattern.Description"
    }


    public Map getCodeClimateIssueFormat() {
        return [
                type       : "issue",
                engine_name: "pfxnarc",
                check_name : IssuePattern.Name,
                description: getDescription(),
                severity   : IssuePattern.Severity,
                categories : IssuePattern.Categories,
                location   : [
                        path : FilePath,
                        lines: [begin: FileLineBegin, end: FileLineEnd],
                        chars: [begin: FileLineCharBegin, end: FileLineCharEnd]
                ],
                fingerprint: Utils.generateIssueFingerprint(FilePath, IssuePattern.Name, IssuePattern.Description)
        ]
    }
}