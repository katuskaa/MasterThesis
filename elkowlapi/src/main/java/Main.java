import base.Parser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import reasoner.ReasonerManager;

public class Main {

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();
        new Parser(args);
        ReasonerManager reasonerManager = new ReasonerManager();
        reasonerManager.initializeReasoner();
    }
}
