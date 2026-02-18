package Utils

import java.util.regex.Matcher

class ApplicationMetadata {
    final static String groupId = "net.pricefx.tools"
    final static String artifactId = "pfxprobe"
    final static String informationUri = "https://gitlab.pricefx.eu/public-tools/pfxprobe"
    final static String sarifRuleHelpUri = "https://codenarc.org/codenarc-rule-index.html"

    private static String resolvedVersion

    static String getVersion() {
        if (resolvedVersion) {
            return resolvedVersion
        }

        String version = readVersionFromPomProperties() ?: readVersionFromPomFile() ?: "unknown"
        resolvedVersion = version

        return resolvedVersion
    }

    private static String readVersionFromPomProperties() {
        String resourcePath = "/META-INF/maven/${groupId}/${artifactId}/pom.properties"
        InputStream stream = ApplicationMetadata.class.getResourceAsStream(resourcePath)

        if (!stream) {
            return null
        }

        Properties properties = new Properties()
        stream.withCloseable { properties.load(it) }

        return properties.getProperty("version")
    }

    private static String readVersionFromPomFile() {
        File pomFile = new File(System.getProperty("user.dir"), "pom.xml")

        if (!pomFile.exists()) {
            return null
        }

        String pomContent = pomFile.text
        Matcher versionMatcher = pomContent =~ /<artifactId>${artifactId}<\/artifactId>[\s\S]*?<version>\s*([^<\s]+)\s*<\/version>/

        if (!versionMatcher.find()) {
            return null
        }

        return versionMatcher.group(1)
    }
}
