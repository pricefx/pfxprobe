import Models.CodeClimateIssue
import Models.CodeNarcIssue
import Models.PfxProbeIssue
import Utils.CodeNarcUtils
import Utils.CommandLineUtils
import Utils.PfxProbeUtils
import Utils.ReportUtils
import org.apache.commons.cli.CommandLine

class Main {
    static void main(String... args) {
        println("pfxprobe Started...")
        CommandLine cmd = CommandLineUtils.parseInputArgs(args)

        ArrayList<PfxProbeIssue> pfxProbeIssues = CommandLineUtils.shouldRunProbeAnalysis(cmd) ?
                PfxProbeUtils.getPfxProbeIssues(cmd.getOptionValues(CommandLineUtils.scanDirArg)) :
                []

        ArrayList<CodeNarcIssue> codeNarcIssues = CommandLineUtils.shouldRunNarcAnalysis(cmd) ?
                CodeNarcUtils.getCodeNarcIssues(cmd.getOptionValues(CommandLineUtils.scanDirArg), cmd.getOptionValue(CommandLineUtils.narcRulesFileArg)) :
                []

        ArrayList<CodeClimateIssue> allIssues = (codeNarcIssues + pfxProbeIssues).sort()

        ReportUtils.printIssueDiscoveriesToConsole(allIssues)
        ReportUtils.writeCodeClimateReport(allIssues)

        String qualitySeverity = cmd.hasOption(CommandLineUtils.qualityGateArg) ?
                resolveAndValidateSeverity(cmd.getOptionValue(CommandLineUtils.qualityGateArg)) :
                "info"
        List<CodeClimateIssue> qualityReportIssues = filterIssuesBySeverity(allIssues, qualitySeverity)
        ReportUtils.writeQualityMarkdownReport(qualityReportIssues, qualitySeverity)

        // Quality gate mode: print detailed report and fail if any issues found
        if (cmd.hasOption(CommandLineUtils.qualityGateArg)) {
            ReportUtils.printQualityGateReport(allIssues, qualitySeverity)

            if (!qualityReportIssues.isEmpty()) {
                throw new Exception("Quality gate failed: found ${qualityReportIssues.size()} issue(s) at or above '${qualitySeverity}' severity")
            }
        }

        println("pfxprobe Finished...")
    }

    /** Resolves an optional severity and validates it against supported levels. */
    private static String resolveAndValidateSeverity(String configuredSeverity) {
        String minSeverity = configuredSeverity ?: "info"

        if (!CodeClimateIssue.severityImportance.contains(minSeverity)) {
            throw new Exception("Invalid quality severity: ${minSeverity}. Valid values: ${CodeClimateIssue.severityImportance.join(', ')}")
        }

        return minSeverity
    }

    /** Returns issues that meet or exceed the provided severity threshold. */
    private static List<CodeClimateIssue> filterIssuesBySeverity(List<CodeClimateIssue> allIssues, String minSeverity) {
        int minSeverityIndex = CodeClimateIssue.severityImportance.findIndexOf { it == minSeverity }

        return allIssues.findAll { issue ->
            int issueSeverityIndex = CodeClimateIssue.severityImportance.findIndexOf { it == issue.getIssueSeverity() }
            issueSeverityIndex >= minSeverityIndex
        }
    }
}
