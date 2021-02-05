package Utils

import Models.CodeNarcIssue
import groovy.json.JsonSlurper
import org.codenarc.CodeNarc

class CodeNarcUtils {

    static String defaultRulesFileRelativePath = "codenarc.ruleset"
    static String jsonCodeReportFileName = 'CodeNarcCodeJsonReport.json'
    static String jsonTestsReportFileName = 'CodeNarcTestsJsonReport.json'

    static ArrayList<CodeNarcIssue> getCodeNarcIssues(String userRulesFileRelativePath) {
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

        // Because the workdir of the image is /, it tries to check each and every file in the image which takes really long time.
        // This is why report from code and from tests is generated independently.
        CodeNarc.main("-rulesetfiles=file:$rulesFileRelativePath", '-basedir=/CalculationLogic', "-report=json:$jsonCodeReportFileName")
        CodeNarc.main("-rulesetfiles=file:$rulesFileRelativePath", '-basedir=/CalculationLogicTest', "-report=json:$jsonTestsReportFileName")
    }

    // TODO - tests
    private static ArrayList<CodeNarcIssue> parseReport(String fileName) {
        Map codeNarcReport = readReport(fileName)
        println("Report parsed")

        if (!isAnyViolationFound(codeNarcReport)) {
            println("No violations found")
            return []
        }

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
        return new JsonSlurper().parse(new File(fileName))
    }

    private static boolean isAnyViolationFound(Map codeNarcReport) {
        int filesWithViolations = codeNarcReport?.summary?.filesWithViolations

        return filesWithViolations > 0
    }
}
