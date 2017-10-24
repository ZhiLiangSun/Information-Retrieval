package TRECIndex;


import Utils.Path;
import Utils.FileLoader;
import Utils.Topic;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class TRECIndex {
    public static void main(String[] args) throws Exception {

        int[] topics = Topic.topics_all;

        TRECIndex trceindex = new TRECIndex();
        for (int i = 0; i < topics.length; i++) {
            trceindex.initialIndexer(topics[i]);
        }
    }

    private void initialIndexer(int queryNumber) throws Exception {
        boolean creat = true;
        String indexPath = Path.Project_Path + "/res/Lucene/index/" + queryNumber;
        String docPath = Path.Data_Path + "/answer/parsed/" + queryNumber;
        File index = new File(indexPath);
        File docs = new File(docPath);
        Directory dir = FSDirectory.open(index);
        StandardAnalyzer analyzer = new StandardAnalyzer(
                new CharArraySet(Utils.FileUtils.getStopWords(), true));
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);

        if (creat) {
            iwc.setOpenMode(OpenMode.CREATE);
        } else {
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        }
        iwc.setRAMBufferSizeMB(2048.0);

        IndexWriter writer = null;
        writer = new IndexWriter(dir, iwc);

        indexDocs(writer, docs);
        writer.close();

    }

    private void indexDocs(IndexWriter writer, File file) throws Exception {
        if (file.isDirectory()) {
            String[] files = file.list();
            Arrays.sort(files);

            for (int i = 0; i < files.length; i++) {
                // recursively index them
                indexDocs(writer, new File(file, files[i]));
            }
        } else {
            System.out.println("adding " + file.getPath());
            addDocuments(writer, file);
        }
    }

    private void addDocuments(IndexWriter writer, File file) throws IOException, InterruptedException {

        FileLoader loader = new FileLoader(file);
        String docNo = file.getName();

        Document doc = TRECDocument.Document(loader, docNo);
        if (doc != null) {
            writer.addDocument(doc);
        }
    }
}





