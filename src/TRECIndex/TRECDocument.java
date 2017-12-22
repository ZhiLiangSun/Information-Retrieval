package TRECIndex;

import Utils.Defs;
import Utils.FileLoader;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;

import java.io.IOException;

public class TRECDocument {
    public static Document Document(FileLoader loader, String docNo) throws IOException {
        Document doc = new Document();

        FieldType type = new FieldType();
        type.setIndexed(true);
        type.setStored(true);
        type.setStoreTermVectors(true);

        doc.add(new Field(Defs.FIELD, loader.loadContent().trim(), type));
        doc.add(new StringField("DOCNO", docNo, Field.Store.YES));

        return doc;
    }

}
