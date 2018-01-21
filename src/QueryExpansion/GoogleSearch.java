package QueryExpansion;

import CoOccurrence.Pair;
import Utils.Path;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class GoogleSearch {
    protected final static double logN = Math.log(4.5e10);

    public static void main(String[] args) throws Exception {

        System.out.println(calculateDistance("apple", "computer"));
        System.out.println(calculateDistance("apple", "ios"));
        System.out.println(calculateDistance("apple", "animal"));
        System.out.println(calculateDistance("dog", "cat"));
        System.out.println(calculateDistance("dog", "maltese"));
        System.out.println(calculateDistance("Shakespeare", "Macbeth"));
        System.out.println(calculateDistance("android", "google"));
        System.out.println(calculateDistance("android", "apple"));
        System.out.println(calculateDistance("horse", "rider"));

    }

    private static double getResultsCount(final String query) throws IOException {
        String[] splitQuery = query.split(" ");
        int length = splitQuery.length;

        LinkedHashMap<String, Double> ngd = new LinkedHashMap<>();
        ArrayList<Pair> ngd_pair = new ArrayList<Pair>();

        BufferedReader br = new BufferedReader(new FileReader(Path.NGD_Path));
        String ngdLine = br.readLine();
        String[] split;
        while (ngdLine != null) {
            split = ngdLine.split(" ");
            if (split[0].equals("1")) {
                ngd.put(split[1], Double.valueOf(split[2]));
            } else {
                ngd_pair.add(new Pair(split[1], split[2], Double.valueOf(split[3])));
            }
            ngdLine = br.readLine();
        }

        if (length == 1) {
            if (ngd.containsKey(query))
                return ngd.get(query);
        } else {
            if (containsPair(ngd_pair, splitQuery[0], splitQuery[1])) {
                for (int i = 0; i < ngd_pair.size(); i++) {
                    if ((ngd_pair.get(i).getTerm1().equals(splitQuery[0]) && ngd_pair.get(i).getTerm2().equals(splitQuery[1]))
                            || (ngd_pair.get(i).getTerm1().equals(splitQuery[1]) && ngd_pair.get(i).getTerm2().equals(splitQuery[0]))) {
                        return ngd_pair.get(i).getNGDDistance();
                    }
                }
            }
        }


        double count = 0;

        final URL url;
        url = new URL("https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8"));

        final URLConnection connection = url.openConnection();

        connection.setConnectTimeout(60000);
        connection.setReadTimeout(60000);
        //put the browser name/version
        connection.addRequestProperty("User-Agent", "Google Chrome/40");
        //scanning a buffer from object returned by http request
        final Scanner reader = new Scanner(connection.getInputStream(), "UTF-8");
        //for each line in buffer
        while (reader.hasNextLine()) {
            final String line = reader.nextLine();

            //line by line scanning for "resultstats" field because we want to extract number after it
            if (!line.contains("\"resultStats\">"))
                continue;

            try {
                //finally extract the number convert from string to integer
                count = Double.parseDouble(line.split("\"resultStats\">")[1].split("<")[0].replaceAll("[^\\d]", ""));
                System.out.println(query + ": " + count);


                BufferedWriter bw = new BufferedWriter(new FileWriter(Path.NGD_Path, true));
                if (length == 1) {
                    bw.write(1 + " " + query + " " + count + "\r\n");
                } else {
                    bw.write(2 + " " + query + " " + count + "\r\n");
                }
                bw.close();
                return count;
            } finally {
                reader.close();
            }
        }
        reader.close();


        return 0;
    }

    public static Double calculateDistance(String term1, String term2) throws IOException {
        double distance = 0.0;
        double min = getResultsCount(term1);
        double max = getResultsCount(term2);
        double both = getResultsCount(term1 + " " + term2);

        // if necessary, swap the min and max
        if (max < min) {
            double temp = max;
            max = min;
            min = temp;
        }

        if (min > 0.0 && both > 0.0) {
            distance = (Math.log(max) - Math.log(both)) / (logN - Math.log(min));
        } else {
            distance = 1.0;
        }

        if (distance < 0.0) {
            distance = 0.0;
        }

        return distance;
    }

    private static boolean containsPair(Collection<Pair> pairs, String term1, String term2) {
        for (Pair pair : pairs) {
            if ((pair.getTerm1().equals(term1) && pair.getTerm2().equals(term2))
                    || (pair.getTerm1().equals(term2) && pair.getTerm2().equals(term1))) {
                return true;
            }
        }
        return false;
    }
}
