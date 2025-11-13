package Utils

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.UnrecognizedOptionException
import spock.lang.Specification

class CommandLineUtilsTest extends Specification {

    def "Help Message Contains All Required Options"() {
        when:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)

        and:
        CommandLineUtils.printInputArgsHelp()

        then:
        def helpText = buffer.toString()
        helpText.contains("usage: pfxprobe")
        helpText.contains("-dir")
        helpText.contains("-n")
        helpText.contains("-p")
        helpText.contains("-rulefile")
        helpText.contains("-qualitygate")
        helpText.contains("Directories to be scanned")
        helpText.contains("Execute CodeNarc analysis")
        helpText.contains("Execute pfxprobe analysis")
        helpText.contains("quality gate mode")
    }

    def "Throws Exception When Required Args Missing And Prints Help Message"() {
        when:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)
        String[] args = []
        CommandLineUtils.parseInputArgs(args)

        then:
        thrown(MissingOptionException)
        def helpText = buffer.toString()
        helpText.contains("usage: pfxprobe")
        helpText.contains("-dir")
    }

    def "Throws Exception When UnRecognised Args Are Given And Prints Help Message"() {
        when:
        def buffer = new ByteArrayOutputStream()
        System.out = new PrintStream(buffer)
        String[] args = ["-some", "other", "-params"]
        CommandLineUtils.parseInputArgs(args)

        then:
        thrown(UnrecognizedOptionException)
        def helpText = buffer.toString()
        helpText.contains("usage: pfxprobe")
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

    def "Quality Gate Option Can Be Parsed Without Value"() {
        when:
        def result = CommandLineUtils.parseInputArgs(["-dir", "ScanDirectory", "-qualitygate"] as String[])

        then:
        result != null
        result.hasOption("qualitygate")
        result.getOptionValue("qualitygate") == null
    }

    def "Quality Gate Option Can Be Parsed With Severity Level"() {
        when:
        def result = CommandLineUtils.parseInputArgs(args)

        then:
        result != null
        result.hasOption("qualitygate")
        result.getOptionValue("qualitygate") == expectedValue

        where:
        args                                                                           | expectedValue
        ["-dir", "ScanDirectory", "-qualitygate", "info"] as String[]                | "info"
        ["-dir", "ScanDirectory", "-qualitygate", "minor"] as String[]               | "minor"
        ["-dir", "ScanDirectory", "-qualitygate", "major"] as String[]               | "major"
        ["-dir", "ScanDirectory", "-qualitygate", "critical"] as String[]            | "critical"
        ["-dir", "ScanDirectory", "-qualitygate", "blocker"] as String[]             | "blocker"
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
