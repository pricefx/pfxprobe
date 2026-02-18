package Utils

import Models.PfxProbeIssue

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.stream.Collectors

class PfxProbeUtils {

    static List<PfxProbeIssue> getPfxProbeIssues(String[] scanDirs) {
        def issuePatterns = PatternDictionary.getPatternDictionary()
        ConcurrentLinkedQueue<PfxProbeIssue> pfxProbeIssues = new ConcurrentLinkedQueue<>()

        List<File> groovyFiles = scanDirs.toList().parallelStream().flatMap { String dirPath ->
            println("pfxprobe Scanning Directory $dirPath")
            return FileUtils.getGroovyFilesInPath(dirPath).stream()
        }.collect(Collectors.toList())

        groovyFiles.parallelStream().forEach { File file ->
            String fileContent = file.text
            Map<Integer, IntRange> fileLines = FileUtils.getFileLines(file)

            issuePatterns.each { issuePattern ->
                pfxProbeIssues.addAll(issuePattern.findOccurrencesInFile(file, fileContent, fileLines))
            }
        }

        return pfxProbeIssues.toList()
    }
}
