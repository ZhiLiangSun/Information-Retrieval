package Utils;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExpUtils {


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
}
