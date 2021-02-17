package Utils

import Models.CodeNarcIssue
import groovy.json.JsonSlurper
import org.codenarc.CodeNarc

class CodeNarcUtils {

    final static String defaultRulesFileRelativePath = "codenarc.ruleset"
    final static String jsonCodeReportFileName = 'CodeNarcCodeJsonReport.json'
    final static String jsonTestsReportFileName = 'CodeNarcTestsJsonReport.json'

    static List<CodeNarcIssue> getCodeNarcIssues(String userRulesFileRelativePath) {
        println("Starting codeNarc analysis")
        runAnalysis(userRulesFileRelativePath)

        ArrayList<CodeNarcIssue> codeNarcCodeIssues = parseReport(jsonCodeReportFileName) ?: []
        ArrayList<CodeNarcIssue> codeNarcTestsIssues = parseReport(jsonTestsReportFileName) ?: []

        return codeNarcCodeIssues + codeNarcTestsIssues
    }

    /**
     * We assume that image runs in repository with PFX Studio file structure
     * @param userRulesFileRelativePath
     */
    private static void runAnalysis(String userRulesFileRelativePath) {
        String rulesFileRelativePath = userRulesFileRelativePath ?: defaultRulesFileRelativePath

        // Because the workdir of the image can be /, it tries to check each and every file in the image which takes really long time.
        // This is why report from code and from tests is generated independently.
        safeExecuteAnalysisForDir(rulesFileRelativePath, './CalculationLogic')
        safeExecuteAnalysisForDir(rulesFileRelativePath, './CalculationLogicTest')
    }

    private static void safeExecuteAnalysisForDir(String rulesFileRelativePath, String baseDir) {
        try {
            if (doesFileExist(baseDir)) {
                // CodeNarc throws System.exit() if the basedir cannot be found, so we must make sure it's there
                CodeNarc.main("-rulesetfiles=file:$rulesFileRelativePath", "-basedir=$baseDir", "-report=json:$jsonCodeReportFileName")
            } else {
                println("Basedir [$baseDir] doesn't exist in the workspace")
            }
        } catch (any) {
            println("There was a problem with generating report for [$baseDir] directory. Skipping analysis...")
            any.printStackTrace()
        }
    }

    // TODO - tests
    private static List<CodeNarcIssue> parseReport(String fileName) {
        Map codeNarcReport = readReport(fileName)
        println("Report [$fileName] parsed")

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
                    results << new CodeNarcIssue(violation.ruleName, filePath, violation.priority, violation.lineNumber, violation.message)
                }
            }

            return results
        }
    }

    private static Map readReport(String fileName) {
        Map report = [:]
        if (doesFileExist(fileName)) {
            report = new JsonSlurper().parse(new File(fileName))
        } else {
            println("Report [$fileName] was not generated. Skipping...")
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
