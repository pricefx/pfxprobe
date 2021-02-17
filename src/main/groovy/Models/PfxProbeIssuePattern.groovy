package Models

import Utils.FileUtils

import java.util.regex.Matcher

class PfxProbeIssuePattern {
    PfxProbeIssuePattern(
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

    String Name
    String Description
    String Pattern
    String Severity
    List<String> Categories

    List<PfxProbeIssue> findOccurrencesInFile(File file) {
        List<PfxProbeIssue> allIssues = []
        Matcher matcher = file.text =~ Pattern
        matcher.results().each { match ->
            Integer lineBegin = FileUtils.getFileLines(file).find {
                it.value.fromInt <= match.start() && it.value.toInt >= match.start()
            }?.key
            Integer lineEnd = FileUtils.getFileLines(file).find {
                it.value.toInt >= match.end()
            }?.key

            if (lineBegin && lineEnd) {
                allIssues << new PfxProbeIssue(this, file.path, lineBegin, lineEnd, match.start(), match.end())
            }
        }
        return allIssues
    }
}
