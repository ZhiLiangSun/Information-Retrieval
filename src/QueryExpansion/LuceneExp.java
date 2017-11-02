package QueryExpansion;

import Utils.ExpUtils;
import Utils.FileUtils;
import Utils.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LuceneExp {

    public static void main(String[] args) throws Exception {

        LuceneExp exp = new LuceneExp();
        exp.search("original", 301);

    }

    public void search(String method, int querynumber) throws Exception {

        System.out.println("===========  " + method + " " + querynumber + "  ===========");

        String queryString = ExpUtils.getQueryString(querynumber);

        Properties prop = new Properties();
        prop.load(new FileInputStream("search.prop"));

        String indexDir = Path.Index_Path + querynumber;
        String outFileName = prop.getProperty("mac-out-file");
        int relDocCount = Integer.valueOf(prop.getProperty("docs-per-query")).intValue();

        IndexReader idxReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
        IndexSearcher searcher = new IndexSearcher(idxReader);
        StandardAnalyzer analyzer = new StandardAnalyzer(new CharArraySet(FileUtils.getStopWords(), true));

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFileName), true));
        int hitsCount = 1000;
        QueryParser parser = new QueryParser("content", analyzer);
        Query query = parser.parse(queryString);
        //Query query = new QueryParser("content",analyzer).parse(queryString);
        TFIDFSimilarity similarity = (TFIDFSimilarity) searcher.getSimilarity();

        TopDocs hits = searcher.search(query, hitsCount);

        List<String> topRDocList = new ArrayList<String>();
        ScoreDoc[] scoreDoc = hits.scoreDocs;

        QueryExpansion q = new QueryExpansion();
        q.getTopDoc(hits,301,searcher);

        int top1000 = 1000;
        if (hits.totalHits < top1000) {
            top1000 = hits.totalHits;
        }

        for (int i = 0; i < top1000; i++) {
            topRDocList.add(searcher.doc(scoreDoc[i].doc).get("DOCNO"));
        }

        for (int i = 0; (i < hits.totalHits) && (i < relDocCount); i++) {
            Document doc = searcher.doc(hits.scoreDocs[i].doc);
            String docno = ((Field) doc.getField("DOCNO")).stringValue().trim();
            writer.write(querynumber + " Q0 " + docno + " " + (i + 1) + " " + hits.scoreDocs[i].score + " Original" + "\r\n");
        }
        writer.flush();
        writer.close();

        System.out.println("Initial Query: " + queryString);
        System.out.println("共找到：" + hits.totalHits);
    }
}
