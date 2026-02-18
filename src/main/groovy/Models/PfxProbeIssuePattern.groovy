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
        return findOccurrencesInFile(file, file.text, FileUtils.getFileLines(file))
    }

    List<PfxProbeIssue> findOccurrencesInFile(File file, String fileContent, Map<Integer, IntRange> fileLines) {
        List<PfxProbeIssue> allIssues = []
        Matcher matcher = fileContent =~ Pattern
        matcher.results().each { match ->
            Integer lineBegin = fileLines.find {
                it.value.fromInt <= match.start() && it.value.toInt >= match.start()
            }?.key
            Integer lineEnd = fileLines.find {
                it.value.toInt >= match.end()
            }?.key

            if (lineBegin && lineEnd) {
                allIssues << new PfxProbeIssue(this, file.path, lineBegin, lineEnd, match.start(), match.end())
            }
        }
        return allIssues
    }
}
