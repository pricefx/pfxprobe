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
                        "deprecated-getelement",
                        "PFX - api.getElement() is deprecated. Please use out.elementName instead",
                        /api\.getElement/,
                        "major",
                        "Style"
                ),
                new PfxProbeIssuePattern(
                        "deprecated-getinput",
                        "PFX - api.input() is deprecated. Please use input.inputName instead",
                        /api\.input/,
                        "major",
                        "Style"
                ),
                new PfxProbeIssuePattern(
                        "deprecated-list-add",
                        "PFX - Use 'list << value' groovy operator instead",
                        /\w*\.add\(/,
                        "minor",
                        "Style"
                ),
        ]
    }
}
