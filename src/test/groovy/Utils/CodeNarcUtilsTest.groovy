package Utils

import Models.CodeNarcIssue
import spock.lang.Specification

class CodeNarcUtilsTest extends Specification {

    def "translateRawReport translates empty report correctly"() {
        given:
        Map codeNarcReport = [
                "codeNarc": [
                        "url"    : "https://www.codenarc.org",
                        "version": "2.0.0"],
                "report"  : [
                        "timestamp": "Feb 3, 2021, 5:49:28 PM"],
                "project" : [
                        "title"            : null,
                        "sourceDirectories": [
                                "/CalculationLogic"
                        ]],
                "summary" : [
                        "totalFiles"         : 43,
                        "filesWithViolations": 0,
                        "priority1"          : 0,
                        "priority2"          : 0,
                        "priority3"          : 0],
                "packages": [
                        ["path"               : "somePath",
                         "totalFiles"         : 43,
                         "filesWithViolations": 0,
                         "priority1"          : 0,
                         "priority2"          : 0,
                         "priority3"          : 0,
                         "files"              : [
                         ]]]
        ]

        List<CodeNarcIssue> expected = []

        when:
        List<CodeNarcIssue> result = CodeNarcUtils.translateRawReport(codeNarcReport)

        then:
        result == expected
    }

    def "translateRawReport translates non-empty report correctly"() {
        given:
        Map codeNarcReport = [
                "codeNarc": [
                        "url"    : "https://www.codenarc.org",
                        "version": "2.0.0"],
                "report"  : [
                        "timestamp": "Feb 3, 2021, 5:49:28 PM"],
                "project" : [
                        "title"            : null,
                        "sourceDirectories": [
                                "/CalculationLogic"
                        ]],
                "summary" : [
                        "totalFiles"         : 43,
                        "filesWithViolations": 2,
                        "priority1"          : 0,
                        "priority2"          : 0,
                        "priority3"          : 2],
                "packages": [
                        ["path"               : "somePath",
                         "totalFiles"         : 43,
                         "filesWithViolations": 2,
                         "priority1"          : 0,
                         "priority2"          : 0,
                         "priority3"          : 2,
                         "files"              : [
                                 ["name"      : "ConfigManager.groovy",
                                  "violations": [["ruleName"  : "MethodSize",
                                                  "priority"  : 3,
                                                  "lineNumber": 20,
                                                  "message"   : "Method \"getInstance\" is 115 lines"]]],
                                 ["name"      : "StrategyInputValidation.groovy",
                                  "violations": [["ruleName"  : "NoDef",
                                                  "priority"  : 3,
                                                  "lineNumber": 3,
                                                  "sourceLine": "@Field ENGINE_VALIDATION_RULES = [",
                                                  "message"   : "def for declaration should not be used"]]],]
                        ]
                ]
        ]

        List<CodeNarcIssue> expected = [
                new CodeNarcIssue("MethodSize", "/CalculationLogic/somePath/ConfigManager.groovy", 3, 20, "Method \"getInstance\" is 115 lines"),
                new CodeNarcIssue("NoDef", "/CalculationLogic/somePath/StrategyInputValidation.groovy", 3, 3, "def for declaration should not be used")
        ]

        when:
        List<CodeNarcIssue> result = CodeNarcUtils.translateRawReport(codeNarcReport)

        then:
        result == expected
    }

    def "translateRawReport translates report with missing fileline correctly"() {
        given:
        Map codeNarcReport = [
                "codeNarc": [
                        "url"    : "https://www.codenarc.org",
                        "version": "2.0.0"],
                "report"  : [
                        "timestamp": "Feb 3, 2021, 5:49:28 PM"],
                "project" : [
                        "title"            : null,
                        "sourceDirectories": [
                                "/CalculationLogic"
                        ]],
                "summary" : [
                        "totalFiles"         : 43,
                        "filesWithViolations": 2,
                        "priority1"          : 0,
                        "priority2"          : 0,
                        "priority3"          : 2],
                "packages": [
                        ["path"               : "somePath",
                         "totalFiles"         : 43,
                         "filesWithViolations": 2,
                         "priority1"          : 0,
                         "priority2"          : 0,
                         "priority3"          : 2,
                         "files"              : [
                                 ["name"      : "ConfigManager.groovy",
                                  "violations": [["ruleName"  : "MethodSize",
                                                  "priority"  : 3,
                                                  "lineNumber": null,
                                                  "message"   : "Method \"getInstance\" is 115 lines"]]]]
                        ]
                ]
        ]

        List<CodeNarcIssue> expected = [
                new CodeNarcIssue("MethodSize", "/CalculationLogic/somePath/ConfigManager.groovy", 3, 0, "Method \"getInstance\" is 115 lines"),
        ]

        when:
        List<CodeNarcIssue> result = CodeNarcUtils.translateRawReport(codeNarcReport)

        then:
        result == expected
    }

    def "parseReport properly handles empty report"() {
        given:
        GroovySpy(CodeNarcUtils, global: true)
        CodeNarcUtils.readReport() >> { [:] }

        when:
        def result = CodeNarcUtils.parseReport()

        then:
        result == []
    }

    def "parseReport doesn't early escape when report exists"() {
        given:
        List dummyReport = ["dummyReport"]

        GroovySpy(CodeNarcUtils, global: true)
        CodeNarcUtils.readReport() >> { [summary: [filesWithViolations: 10]] }
        CodeNarcUtils.translateRawReport(_) >> { dummyReport }

        when:
        List result = CodeNarcUtils.parseReport()

        then:
        result == dummyReport
    }

    def "isAnyViolationFound"() {
        when:
        boolean result = CodeNarcUtils.isAnyViolationFound(codeNarcReport)

        then:
        result == expected

        where:
        codeNarcReport                         | expected
        [summary: [filesWithViolations: 10]]   | true
        [summary: [filesWithViolations: 1]]    | true
        [summary: [filesWithViolations: 0]]    | false
        [summary: [filesWithViolations: null]] | false
        [summary: [:]]                         | false
        [summary: null]                        | false
        [:]                                    | false
        null                                   | false
    }
}
