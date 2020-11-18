import groovy.io.FileType
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.commons.cli.BasicParser
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.MissingArgumentException
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException

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
        String filePath = getUnixStyleFilePath(file.path)
        Boolean matchFound = false
        for (regexp in searchExclusions) {
            if (filePath == regexp || filePath ==~ regexp) {
                matchFound = true
                break
            }
        }
        return matchFound
    }

    static String getUnixStyleFilePath(String filePath) {
        return File.separator == "\\" ? filePath.replace("\\", "/") : filePath
    }

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

        List<Map> allIssuesFormatted = pfxCodeIssues.collect { it.getCodeClimateIssueFormat() }
        List<Map> codeClimateIssues = codeClimateReport?.size() > 0 ? jsonReader.parse(codeClimateReport) as List<Map> : []
        allIssuesFormatted.addAll(codeClimateIssues)

        codeClimateReport.setText(new JsonBuilder(allIssuesFormatted).toString().replace("},{", "},\n{"))
    }
}