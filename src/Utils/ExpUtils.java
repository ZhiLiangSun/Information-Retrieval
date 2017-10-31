package Utils;

import org.apache.lucene.document.Document;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ExpUtils {

    public static Document doc = null;
    public static int docTermCount = 0;

    public static void printTimeUsage(Date start, Date end) {
        System.out.println("===========  " + "Finished" + "  ===========");

        long milliseconds = end.getTime() - start.getTime();
        int h, m, s;
        s = (int) (milliseconds / 1000);
        h = s / 3600;
        s %= 3600;
        m = s / 60;
        s %= 60;

        System.out.println(h + " hours " + m + " minutes " + s + " seconds(" + milliseconds + " milliseconds)");
    }


    public static String getQueryString(int queryNumber) {
        Map<Integer, String> topics = getTopicList("topics");

        String queryString = topics.get(queryNumber).trim();
        if (queryNumber != 344) {
            queryString = queryString.replaceAll("[-/]", " ");

            if (queryNumber!=339) {
                queryString = queryString.replaceAll("[^A-Za-z ]", "");
            }
        }

        return queryString;
    }

    public static Map<Integer, String> getTopicList(String prop) {
        Map<Integer, String> topics = new HashMap<Integer, String>();
        File file = new File(Path.Project_Path + "/res/" + prop + ".txt");
        FileReader fr = null;
        BufferedReader br = null;

        try {
            // initial reader
            fr = new FileReader(file);
            br = new BufferedReader(fr);

            String line = br.readLine();
            while (line != null) {
//                String[] splitedLine = line.split(" ");

                topics.put(Integer.valueOf(line.substring(0, 3)), line.substring(4));
                line = br.readLine();
            }

            // close reader
            br.close();
            fr.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return topics;

    }

    public static void main(String[] args) {
        System.out.println(getTopicList("porter topics"));

    }
    public static int getDocTermCount(Document doc) {
        return getDocTermCount(doc, false);
    }

    public static int getDocTermCount(Document doc, boolean cache) {
        int docTermCount;

        if (cache) {
            if (doc.equals(ExpUtils.doc)) {
                return ExpUtils.docTermCount;
            }
            // cache is empty
            else {
                // Calculate docTermCount
                docTermCount = getDocTermCount(doc, false);
                // cache values
                ExpUtils.doc = doc;
                ExpUtils.docTermCount = docTermCount;
                return docTermCount;
            }
        }
        else {
            StringBuffer strb = new StringBuffer();
            String[] txt = doc.getValues("content");
            for (int i = 0; i < txt.length; i++) {
                strb.append(txt[i]);
            }
            StringTokenizer tknzr = new StringTokenizer(strb.toString());
            docTermCount = tknzr.countTokens();
            return docTermCount;
        }
    }
}
