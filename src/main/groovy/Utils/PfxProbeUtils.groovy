package Utils

import Models.PfxProbeIssue

import java.util.concurrent.CopyOnWriteArrayList

class PfxProbeUtils {

    static List<PfxProbeIssue> getPfxProbeIssues(String[] scanDirs) {
        CopyOnWriteArrayList<PfxProbeIssue> pfxProbeIssues = []

        scanDirs.toList().parallelStream().forEach { String dirPath ->
            def issuePatterns = PatternDictionary.getPatternDictionary()
            def groovyFiles = FileUtils.getGroovyFilesInPath(dirPath)

            println("pfxprobe Scanning Directory $dirPath")
            groovyFiles.parallelStream().forEach { file ->
                issuePatterns.parallelStream().forEach { issuePattern ->
                    pfxProbeIssues.addAll(issuePattern.findOccurrencesInFile(file))
                }
            }
        }

        return pfxProbeIssues
    }
}
