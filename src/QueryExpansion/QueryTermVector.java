package QueryExpansion;

import Utils.Defs;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class QueryTermVector {
    private String[] terms = new String[0];
    private int[] termFreqs = new int[0];

    public String getField() {
        return null;
    }

    /**
     * @param queryTerms The original list of terms from the query, can contain duplicates
     */
    public QueryTermVector(String[] queryTerms) {
        processTerms(queryTerms);
    }

    public QueryTermVector(String queryString, Analyzer analyzer) throws IOException {
        if (analyzer != null) {
            TokenStream stream = analyzer.tokenStream(Defs.FIELD, new StringReader(queryString));

            if (stream != null) {
                List<String> terms = new ArrayList<String>();

                stream.reset();
                CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
                while (stream.incrementToken()) {
                    terms.add(termAtt.toString());
                }

//                processTerms(queryString.split(" "));
                processTerms(terms.toArray(new String[terms.size()]));
            }
            stream.close();
        }
    }

    private void processTerms(String[] queryTerms) {
        if (queryTerms != null) {

            Arrays.sort(queryTerms);
            Map<String, Integer> tmpSet = new HashMap<String, Integer>(queryTerms.length);
            // filter out duplicates
            List<String> tmpList = new ArrayList<String>(queryTerms.length);
            List<Integer> tmpFreqs = new ArrayList<Integer>(queryTerms.length);
            int j = 0;
            for (int i = 0; i < queryTerms.length; i++) {
                String term = queryTerms[i];

                Integer position = tmpSet.get(term);
                if (position == null) {
                    tmpSet.put(term, Integer.valueOf(j++));
                    tmpList.add(term);
                    tmpFreqs.add(Integer.valueOf(1));
                } else {
                    Integer integer = tmpFreqs.get(position.intValue());
                    tmpFreqs.set(position.intValue(), Integer.valueOf(integer.intValue() + 1));
                }
            }
            terms = tmpList.toArray(terms);
            // termFreqs = (int[])tmpFreqs.toArray(termFreqs);
            termFreqs = new int[tmpFreqs.size()];
            int i = 0;
            for (final Integer integer : tmpFreqs) {
                termFreqs[i++] = integer.intValue();
            }
        }
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int i = 0; i < terms.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(terms[i]).append('/').append(termFreqs[i]);
        }
        sb.append('}');
        return sb.toString();
    }

    public int size() {
        return terms.length;
    }

    public String[] getTerms() {
        return terms;
    }

    public int[] getTermFrequencies() {
        return termFreqs;
    }

    public int indexOf(String term) {
        int res = Arrays.binarySearch(terms, term);
        return res >= 0 ? res : -1;
    }

    public int[] indexesOf(String[] terms, int start, int len) {
        int res[] = new int[len];

        for (int i = 0; i < len; i++) {
            res[i] = indexOf(terms[i]);
        }
        return res;
    }
}
