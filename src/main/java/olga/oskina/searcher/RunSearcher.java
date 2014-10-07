package olga.oskina.searcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by olgaoskina
 * 07 October 2014.
 */
public class RunSearcher {

    @Parameter(names = {"-indexes", "--i"}, required = true)
    String fileWithIndexes;

    @Parameter(names = {"-help", "--h"}, help = true)
    Boolean help = false;

    public static void main(String[] args) {
        RunSearcher runIndexer = new RunSearcher();
        JCommander jCommander = new JCommander(runIndexer, args);
        if (runIndexer.help) {
            jCommander.usage();
        } else {
            runIndexer.work();
        }
    }

    public void work() {
        Scanner scanner = new Scanner(System.in);
        Searcher searcher;
        searcher = new Searcher(Paths.get(fileWithIndexes));
        while (scanner.hasNext()) {
            String request = scanner.nextLine();
            System.out.println("\t" + searcher.find(request));
        }
    }
}
