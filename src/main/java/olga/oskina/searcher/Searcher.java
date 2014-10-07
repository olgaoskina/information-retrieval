package olga.oskina.searcher;

import olga.oskina.index.InvertedIndex;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.morphology.russian.RussianAnalyzer;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by olgaoskina
 * 07 October 2014.
 */
public class Searcher {
    private enum Kind {
        OR, AND
    }

    private InvertedIndex invertedIndex;

    public Searcher(File inputFile) {
        invertedIndex = new InvertedIndex(inputFile);
        invertedIndex.read();
    }

    public String find(String request) {
        Kind typeOfRequest = typeOfRequest(request);
        String result;
        if (typeOfRequest == null) {
            result = "incorrect query";
        } else if (typeOfRequest == Kind.AND) {
            result = convertToValidFormat(changeKeyToFileName(searchAnd(request)));
        } else {
            result = convertToValidFormat(changeKeyToFileName(searchOR(request)));
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

    private List<String> changeKeyToFileName(List<Integer> keys) {
        List<String> fileNames = new ArrayList<String>();
        for (Integer key : keys) {
            fileNames.add(invertedIndex.getFileNameByIndex(key));
        }
        return fileNames;
    }

    private List<Integer> searchAnd(String request) {
        String[] terms = request.replaceAll("AND ", "").split(" ");
        RussianAnalyzer russianAnalyzer = null;
        TokenStream tokenStream = null;
        List<Integer> result = null;
        try {
            russianAnalyzer = new RussianAnalyzer();
            for (int i = 0; i < terms.length; i++) {
                tokenStream = russianAnalyzer.tokenStream(null, new StringReader(terms[i]));
                tokenStream.incrementToken();
                terms[i] = tokenStream.getAttribute(TermAttribute.class).term();
            }

            result = invertedIndex.getFilesIndexesByTerm(terms[0]);

            for (int i = 1; i < terms.length; i++) {
                List<Integer> filesIndexesByTerm = invertedIndex.getFilesIndexesByTerm(terms[i]);
                result = searchAnd(result, filesIndexesByTerm);
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

    private List<Integer> searchAnd(List<Integer> resultIndexes, List<Integer> newIndexes) {
        List<Integer> answer = new ArrayList<Integer>();
        int p1 = 0;
        int p2 = 0;

        while (p1 < resultIndexes.size() && p2 < newIndexes.size()) {
            int resultNextInteger = resultIndexes.get(p1);
            int newNextInteger = newIndexes.get(p2);
            if (resultNextInteger == newNextInteger) {
                answer.add(resultNextInteger);
                p1++;
                p2++;
            } else if (resultNextInteger < newNextInteger) {
                p1++;
            } else {
                p2++;
            }
        }
        return answer;
    }

    private List<Integer> searchOR(String request) {
        List<Integer> result = new ArrayList<Integer>();
        RussianAnalyzer russianAnalyzer = null;
        TokenStream tokenStream = null;
        String[] terms = request.replaceAll("OR ", "").split(" ");
        try {
            russianAnalyzer = new RussianAnalyzer();
            for (int i = 0; i < terms.length; i++) {
                tokenStream = russianAnalyzer.tokenStream(null, new StringReader(terms[i]));
                tokenStream.incrementToken();
                terms[i] = tokenStream.getAttribute(TermAttribute.class).term();
            }

            Set<Integer> set = new HashSet<Integer>();
            for (String term : terms) {
                set.addAll(invertedIndex.getFilesIndexesByTerm(term));
            }
            result.addAll(set);
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

    private Kind typeOfRequest(String request) {
        Pattern patternOR = Pattern.compile("^([A-Za-zА-Яа-я0-9]+ OR )*[A-Za-zА-Яа-я0-9]+$");
        Matcher matcherOR = patternOR.matcher(request);
        Pattern patternAND = Pattern.compile("^([A-Za-zА-Яа-я0-9]+ AND )+[A-Za-zА-Яа-я0-9]+$");
        Matcher matcherAND = patternAND.matcher(request);
        if (matcherAND.find()) {
            return Kind.AND;
        } else if (matcherOR.find()) {
            return Kind.OR;
        }
        return null;
    }
}
