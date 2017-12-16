package QueryExpansion;

import Utils.FileUtils;
import Utils.Path;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import java.util.List;
import java.util.StringJoiner;
import java.util.Vector;

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
}
