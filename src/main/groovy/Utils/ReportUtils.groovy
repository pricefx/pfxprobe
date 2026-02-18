package Utils

import Models.CodeClimateIssue
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ReportUtils {
    final static String reportsDirectoryName = ".pfxprobe"
    final static String codeClimateReportFileName = "codeclimate.json"
    final static String sarifReportFileName = "codeclimate.sarif.json"
    final static String qualityReportFileName = "pfxprobe-quality.md"

    static void printIssueDiscoveriesToConsole(List<CodeClimateIssue> codeIssues) {
        Map<String, Integer> severityCounts = [:]
        for (issue in codeIssues)
            severityCounts[issue.getIssueSeverity()] = (severityCounts[issue.getIssueSeverity()] ?: 0) + 1

        println "Found a total of ${codeIssues.size()} issues, ${severityCounts.toString()}"
    }

    static void writeCodeClimateReport(List<CodeClimateIssue> pfxCodeIssues) {
        JsonSlurper jsonReader = new JsonSlurper();
        File codeClimateReport = getReportFile(codeClimateReportFileName)

        // Read codeclimate file, add to end of issues list and overwrite file with all results
        println("Appending Issues to CodeClimate Report")

        ArrayList<Map> allIssuesFormatted = pfxCodeIssues.collect { it.getCodeClimateIssueFormat() }

        ArrayList<Map> codeClimateIssues = codeClimateReport?.size() > 0 ? jsonReader.parse(codeClimateReport) as List<Map> : []
        allIssuesFormatted.addAll(codeClimateIssues)

        codeClimateReport.setText(new JsonBuilder(allIssuesFormatted).toString().replace("},{", "},\n{"))

        writeSarifReport(allIssuesFormatted)
    }

    static void printQualityGateReport(List<CodeClimateIssue> codeIssues, String minSeverity = "info") {
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

    /** Writes a markdown quality report for issues at or above the configured threshold. */
    static void writeQualityMarkdownReport(List<CodeClimateIssue> codeIssues, String minSeverity = "info") {
        File markdownReport = getReportFile(qualityReportFileName)

        StringBuilder content = new StringBuilder()
        content << "# Quality Gate Report\n\n"
        content << "- Total issues: ${codeIssues.size()}\n"
        content << "- Threshold: ${minSeverity}\n\n"

        if (codeIssues.isEmpty()) {
            content << "No code quality issues found.\n"
            markdownReport.setText(content.toString())
            return
        }

        Map<String, Integer> severityCounts = codeIssues
            .groupBy { it.getIssueSeverity() }
            .collectEntries { severity, issues -> [severity, issues.size()] }

        Map<String, Integer> checkCounts = codeIssues
            .groupBy { it.getCodeClimateIssueFormat().check_name }
            .collectEntries { check, issues -> [check, issues.size()] }
            .sort { -it.value }

        content << "## Issues\n\n"
        codeIssues.each { issue ->
            Map<String, Object> format = issue.getCodeClimateIssueFormat()
            content << "- ${format.check_name}: ${issue.getIssueDescription()} (${format.location.path}:${format.location.lines.begin})\n"
        }

        content << "\n## Summary by severity\n\n"
        severityCounts.each { severity, count ->
            content << "- ${severity}: ${count}\n"
        }

        content << "\n## Summary by rule\n\n"
        checkCounts.each { check, count ->
            content << "- ${check}: ${count}\n"
        }

        markdownReport.setText(content.toString())
    }

    /** Converts Code Climate issues into SARIF and writes the SARIF report file. */
    private static void writeSarifReport(List<Map> codeClimateIssues) {
        println("Writing SARIF report to ${sarifReportFileName}")

        Map<String, Integer> ruleIndexByRuleId = [:]
        List<Map> rules = []
        List<Map> results = []

        codeClimateIssues.each { Map issue ->
            String engineName = (issue.engine_name ?: "unknown-engine").toString()
            String checkName = (issue.check_name ?: "unknown-check").toString()
            String ruleId = "${engineName}/${checkName}"

            if (!ruleIndexByRuleId.containsKey(ruleId)) {
                ruleIndexByRuleId[ruleId] = rules.size()
                rules << [
                        id              : ruleId,
                        name            : checkName,
                        shortDescription: [text: checkName],
                        helpUri         : ApplicationMetadata.sarifRuleHelpUri,
                        properties      : [engine_name: engineName]
                ]
            }

            Map location = issue.location instanceof Map ? issue.location as Map : [:]
            Map lines = location.lines instanceof Map ? location.lines as Map : [:]
            Integer startLine = normalizeLine(lines.begin)
            Integer endLine = normalizeLine(lines.end)

            Map region = [startLine: startLine]
            if (endLine >= startLine) {
                region.endLine = endLine
            }

            Map result = [
                    ruleId   : ruleId,
                    ruleIndex: ruleIndexByRuleId[ruleId],
                    level    : mapSeverityToSarifLevel(issue.severity?.toString()),
                    message  : [text: (issue.description ?: checkName).toString()],
                    locations: [[
                                        physicalLocation: [
                                                artifactLocation: [uri: (location.path ?: "").toString()],
                                                region          : region
                                        ]
                                ]]
            ]

            if (issue.fingerprint) {
                result.partialFingerprints = [
                        codeClimateFingerprint: issue.fingerprint.toString()
                ]
            }

            results << result
        }

        Map sarifReport = [
                '$schema': 'https://json.schemastore.org/sarif-2.1.0.json',
                version  : '2.1.0',
                runs     : [[
                                      tool   : [
                                              driver: [
                                                      name          : ApplicationMetadata.artifactId,
                                                      informationUri: ApplicationMetadata.informationUri,
                                                      version       : ApplicationMetadata.version,
                                                      rules: rules
                                              ]
                                      ],
                                     results: results
                             ]]
        ]

        getReportFile(sarifReportFileName).setText(new JsonBuilder(sarifReport).toPrettyString())
    }

    /** Ensures the reports directory exists and returns the requested report file. */
    private static File getReportFile(String reportFileName) {
        File reportsDirectory = new File(System.getProperty("user.dir"), reportsDirectoryName)
        reportsDirectory.mkdirs()
        return new File(reportsDirectory, reportFileName)
    }

    /** Normalizes line values to a valid 1-based line number. */
    private static Integer normalizeLine(Object lineValue) {
        Integer parsedLine = (lineValue instanceof Number) ? lineValue as Integer : 1
        if (parsedLine < 1) {
            return 1
        }

        return parsedLine
    }

    /** Maps Code Climate severities to SARIF levels. */
    private static String mapSeverityToSarifLevel(String severity) {
        switch (severity) {
            case "major":
                return "warning"
            case "critical":
                return "error"
            case "info":
            case "minor":
            default:
                return "note"
        }
    }
}
