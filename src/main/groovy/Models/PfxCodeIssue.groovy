package Models

import java.security.MessageDigest

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

    String getDescription() {
        return "[$IssuePattern.Severity] $IssuePattern.Description"
    }

    Map<String, Object> getCodeClimateIssueFormat() {
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
                fingerprint: generateIssueFingerprint()
        ]
    }

    String generateIssueFingerprint() {
        return generateIssueFingerprint(FilePath, IssuePattern.Name, IssuePattern.Description)
    }

    static String generateIssueFingerprint(String path, String name, String description) {
        byte[] b = [path, name, description].bytes.flatten()
        return MessageDigest.getInstance("MD5").digest(b).encodeHex().toString()
    }
}
