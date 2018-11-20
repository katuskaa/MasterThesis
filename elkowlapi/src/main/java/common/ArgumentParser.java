package common;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


public class ArgumentParser {

    @Option(name = "-f", aliases = "--file", required = true)
    private String fileName;

    @Option(name = "-o", aliases = "--observation", required = true)
    private String observation;

    public void parse(String[] args) {
        CmdLineParser cmdLineParser = new CmdLineParser(this);

        try {
            cmdLineParser.parseArgument(args);

            Configuration.INPUT_FILE = fileName;
            Configuration.OBSERVATION = observation;

        } catch (CmdLineException exception) {
            exception.printStackTrace();
        }
    }

}
