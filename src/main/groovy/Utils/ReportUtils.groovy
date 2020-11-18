package Utils

import Models.PfxCodeIssue
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ReportUtils {

    static void printIssueDiscoveriesToConsole(List<PfxCodeIssue> codeIssues) {
        Map<String, Integer> severityCounts = [:]
        for (issue in codeIssues)
            severityCounts[issue.IssuePattern.Severity] = (severityCounts[issue.IssuePattern.Severity] ?: 0) + 1

        println "Found a total of ${codeIssues.size()} issues, ${severityCounts.toString()}"
    }

    static void writeCodeClimateReport(List<PfxCodeIssue> pfxCodeIssues) {
        JsonSlurper jsonReader = new JsonSlurper();
        File codeClimateReport = new File("codeclimate.json")

        // Read codeclimate file, add to end of issues list and overwrite file with all results
        println("Appending Issues to CodeClimate Report")

        HashSet<Map> allIssuesFormatted = pfxCodeIssues.collect { it.getCodeClimateIssueFormat() }
        HashSet<Map> codeClimateIssues = codeClimateReport?.size() > 0 ? jsonReader.parse(codeClimateReport) as List<Map> : []
        allIssuesFormatted.addAll(codeClimateIssues)

        codeClimateReport.setText(new JsonBuilder(allIssuesFormatted).toString().replace("},{", "},\n{"))
    }
}