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

        // Quality gate mode: print detailed report and fail if any issues found
        if (cmd.hasOption(CommandLineUtils.qualityGateArg)) {
            String minSeverity = cmd.getOptionValue(CommandLineUtils.qualityGateArg) ?: "info"
            
            if (!CodeClimateIssue.severityImportance.contains(minSeverity)) {
                throw new Exception("Invalid quality gate severity: ${minSeverity}. Valid values: ${CodeClimateIssue.severityImportance.join(', ')}")
            }
            
            int minSeverityIndex = CodeClimateIssue.severityImportance.findIndexOf { it == minSeverity }
            List<CodeClimateIssue> filteredIssues = allIssues.findAll { issue ->
                int issueSeverityIndex = CodeClimateIssue.severityImportance.findIndexOf { it == issue.getIssueSeverity() }
                issueSeverityIndex >= minSeverityIndex
            }
            
            ReportUtils.printQualityGateReport(allIssues, minSeverity)
            
            if (!filteredIssues.isEmpty()) {
                throw new Exception("Quality gate failed: found ${filteredIssues.size()} issue(s) at or above '${minSeverity}' severity")
            }
        }

        // Check if failure severities are defined and fail job if matching issues are found
        if (allIssues.any { issue -> issue.isFailingSeverity() }) {
            throw new Exception("Found issue(s) >= ${CodeClimateIssue.failureSeverity} severity")
        }

        println("pfxprobe Finished...")
    }
}