package Utils

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException

class CommandLineUtils {
    final static String probeAnalysisArg = "p"
    final static String probeScanDirArg = "dir"
    final static String narcAnalysisArg = "n"
    final static String narcRulesFileArg = "rulefile"

    private static Options getParserOptions() {
        Option probeAnalysis = new Option(probeAnalysisArg, false, "Execute pfxprobe analysis")
        Option scanDir = new Option(probeScanDirArg, true, "Directories to be scanned by pfxprobe rules")
        Option narcAnalysis = new Option(narcAnalysisArg, false, "Execute CodeNarc analysis")
        Option narcRulesFile = new Option(narcRulesFileArg, true, "(Optional) Relative path to ruleset file. If not passed, default Accelerator ruleset will be used")

        return new Options().addOption(probeAnalysis)
                .addOption(scanDir)
                .addOption(narcAnalysis)
                .addOption(narcRulesFile)
    }

    static void printInputArgsHelp() {
        HelpFormatter helpFormatter = new HelpFormatter()
        String header = ""
        String footer = ""
        helpFormatter.printHelp("pfxprobe", header, getParserOptions(), footer, true)
    }

    static CommandLine parseInputArgs(String... args) throws ParseException, MissingOptionException {
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(getParserOptions(), args)
        }
        catch (Exception e) {
            printInputArgsHelp()
            throw e
        }
    }
}
