import groovy.io.FileType
import groovy.transform.Field
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.regex.Matcher
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

@Field final String scanDirectory = "/code"
@Field final String failureSeverity = "blocker"
@Field final String[] scanExcludePatterns = [
        "**/CalculationLogicTest/**",
        "**/Modules/SharedLib/**",
]
@Field final String[] severityImportance = [
        "info",
        "minor",
        "major",
        "critical",
        "blocker"
]

JsonSlurper jsonReader = new JsonSlurper();

List<Map> issuePatterns = [
        [
                name       : "trace-in-repo",
                description: "PFX - Don't commit traces to the repo",
                pattern    : /(?<!isDebugMode.*\s*)api\.trace/,
                severity   : "minor",
                categories : ["Style"],
                exclude    : [
                        "**/_secore/elements/logger.groovy"
                ]
        ],
        [
                name       : "logging-in-repo",
                description: "PFX - Don't commit logging to the repo",
                pattern    : /(?<!isDebugMode.*\s*)api\.log/,
                severity   : "critical",
                categories : ["Performance", "Style"],
                exclude    : [
                        "**/_secore/elements/logger.groovy"
                ]
        ],
        [
                name       : "lib-explicit-returns",
                description: "PFX - Libs and methods should have explicit return types",
                pattern    : /def\s\w*\(.*\)/,
                severity   : "major",
                categories : ["Style"],
                exclude    : []
        ],
        [
                name       : "method-signature-explicit-type",
                description: "PFX - Method signature should be explicitly typed",
                pattern    : /(?<=\w\(.*)def\s\w+(?=.*\))/,
                severity   : "major",
                categories : ["Style"],
                exclude    : []
        ],
        [
                name       : "use-stream-util",
                description: "PFX - Use libs.SharedLib.StreamUtils.stream for safe stream handling",
                pattern    : /api\.stream/,
                severity   : "critical",
                categories : ["Bug Risk", "Performance", "Style"],
                exclude    : []
        ],
        [
                name       : "harcoded-attribute",
                description: "PFX - HardCoded Attribute or Key, please use @Field final",
                pattern    : /(?<!@Field.*)(attribute|key)\d+/,
                severity   : "major",
                categories : ["Style"],
                exclude    : []
        ],
        [
                name       : "file-not-formatted",
                description: "File is not auto-formatted. Please use Ctrl+Alt+L in intellij before committing changes",
                pattern    : /(?<!"""(?s).*)([\w'`)]=|=[\w'`(])((?!(?s).*"""))|(\)\{)(?s).*/,
                severity   : "minor",
                categories : ["Style"],
                exclude    : []
        ],
        [
                name       : "deprecated-getelement",
                description: "PFX - api.getElement() is deprecated. Please use out.elementName",
                pattern    : /api\.getElement/,
                severity   : "major",
                categories : ["Style"],
                exclude    : []
        ],
]

Boolean isDevEnv = new File(".idea").isDirectory()
File dir = new File(scanDirectory)
println("Scanning Directory: $dir.path")
FileNameFinder fileFinder = new FileNameFinder()

CopyOnWriteArrayList<Map> allIssues = new CopyOnWriteArrayList<>()
HashSet<String> excludedFiles = []

// Build global and issue specific excluded file lists
scanExcludePatterns.each { pattern ->
    excludedFiles.addAll(fileFinder.getFileNames(scanDirectory, pattern))
}

issuePatterns.forEach { issue ->
    HashSet<String> issueExcludedFiles = []
    issue.exclude.each { pattern ->
        issueExcludedFiles.addAll(fileFinder.getFileNames(scanDirectory, pattern))
    }
    issue.excludedFiles = issueExcludedFiles
}


// FILES
List<File> allFiles = []
dir.eachFileRecurse(FileType.FILES) { file ->
    println("scanning $file.name")
    if (!excludedFiles.contains(file.canonicalPath)) {
        allFiles << file
    }
}

println("Scanning files with pfxnarc...")
allFiles.parallelStream().forEach({ file ->
    if (file.name.endsWith(".groovy")) {
        issuePatterns.parallelStream().forEach({ issuePattern ->
            if (!issuePattern.excludedFiles?.any { it == file.canonicalPath }) {
                Matcher matcher = file.text =~ issuePattern.pattern
                matcher.results().each { match ->
                    Integer lineBegin = getFileLines(file).find { it.value.fromInt <= match.start() && it.value.toInt >= match.start() }?.key
                    Integer lineEnd = getFileLines(file).find { it.value.toInt >= match.end() }?.key
                    def issue = [
                            type       : "issue",
                            engine_name: "pfxnarc",
                            check_name : issuePattern.name,
                            description: "[$issuePattern.severity] $issuePattern.description",
                            severity   : issuePattern.severity,
                            categories : issuePattern.categories,
                            location   : [
                                    path : file.path,
                                    lines: [begin: lineBegin, end: lineEnd],
                                    chars: [begin: match.start(), end: match.end()]
                            ],
                            fingerprint: generateMD5(file.path, issuePattern.name, issuePattern.description)
                    ]
                    allIssues << issue
                    if (isDevEnv)
                        println("$issue.description at path $file.path:${issue.location.lines.begin}")
                }
            }
        })
    }
})

//sort found issues by highest severity
allIssues.sort { it.description }.sort { a, b -> severityImportance.findIndexOf { it == b.severity } <=> severityImportance.findIndexOf { it == a.severity } }
Map<String, Integer> severityCounts = [:]
for (issue in allIssues)
    severityCounts[issue.severity] = (severityCounts[issue.severity] ?: 0) + 1
println "Found a total of ${allIssues.size()} issues, ${severityCounts.toString()}"

// TODO: FOLDERS

// dont write json report if idea folder (dev environment) exists or if there are no issues found
if (!isDevEnv && allIssues.size() > 0) {
    // Read codeclimate file, add to end of allIssues list and overwrite file with all results
    File codeClimateReport = new File("codeclimate.json")
    List<Map> codeClimateIssues = codeClimateReport?.size() > 0 ? jsonReader.parse(codeClimateReport) as List<Map> : []
    allIssues.addAll codeClimateIssues
    codeClimateReport.setText(new JsonBuilder(allIssues).toString().replace("},{", "},\n{"))
}

// Check if failure severities are defined and fail job if matching issues are found
if (allIssues.any { issue -> severityImportance.findIndexOf { it == issue.severity } >= severityImportance.findIndexOf { it == failureSeverity } })
    throw new Exception("Found issue(s) >= $failureSeverity severity")


// Utils ===============================================================================================================

@Field final ConcurrentHashMap<File, Map<Integer, IntRange>> fileLinesCache = new ConcurrentHashMap<>()

Map<Integer, IntRange> getFileLines(File file) {
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

String generateMD5(String path, String name, String description) {
    byte[] b = [path, name, description].bytes.flatten()
    return MessageDigest.getInstance("MD5").digest(b).encodeHex().toString()
}
