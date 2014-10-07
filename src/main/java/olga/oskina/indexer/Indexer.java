package olga.oskina.indexer;

import olga.oskina.index.InvertedIndex;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.morphology.russian.RussianAnalyzer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by olgaoskina
 * 07 October 2014.
 */
public class Indexer {
    private Logger logger;
    private int indexOfFile = 0;
    private InvertedIndex invertedIndex;
    private File folder;

    public Indexer(File folder, File outputFile, Logger logger) {
        this.logger = logger;
        this.folder = folder;

        if (!(folder.exists() && folder.isDirectory())) {
            throw new IllegalArgumentException("Directory doesn't exist");
        }
        invertedIndex = new InvertedIndex(outputFile);
    }

    public void work() {
        readAndTokenizeFiles(folder);
        invertedIndex.write();
        invertedIndex.closeReaderAndWriter();
    }

    private void readAndTokenizeFiles(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    readAndTokenizeFiles(file);
                } else {
                    indexOfFile++;
                    long time = System.currentTimeMillis();
                    tokenizeFile(file, indexOfFile);
                    long workingTime = System.currentTimeMillis() - time;
                    logger.info("[TIME]: " + convertToValidTime(workingTime) + " [FILE]: " + file.getPath());
                    invertedIndex.appendFileWithIndex(file.getPath(), indexOfFile);
                }
            }
        }
    }

    private String convertToValidTime(long millis) {
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        return String.format("%d min, %d sec, %d millis",
                minutes,
                seconds - TimeUnit.MINUTES.toSeconds(minutes),
                millis -
                        TimeUnit.SECONDS.toMillis(
                                seconds - TimeUnit.MINUTES.toSeconds(minutes)
                        )
        );
    }

    private void tokenizeFile(File file, int fileIndex) {
        RussianAnalyzer russian = null;
        TokenStream tokenStream = null;
        try {
            russian = new RussianAnalyzer();
            tokenStream = russian.tokenStream(
                    file.getName(),
                    new FileReader(file)
            );
            while (tokenStream.incrementToken()) {
                String term = tokenStream.getAttribute(TermAttribute.class).term();
                invertedIndex.appendTermWithIndex(term, fileIndex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (russian != null) {
                    russian.close();
                }
                if (tokenStream != null) {
                    tokenStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
