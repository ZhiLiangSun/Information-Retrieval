package TopicParser;

import TRECParser.Path;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Stopwords {
    public ArrayList<String> run(ArrayList<String> topicWords) throws IOException {

        FileReader StopList_File = new FileReader(Path.Project_Path + "/res/stoplist.dft");
        BufferedReader buffer = new BufferedReader(StopList_File);
        ArrayList<String> stopwords = new ArrayList<String>();

        while (buffer.ready()) {
            stopwords.add(buffer.readLine());
        }
        StopList_File.close();

        for (int j = 0; j < stopwords.size(); j++) {
            if (topicWords.contains(stopwords.get(j))) {
                topicWords.remove(stopwords.get(j)); //remove it
            }
        }

        return topicWords;
    }
}
