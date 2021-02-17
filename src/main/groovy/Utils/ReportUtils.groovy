package Utils

import Models.PfxProbeIssue
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ReportUtils {

    static void printIssueDiscoveriesToConsole(List<PfxProbeIssue> codeIssues) {
        Map<String, Integer> severityCounts = [:]
        for (issue in codeIssues)
            severityCounts[issue.getIssueSeverity()] = (severityCounts[issue.getIssueSeverity()] ?: 0) + 1

        println "Found a total of ${codeIssues.size()} issues, ${severityCounts.toString()}"
    }

    static void writeCodeClimateReport(List<PfxProbeIssue> pfxCodeIssues) {
        JsonSlurper jsonReader = new JsonSlurper();
        File codeClimateReport = new File("codeclimate.json")

        // Read codeclimate file, add to end of issues list and overwrite file with all results
        println("Appending Issues to CodeClimate Report")

        // TODO - HashSet for allIssuesFormatted made sure that there are no duplicates while appending to existing file.
        //  I should check if Gitlab is not doing the sorting by itself. Then we could ignore the sorting and use HashSet.
        //  If Gitlab doesn't handle sorting, we have to sort it after this hashset was created and populated.
        //  But I don't know if from commit X we can even see codeclimate report for commit X-1. If we can't see it -
        //  appending to file if not necessary at all because new file will be generated for every commit.
        //  Seems like artifacts are not shared between pipelines, so we will never append to an existing file Code-Quality.gitlab-ci.yml Assuming that only one report like this will be generated in a single pipeline
        ArrayList<Map> allIssuesFormatted = pfxCodeIssues.collect { it.getCodeClimateIssueFormat() }

        ArrayList<Map> codeClimateIssues = codeClimateReport?.size() > 0 ? jsonReader.parse(codeClimateReport) as List<Map> : []
        allIssuesFormatted.addAll(codeClimateIssues)

        codeClimateReport.setText(new JsonBuilder(allIssuesFormatted).toString().replace("},{", "},\n{"))
    }
}