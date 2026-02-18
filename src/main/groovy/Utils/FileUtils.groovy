package Utils
import groovy.io.FileType
import java.util.concurrent.ConcurrentHashMap

class FileUtils {
    private static ConcurrentHashMap<File, Map<Integer, IntRange>> fileLinesCache = new ConcurrentHashMap<>()

    static Map<Integer, IntRange> getFileLines(File file) {
        return fileLinesCache.computeIfAbsent(file) { File cachedFile ->
            int charCount = 0
            int lineCount = 1
            Map<Integer, IntRange> fileLines = [:]
            cachedFile.eachLine { line ->
                if (charCount != 0) {
                    charCount++
                }

                fileLines[lineCount] = charCount..(charCount + line.size())
                charCount += line.size()
                lineCount++
            }

            return fileLines
        }
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
}
