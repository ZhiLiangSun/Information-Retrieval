package TRECParser;

import Utils.FileSaver;


import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;

public class QueryParser extends Parser {

    public QueryParser(File textFile) {
        super(textFile);
    }

    public QueryParser(String content) {
        super(content);
    }

    @Override
    protected String regularExp() {
        // TODO Auto-generated method stub
        return "<top>[\\w\\W]*?<num>([\\w\\W]*?)<title>([\\w\\W]*?)<desc>([\\w\\W]*?)<narr>([\\w\\W]*?)</top>";
    }

    @Override
    protected String[] ResultImplement(Matcher matcher) {
        // TODO Auto-generated method stub
        String[] group = new String[4];
        group[0] = matcher.group(1).replaceFirst("Number:", "").trim();
        group[1] = matcher.group(2).trim();
        group[2] = matcher.group(3).replaceFirst("Description:", "").trim();
        group[3] = matcher.group(4).replaceFirst("Narrative:", "").trim();
        return group;
    }


    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        QueryParser parser = new QueryParser(new File(Path.Data_Path + "/topics.301-350"));
        Vector<String[]> result = parser.getParsedResult();


        String QueryID = null;
        String QueryTitle = null;
        String QueryDesc = null;
        String QueryNarrtive = null;


        int c = 0;
        for (String[] k : result) {
            StringBuffer shortQuery = new StringBuffer();
            StringBuffer midQuery = new StringBuffer();
            StringBuffer longQuery = new StringBuffer();

            QueryID = k[0];
            QueryTitle = k[1];
            QueryDesc = k[2];
            QueryNarrtive = k[3];

            // Short Query
            shortQuery.append(QueryTitle);

            // Mid Query
            midQuery.append(QueryTitle + "\r\n");
            midQuery.append(QueryDesc);

            // Long Query
            longQuery.append(QueryTitle + "\r\n");
            longQuery.append(QueryDesc + "\r\n");
            longQuery.append(QueryNarrtive);

            FileSaver shortSaver = new FileSaver("res/OriginalShortTopic/" + QueryID);
            shortSaver.saveContent(shortQuery);

            ++c;
            
        }

        System.out.println("Total " + c + " documents parsed.");
    }

}
