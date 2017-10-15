package TopicParser;

import java.util.ArrayList;

public class Stemming {
    public ArrayList<String> run(ArrayList<String> topicWords) {
        Stemmer s = new Stemmer();
        String word;
        for (int j = 0; j < topicWords.size(); j++) {
            word = topicWords.get(j);
            char temp[] = new char[word.length()];
            temp = word.toCharArray();
            s.add(temp, word.length());
            s.stem();
            topicWords.set(j, s.toString());
        }
        return topicWords;
    }
}
