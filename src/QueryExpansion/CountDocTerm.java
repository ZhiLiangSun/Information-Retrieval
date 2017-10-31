package QueryExpansion;

import Utils.ExpUtils;
import Utils.FileLoader;
import Utils.FileUtils;
import Utils.Path;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CountDocTerm {
    public static void main(String[] args) throws IOException {
        CountDocTerm CDT = new CountDocTerm();
        System.out.println(CDT.Count(301));
        System.out.println(CDT.Count(302));


    }

    public int Count(int querynumber) throws IOException {
        int averange = 0;
        int sum = 0;
        List<File> files = new ArrayList<File>();
        String docPath = Path.Data_Path + "/answer/parsed/" + querynumber ;
        //String docPath = Path.Data_Path + "/test/";

        files = FileUtils.listAllFiles(docPath);

        for (File file : files) {
            FileLoader loader = new FileLoader(file);

            FieldType type = new FieldType();
            type.setIndexed(true);

            Document doc = new Document();
            doc.add(new Field("content", loader.loadContent().trim(), type));

            sum += ExpUtils.getDocTermCount(doc);
        }
        averange = sum / files.size();
        return averange;
    }

}
