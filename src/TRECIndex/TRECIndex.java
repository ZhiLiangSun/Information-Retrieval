package TRECIndex;


import TRECParser.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TRECIndex {
    public static void main(String[] args) throws Exception {
        File f = new File(Path.Data_Path+"/raw_parsed/CR93E-220");
        String indexPath = Path.Project_Path + "/res/testing/";
        Directory dir = FSDirectory.open(new File(indexPath));

        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
        IndexWriter writer = null ;

        writer = new IndexWriter(dir, iwc);

        List<String> lines = Files.readAllLines(Paths.get(Path.Data_Path+"/raw_parsed/CR93E-220"));
        StringBuilder sb1 = new StringBuilder();
        for (String s : lines)
        {
            sb1.append(s);
            sb1.append("\t");
        }

        System.out.println("Indexing file " + "CR93E-220");

        FieldType type = new FieldType();
        type.setIndexed(true);
        type.setStored(true);
        type.setStoreTermVectors(true);

        Document doc = new Document();

        Field filePathField = new Field("content",sb1.toString().trim(),type);

        doc.add(filePathField);
        writer.addDocument(doc);

        writer.close();
    }


}


