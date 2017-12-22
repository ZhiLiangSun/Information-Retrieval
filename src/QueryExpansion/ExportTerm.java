package QueryExpansion;

import Utils.FileUtils;
import Utils.Path;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import java.util.*;

public class ExportTerm {

    public void exportxt(Vector<TermQuery> termQueries, String name) {
        //test for top 20 terms

        int threshold = 100;
        int index = 0;
        StringJoiner term_joiner = new StringJoiner(" ");
        List<String> stopWords = FileUtils.getStopWords();


        while (index < 20) {
            TermQuery termQuery = termQueries.elementAt(index);
            Term term = termQuery.getTerm();
            String termString = term.text().toLowerCase();

            if (stopWords.contains(termString))
                continue;

            //if (termQuery.getBoost() > threshold)
            term_joiner.add(termString);

            //if (termQuery.getBoost() < threshold)
            //    break;

            index++;
        }
        String targetStr = term_joiner.toString();
        FileUtils.writeFile(Path.docTerm_Path + name + ".txt", targetStr, "utf-8");
    }

    public LinkedHashMap<String, Float> synTFIDF(Vector<TermQuery> docrTerm, int querynum) {

        LinkedHashMap<String, Float> term_TFIDF = new LinkedHashMap<>();
        LinkedHashMap<String, Float> term_w2v = new LinkedHashMap<>();
        LinkedHashMap<String, Float> term_score = new LinkedHashMap<>();
        String syn = FileUtils.readFile(Path.word2vec_Path + "synTerm/" + querynum + ".txt");
        Float tmp;

        String delim = " \n";
        StringTokenizer st = new StringTokenizer(syn, delim);

        while (st.hasMoreTokens()) {
            term_w2v.put(st.nextToken(), Float.parseFloat(st.nextToken()));
        }

        for (int i = 0; i < docrTerm.size(); i++) {
            String[] rel_term = docrTerm.get(i).toString("content")
                    .replace("^", ",").split(",");

            if (term_w2v.get(rel_term[0]) != null) {
                term_TFIDF.put(rel_term[0], Float.parseFloat(rel_term[1]));
                // similarity * TF-IDF
                tmp = term_w2v.get(rel_term[0]) * term_TFIDF.get(rel_term[0]);
                term_score.put(rel_term[0], tmp);

            }

        }

        List<Map.Entry<String, Float>> entries =
                new ArrayList<Map.Entry<String, Float>>(term_score.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<String, Float>>() {
            public int compare(Map.Entry<String, Float> a, Map.Entry<String, Float> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });

        int size = entries.size();
        for (int i = 0; i < size - 20; i++) {
            entries.remove(entries.size() - 1);
        }

        LinkedHashMap<String, Float> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;

    }
}
