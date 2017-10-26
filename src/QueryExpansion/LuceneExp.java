package QueryExpansion;

import Utils.FileUtils;
import Utils.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class LuceneExp {

    public static void main(String[] args) throws Exception {

        LuceneExp exp = new LuceneExp();
        exp.search("house",301);

    }

    public void search(String queryString,int querynumber)throws Exception{

        Properties prop = new Properties();
        prop.load(new FileInputStream("search.prop"));

        String indexDir = Path.Index_Path + querynumber;
        String outFileName = prop.getProperty("mac-out-file");
        int relDocCount = Integer.valueOf(prop.getProperty("docs-per-query")).intValue();

        IndexReader idxReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
        IndexSearcher searcher = new IndexSearcher(idxReader);
        StandardAnalyzer analyzer = new StandardAnalyzer(new CharArraySet(FileUtils.getStopWords(), true));

        //BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFileName), true));
        int hitsCount = 1000;
        //QueryParser parser = new QueryParser("content", analyzer);
        //Query query = parser.parse(queryString);
        Query query = new QueryParser("content",analyzer).parse(queryString);
        //TFIDFSimilarity similarity = (TFIDFSimilarity) searcher.getSimilarity();

        TopDocs hits = searcher.search(query, hitsCount);

        System.out.println("Initial Query: " + queryString);
        System.out.println("共找到：" + hits.totalHits);


    }
}
