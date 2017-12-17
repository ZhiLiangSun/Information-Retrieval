package TRECParser;

import Utils.Path;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;

public class LATIMESParser extends Parser {

    public LATIMESParser(File textFile) {
        super(textFile);
    }

    public LATIMESParser(String content) {
        super(content);
    }

    @Override
    protected String regularExp() {
        // TODO Auto-generated method stub
        return "<DOC>[\\w\\W]*?<DOCNO>([\\w\\W]*?)</DOCNO>[\\w\\W]*?</DATE>([\\w\\W]*?)</DOC>";
    }

    @Override
    protected String[] ResultImplement(Matcher matcher) {
        // TODO Auto-generated method stub
        String[] group = new String[3];
        group[0] = matcher.group(1).trim();
        group[1] = matcher.group(2).replaceAll("\\&amp;", "&")
                .replaceAll("<LENGTH.*?>\\r\\n<P.*?>\\r\\n.*?\\r\\n</P.*?>\\r\\n</LENGTH.*?>", "")
                .replaceAll("<.*?>", "")
                .replaceAll("[^a-zA-Z ]", " ").toLowerCase()
                .replaceAll("\\s+", " ").trim();
        return group;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        LATIMESParser parser = null;
        Vector<String[]> result = null;
        FileWriter fw = null;
        ArrayList<String> file_list = new ArrayList<String>();

        //catch all path of file name in FR folder
        for (File f : new File(Path.Data_Path + "/raw/TREC5/LATIMES/").listFiles()) {
            file_list.add(f.getAbsolutePath());
        }

        for (String file : file_list) {
            parser = new LATIMESParser(new File(file));
            result = parser.getParsedResult();
            for (String[] k : result) {
                System.out.println(k[0]);
                fw = new FileWriter(Path.Data_Path + "/raw_parsed/" + k[0]);
                fw.write(k[1] + "\r\n");
                //System.out.println(k[1]);
                //System.out.println(k[2]);

                fw.close();
                //System.out.println();
                //break;
            }
        }
    }

}