package TopicParser;

import java.util.ArrayList;
import java.util.Arrays;

public class Punctuation {
    public ArrayList<String> run(String s) {

        String[] words = s.replaceAll("[^\\w-/\\s]", "")
                .toLowerCase()
                .replaceAll("[-/]", " ").split(" ");
        ArrayList<String> result = new ArrayList<String>(Arrays.asList(words));

        return result;
    }
}
