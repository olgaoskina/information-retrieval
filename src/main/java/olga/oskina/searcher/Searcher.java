package olga.oskina.searcher;

import olga.oskina.index.CoordinateIndex;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.morphology.russian.RussianAnalyzer;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by olgaoskina
 * 07 October 2014.
 */
public class Searcher {

    private CoordinateIndex coordinateIndex;

    public Searcher(File inputFile) {
        coordinateIndex = new CoordinateIndex(inputFile);
        coordinateIndex.read();
    }

    public String find(String request) {
        boolean typeOfRequest = checkFormat(request);
        String result;
        if (!typeOfRequest) {
            result = "incorrect query";
        } else {
            result = convertToValidFormat(changeKeyToFileName(search(request)));
        }
        return result;
    }

    private String convertToValidFormat(List<String> files) {
        String result = "";
        switch (files.size()) {
            case 0:
                result = "no documents found";
                break;
            case 1:
                result = files.get(0);
                break;
            case 2:
                result = files.get(0) + ", " + files.get(1);
                break;
            default: {
                result += files.get(0);
                result += ", ";
                result += files.get(1);
                result += " and " + (files.size() - 2) + " more";
            }
        }
        return result;
    }

    private List<String> changeKeyToFileName(List<Pair<Integer, Integer>> keysAndPos) {
        Set<String> fileNames = new HashSet<String>();
        for (Pair<Integer, Integer> keyAndPos : keysAndPos) {
            fileNames.add(coordinateIndex.getFileNameByIndex(keyAndPos.getLeft()));
        }
        return new ArrayList<String>(fileNames);
    }

    private List<Pair<Integer, Integer>> search(String request) {
        String[] terms = request.trim().replaceAll(" +", " ").split(" ");
        RussianAnalyzer russianAnalyzer = null;
        TokenStream tokenStream = null;
        List<Pair<Integer, Integer>> result = null;
        try {
            russianAnalyzer = new RussianAnalyzer();
			//	handle only the even positions, because there are no terms in odd positions
            for (int i = 0; i < terms.length; i += 2) {
                tokenStream = russianAnalyzer.tokenStream(null, new StringReader(terms[i]));
                tokenStream.incrementToken();
                terms[i] = tokenStream.getAttribute(TermAttribute.class).term();
            }
			result = coordinateIndex.getFilesIndexesByTerm(terms[0]);
			for (int i = 2; i < terms.length; i += 2) {
                List<Pair<Integer, Integer>> filesIndexesByTerm = coordinateIndex.getFilesIndexesByTerm(terms[i]);
				result = search(result, filesIndexesByTerm, terms[i - 1].replace("/", ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (russianAnalyzer != null) {
                    russianAnalyzer.close();
                }
                if (tokenStream != null) {
                    tokenStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private List<Pair<Integer, Integer>> search(List<Pair<Integer, Integer>> result, List<Pair<Integer, Integer>> secondIndexes, String term) {
        List<Pair<Integer, Integer>> answer = new ArrayList<Pair<Integer, Integer>>();
		int distance = Integer.valueOf(term);
		boolean useAbs = term.matches("^[0-9]+$");
		for (Pair<Integer, Integer> resultPair : result) {
			for (Pair<Integer, Integer> secondPair : secondIndexes) {
				if (resultPair.getLeft().equals(secondPair.getLeft())) {
					int distanceBetweenPairs;
					if (useAbs) {
						distanceBetweenPairs = Math.abs(resultPair.getRight() - secondPair.getRight());
					} else {
						distanceBetweenPairs = secondPair.getRight() - resultPair.getRight();
					}

					if (Math.abs(distanceBetweenPairs) <= Math.abs(distance)
							&& Integer.signum(distance) == Integer.signum(distanceBetweenPairs)) {
						answer.add(secondPair);
					}
				}
			}
		}
		return answer;
    }

    private boolean checkFormat(String request) {
        Pattern pattern = Pattern.compile("^([A-Za-zА-Яа-я0-9]+ /[+-]?[0-9]+ )*[A-Za-zА-Яа-я0-9]+$");
		Matcher matcher = pattern.matcher(request);
		return matcher.find();
    }
}
