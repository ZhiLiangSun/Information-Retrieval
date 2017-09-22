package TRECParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;


public class FBISParser extends Parser {

    public FBISParser(File textFile) {
        super(textFile);
    }

    public FBISParser(String content) {
        super(content);
    }

    @Override
    protected String regularExp() {
        //This regular expression have two groups
        //group 1 -- DOCNO
        //group 2 -- TITLE
        //group 3 -- TEXT
        return "<DOC>[\\w\\W]*?<DOCNO>([\\w\\W]*?)</DOCNO>[\\w\\W]*?<HEADER>[\\w\\W]*?</HEADER>([\\w\\W]*?)</DOC>";
    }

    @Override
    protected String[] ResultImplement(Matcher matcher) {
        //Making record of a parsed document
        String[] group = new String[3];
        group[0] = matcher.group(1).trim();
        group[1] = matcher.group(2).replaceAll("<ABS.*?>.*?</ABS.*?>", "")
                .replaceAll("<DATE1.*?>.*?</DATE1.*?>", "")
                .replaceAll("<AU.*?>.*?</AU.*?>", "")
                .replaceAll("<HT.*?>.*?</HT.*?>", "")
                .replaceAll("<F.*?>[\\w\\W]*?</F.*?>", "")
                .replaceAll("<.*?>", "")
                .replaceAll("\\[.*?\\]", "")

                //extra insurance
                .replaceAll("\\&hyph;", "-")
                .replaceAll("\\&amp;", "&")
                .replaceAll("\\&.*?;", " ").trim();

        return group;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        FBISParser parser = null;
        Vector<String[]> result = null;
        FileWriter fw = null;

        ArrayList<String> file_list = new ArrayList<String>();

        //catch all path of file name in FR folder
        for (File f : new File(Path.Data_Path + "raw/TREC5/FBIS/").listFiles()) {
            file_list.add(f.getAbsolutePath());
        }

        for (String file : file_list) {
            parser = new FBISParser(new File(file));
            result = parser.getParsedResult();
            for (String[] k : result) {
                System.out.println(k[0]);
                fw = new FileWriter(Path.Data_Path + "raw_parsed/" + k[0]);
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
