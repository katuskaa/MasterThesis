import algorithms.mergeXPlain.MergeXPlain;
import common.ArgumentParser;
import common.Loader;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main {

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        ArgumentParser argumentParser = new ArgumentParser();
        argumentParser.parse(args);

        Loader loader = new Loader();
        loader.initialize();

        MergeXPlain mergeXPlain = new MergeXPlain(loader);

    }
}
