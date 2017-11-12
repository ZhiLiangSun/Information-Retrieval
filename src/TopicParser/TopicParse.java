package TopicParser;

import Utils.FileUtils;
import Utils.Path;

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

        String content = "";
        String porter = "";
        String stem_porter = "";
        int count = 0;

        for (int i = 301; i < 351; i++) {
            Topic_File = new FileReader(Path.Project_Path + "/res/OriginalShortTopic/" + i);
            content = content + i + " " + FileUtils.readFile(Path.Project_Path + "/res/OriginalShortTopic/" + i);
            BufferedReader buffer = new BufferedReader(Topic_File);
            String TopicSentence = buffer.readLine();
            TopicWords = p.run(TopicSentence);
            TopicWords = s.run(TopicWords);
            TopicWords = st.run(TopicWords);
            porter += i + " " + String.join(" ", TopicWords) + "\r\n";
            stem_porter += String.join(" ", TopicWords) + "\r\n";
            TREC_Format(TopicWords, i);
        }

        FileUtils.writeFile(Path.Project_Path + "/res/porter topics.txt", porter);
        FileUtils.writeFile(Path.Project_Path + "/res/topics.txt", content);
        FileUtils.writeFile(Path.Project_Path + "/res/stem topics.txt", stem_porter);
        Topic_File.close();
    }
}
