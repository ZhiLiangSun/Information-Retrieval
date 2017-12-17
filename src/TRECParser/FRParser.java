package TRECParser;

import Utils.Path;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;

public class FRParser extends Parser {

    public FRParser(File textFile) {
        super(textFile);
    }

    public FRParser(String content) {
        super(content);
    }

    @Override
    protected String regularExp() {
        // TODO Auto-generated method stub
        return "<DOC>[\\w\\W]*?<DOCNO>([\\w\\W]*?)</DOCNO>[\\w\\W]*?<TEXT>([\\w\\W]*?)</TEXT>";
    }

    @Override
    protected String[] ResultImplement(Matcher matcher) {
        // TODO Auto-generated method stub
        String[] group = new String[2];
        group[0] = matcher.group(1).trim();
        group[1] = matcher.group(2).replaceAll("<.*?>", "")
                .replaceAll("[^a-zA-Z ]", " ").toLowerCase()
                .replaceAll("\\bsect\\b"," ") //replace "sect" only
                .replaceAll("\\s+", " ")

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
        FRParser parser = null;
        Vector<String[]> result = null;
        FileWriter fw = null;
        ArrayList<String> file_list = new ArrayList<String>();
        String FRPath = Path.Data_Path + "/raw/TREC4/FR94/";
        File fs = new File(FRPath);
        String[] list = fs.list();

        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(".DS_Store")) //for Mac OS
                continue;
            for (File f : new File(FRPath + list[i]).listFiles()) {
                file_list.add(f.getAbsolutePath());
            }
        }

        for (String file : file_list) {
            parser = new FRParser(new File(file));
            result = parser.getParsedResult();
            for (String[] k : result) {
                System.out.println(k[0]);
                fw = new FileWriter(Path.Data_Path + "/raw_parsed/" + k[0]);
                fw.write(k[1]);
                //System.out.println(k[1]);
                //System.out.println(k[2]);

                fw.close();
                //System.out.println();
                //break;
            }
        }
    }
}
