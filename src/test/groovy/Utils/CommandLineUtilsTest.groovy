package Utils

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.UnrecognizedOptionException
import spock.lang.Specification

class CommandLineUtilsTest extends Specification {
    static String helpMessage = """usage: pfxprobe -dir <arg> [-n] [-p] [-rulefile <arg>]${System.lineSeparator()}\
By default, when -p or -n parameters are not provided, both analysis types${System.lineSeparator()}\
are executed.${System.lineSeparator()}\
 -dir <arg>        Directories to be scanned. CodeNarc analysis will run${System.lineSeparator()}\
                   only on first one${System.lineSeparator()}\
 -n                Execute CodeNarc analysis${System.lineSeparator()}\
 -p                Execute pfxprobe analysis${System.lineSeparator()}\
 -rulefile <arg>   Path to ruleset file relative to project directory. By${System.lineSeparator()}\
                   default Accelerators team ruleset is used. Custom${System.lineSeparator()}\
                   configurations can be created using codenarc.ruleset${System.lineSeparator()}\
                   file as a template${System.lineSeparator()}\
"""

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
        def result = CommandLineUtils.parseInputArgs(args)

        then:
        result != null

        where:
        args << [["-dir", "ScanDirectory"] as String[],
                 ["-dir", "ScanDirectory", "-n", "-p", "-rulefile", "/codenarc.ruleset"] as String[]]
    }

    def "Probe Analysis Is Triggered Under Proper Conditions"() {
        when:
        CommandLine cmd = Mock(CommandLine)
        cmd.hasOption("p") >> probeOption
        cmd.hasOption("n") >> narcOption

        and:
        boolean result = CommandLineUtils.shouldRunProbeAnalysis(cmd)

        then:
        result == expected

        where:
        probeOption | narcOption | expected
        true        | true       | true
        false       | false      | true
        false       | true       | false
        true        | false      | true
    }

    def "Narc Analysis Is Triggered Under Proper Conditions"() {
        when:
        CommandLine cmd = Mock(CommandLine)
        cmd.hasOption("p") >> probeOption
        cmd.hasOption("n") >> narcOption

        and:
        boolean result = CommandLineUtils.shouldRunNarcAnalysis(cmd)

        then:
        result == expected

        where:
        probeOption | narcOption | expected
        true        | true       | true
        false       | false      | true
        false       | true       | true
        true        | false      | false
    }
}
