package olga.oskina.index;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by olgaoskina
 * 07 October 2014.
 */
public class InvertedIndex {
    private HashMap<String, List<Integer>> invertedIndex;
    private HashMap<Integer, String> indexesOfFiles;
    private BufferedWriter fileWriter = null;
    private BufferedReader fileReader = null;
    private File fileToWriteOrRead = null;

    public InvertedIndex(File fileToWriteOrRead) {
        this.fileToWriteOrRead = fileToWriteOrRead;
        invertedIndex = new HashMap<String, List<Integer>>();
        indexesOfFiles = new HashMap<Integer, String>();
    }

    public void write() {
        try {
            fileWriter = new BufferedWriter(new FileWriter(fileToWriteOrRead));
            fileWriter.append(String.valueOf(indexesOfFiles.size()));
            fileWriter.newLine();
            for (Map.Entry<Integer, String> entry : indexesOfFiles.entrySet()) {
                Integer fileKey = entry.getKey();
                String fileName = entry.getValue();

                fileWriter
                        .append(fileName)
                        .append(":")
                        .append(fileKey.toString());
                fileWriter.newLine();
            }

            for (Map.Entry<String, List<Integer>> entry : invertedIndex.entrySet()) {
                String word = entry.getKey();
                List<Integer> files = entry.getValue();

                fileWriter
                        .append(word)
                        .append(":");
                for (Integer fileIndex : files) {
                    fileWriter
                            .append(fileIndex.toString())
                            .append(" ");
                }
                fileWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read() {
        try {
            fileReader = new BufferedReader(new FileReader(fileToWriteOrRead));
            int countOfFiles = Integer.valueOf(fileReader.readLine());
            int i = 0;
            String line;
            while ((line = fileReader.readLine()) != null) {
                if (i < countOfFiles) {
                    splitFile(line);
                } else {
                    splitTerm(line);
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendFileWithIndex(String fileName, int fileIndex) {
        String fileNameFromHash = indexesOfFiles.get(fileIndex);
        if (fileNameFromHash == null) {
            indexesOfFiles.put(fileIndex, fileName);
        } else {
            if (!fileNameFromHash.equals(fileName)) {
                throw new IllegalStateException("Names must be the same");
            }
        }
    }

    public void appendTermWithIndex(String term, int fileIndex) {
        List<Integer> tokenIndexes = invertedIndex.get(term);
        if (tokenIndexes != null) {
            int lastValue = tokenIndexes.get(tokenIndexes.size() - 1);
            if (lastValue != fileIndex) {
                tokenIndexes.add(fileIndex);
            }
        } else {
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(fileIndex);
            invertedIndex.put(term, list);
        }
    }

    /*
    * Empty if null
    * */
    public List<Integer> getFilesIndexesByTerm(String term) {
        List<Integer> filesIndexes = invertedIndex.get(term);
        if (filesIndexes == null) {
            filesIndexes = new ArrayList<Integer>();
        }
        return filesIndexes;
    }

    public String getFileNameByIndex(Integer key) {
        String fileName = indexesOfFiles.get(key);
        if (fileName == null) {
            throw new IllegalStateException("No file with index " + key);
        }
        return fileName;
    }

    public void appendTermWithIndex(String term, String[] filesIndexes) {
        for (String fileIndex : filesIndexes) {
            appendTermWithIndex(term, Integer.valueOf(fileIndex));
        }
    }

    public void closeReaderAndWriter() {
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
            if (fileReader != null) {
                fileReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void splitFile(String line) {
        String[] fileNameAndIndex = line.split(":");
        appendFileWithIndex(fileNameAndIndex[0], Integer.valueOf(fileNameAndIndex[1]));
    }

    private void splitTerm(String line) {
        String[] fileNameAndIndex = line.split(":");
        appendTermWithIndex(fileNameAndIndex[0], fileNameAndIndex[1].split(" "));
    }
}
