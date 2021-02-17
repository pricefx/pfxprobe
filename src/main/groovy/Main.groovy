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
        // TODO - update readme
        println("pfxprobe Started...")
        CommandLine cmd = CommandLineUtils.parseInputArgs(args)

        ArrayList<PfxProbeIssue> pfxProbeIssues = cmd.hasOption(CommandLineUtils.probeAnalysisArg) ?
                PfxProbeUtils.getPfxProbeIssues(cmd.getOptionValues(CommandLineUtils.probeScanDirArg)) :
                []

        ArrayList<CodeNarcIssue> codeNarcIssues = cmd.hasOption(CommandLineUtils.narcAnalysisArg) ?
                CodeNarcUtils.getCodeNarcIssues(cmd.getOptionValue(CommandLineUtils.narcRulesFileArg)) :
                []

        ArrayList<CodeClimateIssue> allIssues = (codeNarcIssues + pfxProbeIssues).sort()

        ReportUtils.printIssueDiscoveriesToConsole(allIssues)
        ReportUtils.writeCodeClimateReport(allIssues)

        // Check if failure severities are defined and fail job if matching issues are found
        if (allIssues.any { issue -> issue.isFailingSeverity() }) {
            throw new Exception("Found issue(s) >= ${CodeClimateIssue.failureSeverity} severity")
        }

        println("pfxprobe Finished...")
    }
}