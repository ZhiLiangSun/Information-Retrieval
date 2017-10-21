package TRECIndex;


import TRECParser.Path;
import Utils.FileLoader;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;

import java.io.File;

public class TRECIndex {
    public static void main(String[] args) throws Exception {
        boolean creat = true;
        String indexPath = Path.Project_Path + "/res/testing/";
        Directory dir = FSDirectory.open(new File(indexPath));
        StandardAnalyzer analyzer = new StandardAnalyzer(
                new CharArraySet(Utils.FileUtils.getStopWords(), true));
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);

        if (creat) {
            iwc.setOpenMode(OpenMode.CREATE);
        } else {
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        }

        IndexWriter writer = null;
        writer = new IndexWriter(dir, iwc);

        for (File f : new File(Path.Data_Path + "/raw_parsed/").listFiles()) {

            FileLoader loader = new FileLoader(f);
            System.out.println("Indexing " + f.getName() + "...");

            FieldType type = new FieldType();
            type.setIndexed(true);
            type.setStored(true);
            type.setStoreTermVectors(true);

            Document doc = new Document();

            Field contentField = new Field("content", loader.loadContent().trim(), type);
            Field docnoField = new StringField("DOCNO", f.getName(), Field.Store.YES);

            doc.add(contentField);
            doc.add(docnoField);

            writer.addDocument(doc);
        }
        writer.close();

    }


}


