package Utils

import Models.CodeNarcIssue
import groovy.json.JsonSlurper
import org.codenarc.CodeNarc

class CodeNarcUtils {

    final static String defaultRulesFileRelativePath = "codenarc.ruleset"
    final static String jsonCodeReportFileName = 'CodeNarcCodeJsonReport.json'

    static List<CodeNarcIssue> getCodeNarcIssues(String[] scanDirs, String userRulesFileRelativePath) {
        println("Starting codeNarc analysis")
        runAnalysis(scanDirs?.getAt(0), userRulesFileRelativePath) // TODO - we can scan only one directory with codeNarc

        ArrayList<CodeNarcIssue> codeNarcCodeIssues = parseReport() ?: []

        return codeNarcCodeIssues
    }

    /**
     * We assume that image runs in repository with PFX Studio file structure
     * @param userRulesFileRelativePath
     */
    private static void runAnalysis(String scanDir, String userRulesFileRelativePath) {
        String rulesFileRelativePath = userRulesFileRelativePath ?: defaultRulesFileRelativePath

        safeExecuteAnalysisForDir(rulesFileRelativePath, scanDir)
    }

    private static void safeExecuteAnalysisForDir(String rulesFileRelativePath, String scanDir) {
        try {
            if (doesFileExist(scanDir)) {
                // CodeNarc throws System.exit() if the basedir cannot be found, so we must make sure it's there
                CodeNarc.main("-rulesetfiles=file:$rulesFileRelativePath", "-basedir=$scanDir", "-report=json:$jsonCodeReportFileName")
            } else {
                println("Basedir [$scanDir] doesn't exist in the workspace")
            }
        } catch (any) {
            println("There was a problem with generating report for [$scanDir] directory. Skipping analysis...")
            any.printStackTrace()
        }
    }

    // TODO - tests
    private static List<CodeNarcIssue> parseReport() {
        Map codeNarcReport = readReport()
        println("Report [$jsonCodeReportFileName] parsed")

        if (!codeNarcReport || !isAnyViolationFound(codeNarcReport)) {
            println("No violations found")
            return []
        }

        // TODO - Try to refactor in a simpler way with .collect(). Write tests first.
        return codeNarcReport.packages.inject([]) { List results, Map pathReport ->
            pathReport.files?.each { Map fileReport ->
                String filePath = "${pathReport.path}/${fileReport.name}"
                println("Translating report for ${filePath}")

                fileReport.violations?.each { Map violation ->
                    results << new CodeNarcIssue(violation.ruleName, filePath, violation.priority, violation.lineNumber ?: 0, violation.message)
                }
            }

            return results
        }
    }

    private static Map readReport() {
        Map report = [:]
        if (doesFileExist(jsonCodeReportFileName)) {
            report = new JsonSlurper().parse(new File(jsonCodeReportFileName))
        } else {
            println("Report [$jsonCodeReportFileName] was not generated. Skipping...")
        }

        return report
    }

    private static boolean isAnyViolationFound(Map codeNarcReport) {
        int filesWithViolations = codeNarcReport?.summary?.filesWithViolations

        return filesWithViolations > 0
    }

    static boolean doesFileExist(String filePath) {
        return new File(filePath).exists()
    }
}
