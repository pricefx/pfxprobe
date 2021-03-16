package Utils

import Models.PfxProbeIssuePattern

class PatternDictionary {

    static List<PfxProbeIssuePattern> getPatternDictionary() {
        return [
                new PfxProbeIssuePattern(
                        "trace-in-repo",
                        "Don't commit traces to the repo",
                        /(?<!isDebugMode.*\s*)api\.trace/,
                        "minor",
                        "Style"
                ),
                new PfxProbeIssuePattern(
                        "logging-in-repo",
                        "Don't commit logging to the repo",
                        /(?<!isDebugMode.*\s*)api\.log/,
                        "major",
                        "Performance", "Style"
                ),
                new PfxProbeIssuePattern(
                        "lib-explicit-returns",
                        "Libs and methods should have explicit return types",
                        /def\s\w*\(.*\)/,
                        "major",
                        "Style"
                ),
                new PfxProbeIssuePattern(
                        "use-stream-util",
                        "Use libs.SharedLib.StreamUtils.stream for safe stream handling",
                        /api\.stream/,
                        "major",
                        "Bug Risk", "Performance", "Style"
                ),
                new PfxProbeIssuePattern(
                        "harcoded-attribute",
                        "HardCoded Attribute or Key, please use @Field final",
                        /(?<!@Field.*)(attribute|key)\d+/,
                        "major",
                        "Style"
                ),
                new PfxProbeIssuePattern(
                        "file-not-formatted",
                        "File is not auto-formatted. Please use Ctrl+Alt+L in intellij before committing changes",
                        /(?<!"""(?s).*)([\w'`)]=|=[\w'`(])((?!(?s).*"""))|(\)\{)(?s).*/,
                        "minor",
                        "Style"
                ),
                new PfxProbeIssuePattern(
                        "deprecated-getelement",
                        "PFX - api.getElement() is deprecated. Please use out.elementName",
                        /api\.getElement/,
                        "major",
                        "Style"
                ),
        ]
    }
}
