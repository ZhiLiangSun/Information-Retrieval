package TopicParser;

import TRECParser.Path;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TopicParse {
    private static void TREC_Format(ArrayList<String> topicWords, int i) throws IOException {
        FileWriter fw = new FileWriter(Path.Project_Path + "/res/ShortTopicsFormated/" + i, false);
        String result = "<DOC " + i + ">\r\n";
        for (String word : topicWords) {
            result = result + word + "\r\n";
        }
        result = result + "</DOC>";


        fw.write(result);
        fw.flush();
        fw.close();
    }

    public static void main(String[] args) throws IOException {
        FileReader Topic_File = null;
        Punctuation p = new Punctuation();
        Stopwords s = new Stopwords();
        Stemming st = new Stemming();
        ArrayList<String> TopicWords;

        for (int i = 301; i < 351; i++) {
            Topic_File = new FileReader(Path.Project_Path + "/res/OriginalShortTopic/" + i);
            BufferedReader buffer = new BufferedReader(Topic_File);
            String TopicSentence = buffer.readLine();
            TopicWords = p.run(TopicSentence);
            TopicWords = s.run(TopicWords);
            TopicWords = st.run(TopicWords);
            TREC_Format(TopicWords, i);
        }
        Topic_File.close();

    }
}
