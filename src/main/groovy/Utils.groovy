import groovy.io.FileType
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.commons.cli.MissingArgumentException

import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher

class Utils {
    private static ConcurrentHashMap<File, Map<Integer, IntRange>> fileLinesCache = new ConcurrentHashMap<>()

    static Map<Integer, IntRange> getFileLines(File file) {
        if (!fileLinesCache[file]) {
            int charCount = 0
            int lineCount = 1
            Map<Integer, IntRange> fileLines = [:]
            file.eachLine { line ->
                if (charCount != 0) //if not first line
                    charCount++ // account for newLine char

                fileLines[lineCount] = charCount..(charCount + line.size())
                charCount += line.size()
                lineCount++
            }
            fileLinesCache[file] = fileLines
        }
        return fileLinesCache[file]
    }

    static String generateIssueFingerprint(String path, String name, String description) {
        byte[] b = [path, name, description].bytes.flatten()
        return MessageDigest.getInstance("MD5").digest(b).encodeHex().toString()
    }

    static List<File> getGroovyFilesInPath(String searchPath, List<String> searchExclusions = null) {
        File searchDir = new File(searchPath)
        List<File> allFiles = []
        searchDir.eachFileRecurse(FileType.FILES) { file ->
            if (isGroovyFile(file) && !matchesSearchExclusions(file, searchExclusions)) {
                allFiles << file
            }
        }
        return allFiles
    }

    static List<PfxCodeIssue> findCodeIssuesInFile(File file, PfxIssuePattern issuePattern) {
        List<PfxCodeIssue> allIssues = []
        Matcher matcher = file.text =~ issuePattern.Pattern
        matcher.results().each { match ->
            Integer lineBegin = getFileLines(file).find { it.value.fromInt <= match.start() && it.value.toInt >= match.start() }?.key
            Integer lineEnd = getFileLines(file).find { it.value.toInt >= match.end() }?.key

            if (lineBegin && lineEnd) {
                allIssues << new PfxCodeIssue(issuePattern, file.path, lineBegin, lineEnd, match.start(), match.end())
            }
        }
        return allIssues
    }

    private static Boolean isGroovyFile(File file) {
        return file.name.endsWith(".groovy")
    }

    private static Boolean matchesSearchExclusions(File file, List<String> searchExclusions = null) {
        return searchExclusions?.contains(file.canonicalPath)
    }

    static HashMap<String, List<String>> parseInputArgs(String... args) {
        HashMap<String, List<String>> executionParams = [:]
        String currentKey = null
        boolean expectingKey = true
        for (arg in args) {
            if (expectingKey) {
                if (!arg.startsWith("-")) {
                    throw new IllegalArgumentException("Malformed Args, Expected Key But Found $arg")
                } else {
                    currentKey = arg.substring(1, arg.length())
                    if (!executionParams.containsKey(currentKey)) {
                        executionParams[currentKey] = []
                    }
                }
            } else {
                executionParams[currentKey] << arg
            }

            expectingKey = !expectingKey
        }

        if (!executionParams.containsKey("from")) {
            throw new MissingArgumentException("Application requires at least one use of -from argument to know where to scan")
        }
        return executionParams
    }

    static void printIssueDiscoveriesToConsole(List<PfxCodeIssue> codeIssues) {
        Map<String, Integer> severityCounts = [:]
        for (issue in codeIssues)
            severityCounts[issue.IssuePattern.Severity] = (severityCounts[issue.IssuePattern.Severity] ?: 0) + 1

        println "Found a total of ${codeIssues.size()} issues, ${severityCounts.toString()}"
    }

    static void writeCodeClimateReport(List<PfxCodeIssue> allIssues) {
        Boolean isDevEnv = new File(".idea").isDirectory()

        // dont write json report if idea folder (dev environment) exists or if there are no issues found
        if (!isDevEnv && allIssues.size() > 0) {
            JsonSlurper jsonReader = new JsonSlurper();
            File codeClimateReport = new File("codeclimate.json")

            // Read codeclimate file, add to end of allIssues list and overwrite file with all results
            println("Appending Issues to CodeClimate Report")

            List<Map> codeClimateIssues = codeClimateReport?.size() > 0 ? jsonReader.parse(codeClimateReport) as List<Map> : []
            allIssues.addAll codeClimateIssues
            codeClimateReport.setText(new JsonBuilder(allIssues).toString().replace("},{", "},\n{"))
        }
    }
}