package QueryExpansion;

import Utils.*;
import com.google.common.io.Files;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.util.*;

public class LuceneExp {

    public static void main(String[] args) throws Exception {
        Date start = new Date();

        int[] topics = Topic.topics_100;
        String[] methods = {"Original", "Sun"};
        LuceneExp exp = new LuceneExp();

        for (int i = 0; i < methods.length; i++) {

            for (int j = 0; j < topics.length; j++) {
                exp.search(methods[i], topics[j]);
            }

            // run evaluate bat
            CMD.run(methods[i]);

            // rename the input file
            File oldFile = new File("C:/Users/Lab714/Desktop/Exp_input/Exp_input");
            File newFile = new File(oldFile.getParent(), methods[i] + "_input");
            Files.move(oldFile, newFile);
        }

        ExpUtils.printTimeUsage(start, new Date());
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
        QueryParser parser = new QueryParser(Defs.FIELD, analyzer);
        Query query = parser.parse(queryString);
        //Query query = new QueryParser("content",analyzer).parse(queryString);
        TFIDFSimilarity similarity = (TFIDFSimilarity) searcher.getSimilarity();

        TopDocs hits = searcher.search(query, hitsCount);

        List<String> topRDocList = new ArrayList<String>();
        ScoreDoc[] scoreDoc = hits.scoreDocs;

        int top1000 = 1000;
        if (hits.totalHits < top1000) {
            top1000 = hits.totalHits;
        }

        //top 1000 relevant documents
        for (int i = 0; i < top1000; i++) {
            topRDocList.add(searcher.doc(scoreDoc[i].doc).get("DOCNO"));
        }

        QueryExpansion queryExpansion;
        if (method.equals(QueryExpansion.SUN_METHOD)) {

            queryExpansion = new QueryExpansion(method, querynumber, searcher, prop, analyzer, similarity);
            Query queryr = queryExpansion.expandQuerySun(queryString, hits, true);
            //Query querynr = queryExpansion.expandQuerySun(queryString, hits, false);

            System.out.println("Expanded Query: " + queryr.toString("contents"));
            hits = searcher.search(queryr, 18948);
            Vector<TermQuery> expandedQueryTerms = queryExpansion.getExpandedTerms();
            System.out.println("Expanded Size: " + queryExpansion.getExpandedTerms().size());

            generateOutput(method, hits, expandedQueryTerms, querynumber, writer,
                    relDocCount, searcher, similarity, idxReader, topRDocList, top1000);

        } else {
            for (int i = 0; (i < hits.totalHits) && (i < relDocCount); i++) {
                Document doc = searcher.doc(hits.scoreDocs[i].doc);
                String docno = ((Field) doc.getField("DOCNO")).stringValue().trim();
                writer.write(querynumber + " Q0 " + docno + " " + (i + 1) + " " + hits.scoreDocs[i].score + " Original" + "\r\n");
            }
        }

        writer.flush();
        writer.close();

        System.out.println("Initial Query: " + queryString);
        System.out.println("共找到：" + hits.totalHits);
    }


    //Generates necessary output
    private void generateOutput(String method, TopDocs hits, Vector<TermQuery> expandedQueryTerms, int query_num,
                                BufferedWriter writer, int outCount, IndexSearcher searcher,
                                TFIDFSimilarity similarity, IndexReader idxReader,
                                List<String> top1000RelevantDocList, int top1000) throws IOException {

        for (int i = 0; ((i < hits.scoreDocs.length) && (i < outCount)); i++) {
            Document doc = searcher.doc(hits.scoreDocs[i].doc);
            String docno = ((Field) doc.getField("DOCNO")).stringValue().trim();

            int docId = hits.scoreDocs[i].doc;
            float coord = QueryUtils.coord(expandedQueryTerms, doc, docId, similarity, idxReader);

            writer.write(query_num + " Q0 " + docno + " " + (i + 1) + " " + hits.scoreDocs[i].score + " " + coord + "\r\n");
        }
    }
}
