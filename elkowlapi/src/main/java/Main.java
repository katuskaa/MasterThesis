import algorithms.ISolver;
import algorithms.mergeXPlain.MergeXPlainSolver;
import common.ArgumentParser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import reasoner.*;

public class Main {

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        ArgumentParser argumentParser = new ArgumentParser();
        argumentParser.parse(args);

        ILoader loader = new Loader();
        loader.initialize(ReasonerType.HERMIT);

        IReasonerManager reasonerManager = new ReasonerManager(loader);

        ISolver mergeXPlainSolver = new MergeXPlainSolver();
        mergeXPlainSolver.solve(loader, reasonerManager);
    }
}
