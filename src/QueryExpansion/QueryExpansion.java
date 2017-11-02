package QueryExpansion;

import Utils.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.*;

public class QueryExpansion {

    public static final String DOC_NUM_FLD = "QE.doc.num";

    private Properties prop;
    private IndexSearcher searcher;

    public List<String> top20RDocList = new ArrayList<>();
    public List<String> top20NRDocList = new ArrayList<>();
    public List<Integer> top20RDocIdList = new ArrayList<>();
    public List<Integer> top20NRDocIdList = new ArrayList<>();
    public List<Integer> rDocOriginalRank = new ArrayList<>();
    public List<Integer> nrDocOriginalRank = new ArrayList<>();


    private Vector<Document> getDocs(String query, TopDocs hits) throws IOException {
        Vector<Document> vHits = new Vector<Document>();
        // Extract only as many docs as necessary
        int docNum = Integer.valueOf(prop.getProperty(QueryExpansion.DOC_NUM_FLD)).intValue();

        // Convert Hits -> Vector
        for (int i = 0; ((i < docNum) && (i < hits.scoreDocs.length)); i++) {
            vHits.add(searcher.doc(hits.scoreDocs[i].doc));
        }

        return vHits;
    }

    public void getTopDoc(TopDocs hits, int querynumber, IndexSearcher searcher) throws IOException {

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
                this.rDocOriginalRank.add(i + 1);
            } else if (!rList.contains(docNo) && top20NRDocIdList.size() < 20) {
                this.top20NRDocList.add(docNo);
                this.top20NRDocIdList.add(scoreDocs[i].doc);
                this.nrDocOriginalRank.add(i + 1);
            }
        }
    }
}
