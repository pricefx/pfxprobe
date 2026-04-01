package Utils

import Models.CodeNarcIssue
import groovy.json.JsonSlurper
import org.codenarc.CodeNarc

class CodeNarcUtils {

    /**
     * Resolves the default ruleset file path.
     * Uses local ruleset if present, otherwise falls back to embedded ruleset.
     */
    private static String getDefaultRulesFilePath() {
        final String relativePath = "./codenarc.ruleset"

        if (new File(relativePath).exists()) {
            return relativePath
        }

        String embeddedRulesetPath = writeEmbeddedRuleset()
        if (embeddedRulesetPath) {
            return embeddedRulesetPath
        }

        return relativePath
    }

    private static String writeEmbeddedRuleset() {
        InputStream rulesetStream = CodeNarcUtils.class.getResourceAsStream("/codenarc.ruleset")

        if (!rulesetStream) {
            return null
        }

        File tempRuleset = File.createTempFile("codenarc", ".ruleset")
        tempRuleset.deleteOnExit()

        rulesetStream.withCloseable { input ->
            tempRuleset.withOutputStream { output ->
                output << input
            }
        }

        return tempRuleset.absolutePath
    }

    static List<CodeNarcIssue> getCodeNarcIssues(String[] scanDirs, String userRulesFileRelativePath) {
        println("Starting codeNarc analysis")
        File temporaryReportFile = File.createTempFile("pfxprobe-codenarc-", ".json")
        temporaryReportFile.delete()

        if (scanDirs.size() > 1) {
            println("CodeNarc analysis will be executed only for the first provided input source directory")
        }

        runAnalysis(scanDirs?.getAt(0), userRulesFileRelativePath, temporaryReportFile.absolutePath)

        ArrayList<CodeNarcIssue> codeNarcCodeIssues = []

        try {
            codeNarcCodeIssues = parseReport(temporaryReportFile.absolutePath) ?: []
        } finally {
            temporaryReportFile.delete()
        }

        return codeNarcCodeIssues
    }

    /**
     * We assume that image runs in repository with PFX Studio file structure
     * @param scanDir
     * @param userRulesFilePath String containing path to ruleset file. Relative to repository root directory or absolute.
     */
    private static void runAnalysis(String scanDir, String userRulesFilePath, String reportFilePath) {
        String rulesFilePath = userRulesFilePath ?: getDefaultRulesFilePath()

        safeExecuteAnalysisForDir(rulesFilePath, scanDir, reportFilePath)
    }

    private static void safeExecuteAnalysisForDir(String rulesFilePath, String scanDir, String reportFilePath) {
        try {
            if (doesFileExist(scanDir)) {
                // CodeNarc throws System.exit() if the basedir cannot be found, so we must make sure it's there
                CodeNarc.main("-rulesetfiles=file:$rulesFilePath", "-basedir=$scanDir", "-report=json:$reportFilePath")
            } else {
                println("Basedir [$scanDir] doesn't exist in the workspace")
            }
        } catch (any) {
            println("There was a problem with generating report for [$scanDir] directory. Skipping analysis...")
            any.printStackTrace()
        }
    }

    private static List<CodeNarcIssue> parseReport(String reportFilePath) {
        Map codeNarcReport = readReport(reportFilePath)
        println("Report [$reportFilePath] parsed")

        if (!codeNarcReport || !isAnyViolationFound(codeNarcReport)) {
            println("No violations found")
            return []
        }

        return translateRawReport(codeNarcReport)
    }

    private static Map readReport(String reportFilePath) {
        Map report = [:]
        if (doesFileExist(reportFilePath)) {
            report = new JsonSlurper().parse(new File(reportFilePath))
        } else {
            println("Report [$reportFilePath] was not generated. Skipping...")
        }

        return report
    }

    private static boolean isAnyViolationFound(Map codeNarcReport) {
        int filesWithViolations = codeNarcReport?.summary?.filesWithViolations ?: 0

        return filesWithViolations > 0
    }

    static boolean doesFileExist(String filePath) {
        return new File(filePath).exists()
    }

    private static List<CodeNarcIssue> translateRawReport(Map codeNarcReport) {
        return codeNarcReport.packages.inject([]) { List results, Map pathReport ->
            pathReport.files?.each { Map fileReport ->
                String filePath = "${pathReport.path}/${fileReport.name}"
                fileReport.violations?.each { Map violation ->
                    results << new CodeNarcIssue(violation.ruleName, filePath, violation.priority, violation.lineNumber ?: 0, violation.message)
                }
            }

            return results
        }
    }
}
