import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException

class CommandLineUtils {
    static String scanDirArg = "dir"
    private static Options getParserOptions(){
        Options options = new Options();
        Option fromInput = new Option(scanDirArg, true, "Directories to be scanned")
        fromInput.setRequired(true)
        options.addOption(fromInput)

        return options
    }

    static void printInputArgsHelp(){
        HelpFormatter helpFormatter = new HelpFormatter()
        String header = ""
        String footer = ""
        helpFormatter.printHelp("pfxnarc", header, getParserOptions(), footer, true)
    }

    static CommandLine parseInputArgs(String... args) throws ParseException, MissingOptionException {
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(getParserOptions(), args)
        }
        catch(Exception e){
            printInputArgsHelp()
            throw e
        }
    }

}
