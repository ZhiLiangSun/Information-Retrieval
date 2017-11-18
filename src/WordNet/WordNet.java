package WordNet;

import Utils.FileUtils;
import Utils.Path;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WordNet {
    public static LinkedHashMap<String, Float> showSynset(LinkedHashMap<String, Float> expansionList, String term) {
        try {
            System.out.print(term + ": ");
            float boost = 0.5f;
            List<String> stopWords = FileUtils.getStopWords();

            //org.apache.log4j.BasicConfigurator.configure();
            //Dictionary dictionary = Dictionary.getMapBackedInstance(Path.WORDNET_MAP_PATH);
            Dictionary dictionary = Dictionary.getFileBackedInstance(Path.WORDNET_DIR_PATH);
            IndexWord word = dictionary.lookupIndexWord(POS.NOUN, term);
            if (word == null)
                word = dictionary.lookupIndexWord(POS.VERB, term);
            if (word == null)
                word = dictionary.lookupIndexWord(POS.ADJECTIVE, term);
            if (word == null) {
                System.out.println();
                return expansionList;
            }

            List<Synset> senses = word.getSenses();
            for (int i = 0; i < senses.size(); i++) {
                Synset sense = senses.get(i);

                // Show Word (lexical) relations
                String synonym;
                for (int k = 0; k < sense.getWords().size(); k++) {
                    synonym = sense.getWords().get(k).getLemma().toLowerCase();

                    String[] s = synonym.split(" ");
                    if (s.length > 1) {
                        // splitted terms
                        for (int j = 0; j < s.length; j++) {
                            if (!expansionList.containsKey(s[j]) && !stopWords.contains(s[j])) {
                                expansionList.put(s[j], boost);
                                System.out.print(s[j] + " ");
                            }
                        }
                    } else {
                        if (synonym.contains("-")) {
                            if (!expansionList.containsKey(synonym.split("-")[0])) {
                                expansionList.put(synonym.split("-")[0], boost);
                                System.out.print(synonym.split("-")[0] + " ");
                            }
                            if (!expansionList.containsKey(synonym.split("-")[1])) {
                                expansionList.put(synonym.split("-")[1], boost);
                                System.out.print(synonym.split("-")[1] + " ");
                            }
                        } else if (!expansionList.containsKey(synonym) && !stopWords.contains(synonym)) {
                            expansionList.put(synonym, boost);
                            System.out.print(synonym + " ");
                        }
                    }
                }
            }
            System.out.println();
        } catch (JWNLException e) {
            e.printStackTrace();
        }

        return expansionList;
    }

    // for the test, remove later
    public static void main(String[] args) {
        LinkedHashMap<String, Float> expansionList = new LinkedHashMap<>();
        expansionList.put("eat",0.03f);
        expansionList.put("apple",0.03f);
        expansionList.put("car",0.01f);
        expansionList.put("pen",0.02f);
        int count = expansionList.size();
        List<String> list = new ArrayList<String>();
        for (Map.Entry<String, Float> entry : expansionList.entrySet()) {
            String key = entry.getKey();
            list.add(key);
        }
        for (int i = 0; i < count; i++) {
            expansionList = WordNet.showSynset(expansionList, list.get(i));
        }
        System.out.println("Synset done.");
    }
}
