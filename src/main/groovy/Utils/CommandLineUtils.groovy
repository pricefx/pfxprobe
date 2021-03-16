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
    final static String probeAnalysisDesc = "Execute pfxprobe analysis"

    final static String scanDirArg = "dir"
    final static String scanDirDesc = "Directories to be scanned. CodeNarc analysis will run only on first one"

    final static String narcAnalysisArg = "n"
    final static String narcAnalysisDesc = "Execute CodeNarc analysis"

    final static String narcRulesFileArg = "rulefile"
    final static String narcRulesFileDesc = "Path to ruleset file relative to project directory. By default Accelerators team ruleset is used. Custom configurations can be created using codenarc.ruleset file as a template"


    private static Options getParserOptions() {
        Option probeAnalysis = new Option(probeAnalysisArg, false, probeAnalysisDesc)
        Option scanDir = new Option(scanDirArg, true, scanDirDesc)
        scanDir.setRequired(true)
        Option narcAnalysis = new Option(narcAnalysisArg, false, narcAnalysisDesc)
        Option narcRulesFile = new Option(narcRulesFileArg, true, narcRulesFileDesc)

        return new Options().addOption(probeAnalysis)
                .addOption(scanDir)
                .addOption(narcAnalysis)
                .addOption(narcRulesFile)
    }

    static void printInputArgsHelp() {
        HelpFormatter helpFormatter = new HelpFormatter()
        String header = "By default, when -$probeAnalysisArg or -$narcAnalysisArg parameters are not provided, both analysis types are executed. "
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

    static boolean shouldRunProbeAnalysis(CommandLine cmd) {
        boolean hasProbeOption = cmd.hasOption(probeAnalysisArg)
        boolean hasNarcOption = cmd.hasOption(narcAnalysisArg)

        boolean isRunAsDefault = !hasProbeOption && !hasNarcOption

        return isRunAsDefault || hasProbeOption
    }

    static boolean shouldRunNarcAnalysis(CommandLine cmd) {
        boolean hasProbeOption = cmd.hasOption(probeAnalysisArg)
        boolean hasNarcOption = cmd.hasOption(narcAnalysisArg)

        boolean isRunAsDefault = !hasProbeOption && !hasNarcOption

        return isRunAsDefault || hasNarcOption
    }
}
