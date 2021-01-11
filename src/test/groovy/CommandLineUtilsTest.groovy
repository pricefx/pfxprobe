import Utils.CommandLineUtils
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.UnrecognizedOptionException
import spock.lang.Specification

class CommandLineUtilsTest extends Specification {
    static String helpMessage = "usage: pfxprobe -dir <arg>${System.lineSeparator()} -dir <arg>   Directories to be scanned${System.lineSeparator()}"

    def "Command Line Help Message Hasn't Changed"() {
        when:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)

        and:
        CommandLineUtils.printInputArgsHelp()

        then:
        buffer.toString() == helpMessage
    }

    def "Throws Exception When Required Args Missing And Prints Help Message"() {
        when:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)
        String[] args = []
        CommandLineUtils.parseInputArgs(args)

        then:
        thrown(MissingOptionException)
        buffer.toString() == helpMessage

    }

    def "Throws Exception When UnRecognised Args Are Given And Prints Help Message"() {
        when:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)
        String[] args = ["-some", "other", "-params"]
        CommandLineUtils.parseInputArgs(args)

        then:
        thrown(UnrecognizedOptionException)
        buffer.toString() == helpMessage
    }

    def "Parser Proceeds When Supplied Required Args"() {
        when:
        String[] args = ["-dir", "ScanDirectory"]

        then:
        CommandLineUtils.parseInputArgs(args) != null
    }
}
