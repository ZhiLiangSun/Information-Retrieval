package TRECParser;

import Utils.FileSaver;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;

public class QueryJudgementParser extends Parser {
    public QueryJudgementParser(File textFile) {
        super(textFile);
    }

    public QueryJudgementParser(String content) {
        super(content);
    }

    @Override
    protected String regularExp() {
        // TODO Auto-generated method stub
        return "(.+?)\\s.*?\\s(.+?)\\s(.+?)";
    }

    @Override
    protected String[] ResultImplement(Matcher matcher) {
        // TODO Auto-generated method stub
        String[] group = new String[3];
        group[0] = matcher.group(1).trim();
        group[1] = matcher.group(2).trim();
        group[2] = matcher.group(3).trim();
        return group;
    }

    public static void QueryJudgementParserSub(String filePath) throws IOException {
        File f = new File(filePath);
        QueryJudgementParser parser = new QueryJudgementParser(f);
        Vector<String[]> result = parser.getParsedResult();

        String QueryNo = null;
        String DocNo = null;
        Boolean Relevance = null;

        //File Variable Declaration
        String FileRoot = Path.Data_Path + "/answer/parsed";
        String TopicPath = null;
        String TopicRPath = null;
        String TopicNRPath = null;

        String currentTopic = "1";
        StringBuffer Rbuffer = null;
        StringBuffer NRbuffer = null;

        for (String[] k : result) {
            QueryNo = k[0];
            DocNo = k[1];
            Relevance = k[2].equals("1");


            if (!currentTopic.equals(QueryNo)) {
                //Save the previous one
                if (TopicPath != null) {
                    FileSaver Rsaver = new FileSaver(TopicPath + "/" + currentTopic + "RList");
                    Rsaver.saveContent(Rbuffer);

                    FileSaver NRsaver = new FileSaver(TopicPath + "/" + currentTopic + "NRList");
                    NRsaver.saveContent(NRbuffer);
                }


                //Create new session
                currentTopic = QueryNo;

                TopicPath = FileRoot + "/" + QueryNo;
                File dir = new File(TopicPath);
                dir.mkdir();

                TopicRPath = TopicPath + "/R";
                File Rdir = new File(TopicRPath);
                Rdir.mkdir();

                TopicNRPath = TopicPath + "/NR";
                File NRdir = new File(TopicNRPath);
                NRdir.mkdir();

                Rbuffer = new StringBuffer();
                NRbuffer = new StringBuffer();
            }

            if (Relevance) {
                Rbuffer.append(DocNo + "\r\n");
            } else {
                NRbuffer.append(DocNo + "\r\n");
            }

            //System.out.println(QueryNo + " " + DocNo + " " + Relevance);
        }

        FileSaver Rsaver = new FileSaver(TopicPath + "/" + currentTopic + "RList");
        Rsaver.saveContent(Rbuffer);

        FileSaver NRsaver = new FileSaver(TopicPath + "/" + currentTopic + "NRList");
        NRsaver.saveContent(NRbuffer);

    }

    public static void main(String[] args) throws IOException {
        String filePath = null;
        for (int i = 1; i < 6; i++) {
            filePath = Path.Data_Path + "/answer/qrels.trec6.adhoc.part" + i;
            QueryJudgementParserSub(filePath);
        }
    }
}
