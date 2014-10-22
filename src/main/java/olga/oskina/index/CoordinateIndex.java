package olga.oskina.index;

import com.beust.jcommander.internal.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by olgaoskina
 * 21 October 2014.
 */
public class CoordinateIndex {
	private HashMap<String, List<Pair<Integer, Integer>>> coordinateIndex;
	private HashMap<Integer, String> indexesOfFiles;
	private BufferedWriter fileWriter = null;
	private BufferedReader fileReader = null;
	private File fileToWriteOrRead = null;

	public CoordinateIndex(File fileToWriteOrRead) {
		this.fileToWriteOrRead = fileToWriteOrRead;
		coordinateIndex = new HashMap<String, List<Pair<Integer, Integer>>>();
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

			for (Map.Entry<String, List<Pair<Integer, Integer>>> entry : coordinateIndex.entrySet()) {
				String word = entry.getKey();
				List<Pair<Integer, Integer>> files = entry.getValue();

				fileWriter
						.append(word)
						.append(":");
				for (Pair<Integer, Integer> fileIndex : files) {
					fileWriter
							.append(fileIndex.getLeft().toString())
							.append("-")
							.append(fileIndex.getRight().toString())
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

	public void appendTermWithIndex(String term, Pair<Integer, Integer> fileIndex) {
		List<Pair<Integer, Integer>> tokenIndexes = coordinateIndex.get(term);
		if (tokenIndexes != null) {
			Pair<Integer, Integer> lastValue = tokenIndexes.get(tokenIndexes.size() - 1);
			if (lastValue != fileIndex) {
				tokenIndexes.add(fileIndex);
			}
		} else {
			ArrayList<Pair<Integer, Integer>> list = new ArrayList<Pair<Integer, Integer>>();
			list.add(fileIndex);
			coordinateIndex.put(term, list);
		}
	}

	/*
	* Empty if null
	* */
	public List<Pair<Integer, Integer>> getFilesIndexesByTerm(String term) {
		List<Pair<Integer, Integer>> filesIndexes = coordinateIndex.get(term);
		if (filesIndexes == null) {
			filesIndexes = new ArrayList<Pair<Integer, Integer>>();
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
		String[] arrayOfPairs = fileNameAndIndex[1].split(" ");
		for (String pair : arrayOfPairs) {
			final Integer right = Integer.valueOf(pair.split("-")[1]);
			final Integer left = Integer.valueOf(pair.split("-")[0]);
			appendTermWithIndex(fileNameAndIndex[0], Pair.of(left, right));
		}

	}
}
