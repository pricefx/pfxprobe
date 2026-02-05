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

        ArrayList<Map> allIssuesFormatted = pfxCodeIssues.collect { it.getCodeClimateIssueFormat() }

        ArrayList<Map> codeClimateIssues = codeClimateReport?.size() > 0 ? jsonReader.parse(codeClimateReport) as List<Map> : []
        allIssuesFormatted.addAll(codeClimateIssues)

        codeClimateReport.setText(new JsonBuilder(allIssuesFormatted).toString().replace("},{", "},\n{"))
    }

    static void printQualityGateReport(List<PfxProbeIssue> codeIssues, String minSeverity = "info") {
        println ""

        if (codeIssues.isEmpty()) {
            println "✅ No code quality issues found!"
            return
        }

        int issueCount = codeIssues.size()
        println "❌ Found ${issueCount} code quality issue(s)"
        if (minSeverity != "info") {
            println "   Quality gate threshold: ${minSeverity.toUpperCase()} and above"
        }
        println ""

        // Print individual issues
        codeIssues.each { issue ->
            Map<String, Object> format = issue.getCodeClimateIssueFormat()
            
            println "  🔴 ${format.check_name}: ${issue.getIssueDescription()}"
            println "  📄 ${format.location.path}:${format.location.lines.begin}"
        }

        // Print summaries
        println ""
        println "📊 Summary by severity:"
        
        Map<String, Integer> severityCounts = codeIssues
            .groupBy { it.getIssueSeverity() }
            .collectEntries { severity, issues -> [severity, issues.size()] }
        
        severityCounts.each { severity, count ->
            println "   • ${severity.toUpperCase()}: ${count}"
        }

        println ""
        println "📋 Summary by rule:"
        
        Map<String, Integer> checkCounts = codeIssues
            .groupBy { it.getCodeClimateIssueFormat().check_name }
            .collectEntries { check, issues -> [check, issues.size()] }
            .sort { -it.value }
        
        checkCounts.each { check, count ->
            println "   • ${check}: ${count}"
        }

        println ""
    }
}
