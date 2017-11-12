package QueryExpansion;

import Utils.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.TFIDFSimilarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class QueryExpansion {

    public static final String DOC_NUM_FLD = "QE.doc.num";

    private int querynumber;
    private IndexSearcher searcher;
    private Properties prop;
    private Analyzer analyzer;
    private TFIDFSimilarity similarity;

    public List<String> top20RDocList;
    public List<String> top20NRDocList;
    public List<Integer> top20RDocIdList;
    public List<Integer> top20NRDocIdList;
    public List<Integer> rDocOriginalRank;
    public List<Integer> nrDocOriginalRank;

    public QueryExpansion(int querynumber, IndexSearcher searcher, Properties prop, Analyzer analyzer, TFIDFSimilarity similarity) {

        this.querynumber = querynumber;
        this.searcher = searcher;
        this.prop = prop;
        this.analyzer = analyzer;
        this.similarity = similarity;

        top20RDocList = new ArrayList<>();
        top20NRDocList = new ArrayList<>();
        top20RDocIdList = new ArrayList<>();
        top20NRDocIdList = new ArrayList<>();
        rDocOriginalRank = new ArrayList<>();
        nrDocOriginalRank = new ArrayList<>();

    }


    public Vector<Document> getDocs(String query, TopDocs hits, List<Integer> DocOriginalRank) throws IOException {
        Vector<Document> vHits = new Vector<>();
        // Extract only as many docs as necessary
        int docNum = Integer.valueOf(prop.getProperty(QueryExpansion.DOC_NUM_FLD)).intValue();

        // Convert Hits -> Vector
        for (int i = 0; ((i < docNum) && (i < hits.scoreDocs.length)); i++) {
            vHits.add(searcher.doc(hits.scoreDocs[DocOriginalRank.get(i)].doc));
        }

        return vHits;
    }

    public void getTopDoc(TopDocs hits) throws IOException {

        ScoreDoc[] scoreDocs = hits.scoreDocs;
        List<String> rList;
        rList = FileUtils.getRelevantDocList(querynumber);

        Document doc;
        String docNo;

        for (int i = 0; i < scoreDocs.length; i++) {
            doc = searcher.doc(scoreDocs[i].doc);
            docNo = doc.get("DOCNO");

            if (rList.contains(docNo) && top20RDocIdList.size() < 20) {
                this.top20RDocList.add(docNo);
                this.top20RDocIdList.add(scoreDocs[i].doc);
                this.rDocOriginalRank.add(i);
            } else if (!rList.contains(docNo) && top20NRDocIdList.size() < 20) {
                this.top20NRDocList.add(docNo);
                this.top20NRDocIdList.add(scoreDocs[i].doc);
                this.nrDocOriginalRank.add(i);
            }
        }
    }

    public Vector<QueryTermVector> getDocsTerms(Vector<Document> hits, int docsRelevantCount) throws IOException {
        Vector<QueryTermVector> docsTerms = new Vector<QueryTermVector>();

        // Process each of the documents
        for (int i = 0; ((i < docsRelevantCount) && (i < hits.size())); i++) {
            Document doc = hits.elementAt(i);
            // Get text of the document and append it
            StringBuffer docTxtBuffer = new StringBuffer();
            String[] docTxtFlds = doc.getValues("content");
            if (docTxtFlds.length == 0)
                continue;
            for (int j = 0; j < docTxtFlds.length; j++) {
                docTxtBuffer.append(docTxtFlds[j] + " ");
            }

            // Create termVector and add it to vector
            QueryTermVector docTerms = new QueryTermVector(docTxtBuffer.toString(), analyzer);
            docsTerms.add(docTerms);
        }

        return docsTerms;
    }

    public Vector<TermQuery> setBoost(Vector<QueryTermVector> docsTerms, float factor, float decayFactor)
            throws IOException {
        Vector<TermQuery> terms = new Vector<TermQuery>();

        // setBoost for each of the terms of each of the docs
        for (int g = 0; g < docsTerms.size(); g++) {
            QueryTermVector docTerms = docsTerms.elementAt(g);
            String[] termsTxt = docTerms.getTerms();
            int[] termFrequencies = docTerms.getTermFrequencies();

            // Increase decay
            float decay = decayFactor * g;

            // Populate terms: with TermQuries and set boost
            for (int i = 0; i < docTerms.size(); i++) {
                // Create Term
                String termTxt = termsTxt[i];
                Term term = new Term("content", termTxt);

                // Calculate weight
                float tf = termFrequencies[i];
                float idf = similarity.idf((long) tf, docTerms.size());
                float weight = tf * idf;
                // Adjust weight by decay factor
                weight = weight - (weight * decay);

                // Create TermQuery and add it to the collection
                TermQuery termQuery = new TermQuery(term);
                // Calculate and set boost
                termQuery.setBoost(factor * weight);
                terms.add(termQuery);
            }
        }

        // Get rid of duplicates by merging termQueries with equal terms
        merge(terms);

        return terms;
    }

    private void merge(Vector<TermQuery> terms) {
        for (int i = 0; i < terms.size(); i++) {
            TermQuery term = terms.elementAt(i);
            // Iterate through terms and if term is equal then merge: add the boost; and delete the term
            for (int j = i + 1; j < terms.size(); j++) {
                TermQuery tmpTerm = terms.elementAt(j);

                // If equal then merge
                if (tmpTerm.getTerm().text().equals(term.getTerm().text())) {
                    // Add boost factors of terms
                    term.setBoost(term.getBoost() + tmpTerm.getBoost());
                    // delete unnecessary term
                    terms.remove(j);
                    // decrement j so that term is not skipped
                    j--;
                }
            }
        }
    }

}
