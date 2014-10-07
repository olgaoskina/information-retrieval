package olga.oskina.indexer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.nio.file.Paths;


/**
 * Created by olgaoskina
 * 07 October 2014.
 */
public class RunIndexer {

    @Parameter(names = {"-folder", "--f"}, required = true)
    String folder;

    @Parameter(names = {"-indexes", "--i"}, required = true)
    String fileWithIndexes;

    @Parameter(names = {"-help", "--h"}, help = true)
    Boolean help = false;

    public static void main(String[] args) {
        PropertyConfigurator.configure("log4g.properties");
        RunIndexer runIndexer = new RunIndexer();
        JCommander jCommander = new JCommander(runIndexer, args);
        if (runIndexer.help) {
            jCommander.usage();
        } else {
            runIndexer.work();
        }
    }

    public void work() {
        Logger logger = Logger.getLogger(this.getClass());
        logger.info("Start working");
        Indexer indexer = new Indexer(
                Paths.get(folder).toAbsolutePath(),
                Paths.get(fileWithIndexes).toAbsolutePath(),
                logger
        );
        indexer.work();
        logger.info("Finished successfully");
    }
}
