package olga.oskina.indexer;

import org.junit.Test;

public class RunIndexerTest {
    @Test
    public void runIndexer() {
        RunIndexer.main(new String[]{
                "--f",
                "./docs_1",
                "--i",
                "index.inv",
        });
    }
}