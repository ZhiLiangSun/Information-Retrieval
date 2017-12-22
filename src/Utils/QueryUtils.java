package Utils;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class QueryUtils {

    public static float getTF(String term, int docId, IndexReader idxReader) throws IOException,
            NullPointerException {
        // tf(t in d)
        Terms termFreqVector = idxReader.getTermVector(docId, Defs.FIELD);
        String[] terms = new String[0];
        int freqs[];
        ArrayList<String> temp1 = new ArrayList<String>();
        ArrayList<Integer> temp2 = new ArrayList<Integer>();
        TermsEnum termsEnum1 = termFreqVector.iterator(null);
        //BytesRef term1 = null;
        while (termsEnum1.next() != null) {
            temp1.add(termsEnum1.term().utf8ToString());
            temp2.add((int) termsEnum1.totalTermFreq());
        }
        terms = temp1.toArray(terms);

        int[] ret = new int[temp2.size()];
        int i = 0;
        for (Integer e : temp2)
            ret[i++] = e.intValue();
        freqs = ret;

        boolean found = false;
        float tf = 0;
        for (i = 0; i < terms.length && !found; i++) {
            if (term.equals(terms[i])) {
                tf = freqs[i];
                found = true;
            }
        }
        return tf;
    }

    public static float coord(Vector<TermQuery> terms, Document doc, int docId, Similarity similarity, IndexReader idxReader) throws IOException {
        int maxOverlap = terms.size();
        int overlap = 0;
        // Calculate overlap (terms w/ freq > 0
        for (int i = 0; i < terms.size(); i++) {
            float tf = 0;
            try {
                tf = getTF(terms.elementAt(i).getTerm().text(), docId, idxReader);
            } catch (Exception e) {
                continue;
            }
            if (tf > 0) {
                overlap++;
            }
        }

        float coord = similarity.coord(overlap, maxOverlap);
        // System.out.println( overlap + " : " + maxOverlap + " : " + coord );
        return coord;
    }
}
