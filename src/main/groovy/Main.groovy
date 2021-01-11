import Models.PfxCodeIssue
import Utils.CommandLineUtils
import Utils.FileUtils
import Utils.ReportUtils
import org.apache.commons.cli.CommandLine
import java.util.concurrent.CopyOnWriteArrayList

class Main {
    private static String failureSeverity = "blocker"
    private static String[] severityImportance = [
            "info",
            "minor",
            "major",
            "critical",
            "blocker"
    ]

    static void main(String... args) {
        println("pfxprobe Started...")
        CommandLine cmd = CommandLineUtils.parseInputArgs(args)

        CopyOnWriteArrayList<PfxCodeIssue> allIssues = []
        cmd.getOptionValues(CommandLineUtils.scanDirArg).toList().parallelStream().forEach { String dirPath ->

            def issuePatterns = PatternDictionary.getPatternDictionary()
            def groovyFiles = FileUtils.getGroovyFilesInPath(dirPath)

            println("pfxprobe Scanning Directory $dirPath")
            groovyFiles.parallelStream().forEach { file ->
                issuePatterns.parallelStream().forEach { issuePattern ->
                    allIssues.addAll(issuePattern.findOccurrencesInFile(file))
                }
            }
        }

        //sort found issues by highest severity
        allIssues.sort { it.getDescription() }.sort { a, b ->
            severityImportance.findIndexOf {
                it == b.IssuePattern.Description
            } <=> severityImportance.findIndexOf {
                it == a.IssuePattern.Severity
            }
        }

        ReportUtils.printIssueDiscoveriesToConsole(allIssues)

        ReportUtils.writeCodeClimateReport(allIssues)

        // Check if failure severities are defined and fail job if matching issues are found
        if (allIssues.any { issue -> severityImportance.findIndexOf { it == issue.IssuePattern.Severity } >= severityImportance.findIndexOf { it == failureSeverity } })
            throw new Exception("Found issue(s) >= $failureSeverity severity")

        println("pfxprobe Finished...")
    }
}