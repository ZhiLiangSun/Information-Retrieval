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

    private int querynumber;
    private Properties prop;
    private IndexSearcher searcher;

    public List<String> top20RDocList;
    public List<String> top20NRDocList;
    public List<Integer> top20RDocIdList;
    public List<Integer> top20NRDocIdList;
    public List<Integer> rDocOriginalRank;
    public List<Integer> nrDocOriginalRank;

    public QueryExpansion(int querynumber, IndexSearcher searcher, Properties prop) {

        this.querynumber = querynumber;
        this.searcher = searcher;
        this.prop = prop;

        top20RDocList = new ArrayList<>();
        top20NRDocList = new ArrayList<>();
        top20RDocIdList = new ArrayList<>();
        top20NRDocIdList = new ArrayList<>();
        rDocOriginalRank = new ArrayList<>();
        nrDocOriginalRank = new ArrayList<>();

    }


    public Vector<Document> getDocs(String query, TopDocs hits, List<Integer> rDocOriginalRank) throws IOException {
        Vector<Document> vHits = new Vector<>();
        // Extract only as many docs as necessary
        int docNum = Integer.valueOf(prop.getProperty(QueryExpansion.DOC_NUM_FLD)).intValue();

        // Convert Hits -> Vector
        for (int i = 0; ((i < docNum) && (i < hits.scoreDocs.length)); i++) {
            vHits.add(searcher.doc(hits.scoreDocs[rDocOriginalRank.get(i)].doc));
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
}
