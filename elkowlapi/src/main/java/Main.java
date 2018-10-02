import common.Loader;
import common.Parser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main {

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        new Parser(args);
        Loader loader = new Loader();
        loader.initialize();

//        TreeBuilder treeBuilder = new TreeBuilder(loader.getKnowledgeBase(), loader.getNegObservation());
//        treeBuilder.build();
    }
}
