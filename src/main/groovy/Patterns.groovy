class Patterns {

    static List<PfxIssuePattern> getPatternDictionary() {
        return [
                new PfxIssuePattern(
                        "trace-in-repo",
                        "Don't commit traces to the repo",
                        /(?<!isDebugMode.*\s*)api\.trace/,
                        "minor",
                        "Style"
                ),
                new PfxIssuePattern(
                        "logging-in-repo",
                        "Don't commit logging to the repo",
                        /(?<!isDebugMode.*\s*)api\.log/,
                        "major",
                        "Performance", "Style"
                ),
                new PfxIssuePattern(
                        "deprecated-getelement",
                        "PFX - api.getElement() is deprecated. Please use out.elementName",
                        /api\.getElement/,
                        "major",
                        "Style"
                ),
        ]
    }
}
