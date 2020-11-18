package Models

import Utils.FileUtils

import java.util.regex.Matcher

class PfxIssuePattern {
    PfxIssuePattern(
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

    List<PfxCodeIssue> findOccurrencesInFile(File file) {
        List<PfxCodeIssue> allIssues = []
        Matcher matcher = file.text =~ Pattern
        matcher.results().each { match ->
            Integer lineBegin = FileUtils.getFileLines(file).find {
                it.value.fromInt <= match.start() && it.value.toInt >= match.start()
            }?.key
            Integer lineEnd = FileUtils.getFileLines(file).find {
                it.value.toInt >= match.end()
            }?.key

            if (lineBegin && lineEnd) {
                allIssues << new PfxCodeIssue(this, file.path, lineBegin, lineEnd, match.start(), match.end())
            }
        }
        return allIssues
    }
}
