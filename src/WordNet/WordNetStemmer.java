package WordNet;

import Utils.Path;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;

import java.util.HashMap;

public class WordNetStemmer {
    private Dictionary dic;
    private MorphologicalProcessor morph;
    private boolean IsInitialized = false;
    public HashMap<String, String> AllWords = null;

    /**
     * Creates a {@link WordNetStemmer} instance
     *
     * @param isLargeFile
     */
    public WordNetStemmer(boolean isLargeFile) {
        AllWords = new HashMap<String, String>();

        try {
            if (isLargeFile) {
                org.apache.log4j.BasicConfigurator.configure();
                dic = Dictionary.getMapBackedInstance(Path.WORDNET_MAP_PATH);
            } else {
                dic = Dictionary.getFileBackedInstance(Path.WORDNET_DIR_PATH);
            }

            morph = dic.getMorphologicalProcessor();
            IsInitialized = true;
        } catch (JWNLException e) {
            System.out.println("Error initializing Stemmer: " + e.toString());
            e.printStackTrace();
        }
    }

    private String stemWordWithWordNet(String word) {
        if (!IsInitialized)
            return word;
        if (word == null)
            return null;
        if (morph == null)
            morph = dic.getMorphologicalProcessor();

        IndexWord w;
        try {
            w = morph.lookupBaseForm(POS.VERB, word);
            if (w != null)
                return w.getLemma().toString();
            w = morph.lookupBaseForm(POS.NOUN, word);
            if (w != null)
                return w.getLemma().toString();
            w = morph.lookupBaseForm(POS.ADJECTIVE, word);
            if (w != null)
                return w.getLemma().toString();
            w = morph.lookupBaseForm(POS.ADVERB, word);
            if (w != null)
                return w.getLemma().toString();
        } catch (JWNLException e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Stem a single word tries to look up the word in the AllWords HashMap If
     * the word is not found it is stemmed with WordNet and put into AllWords
     *
     * @param word word to be stemmed
     * @return stemmed word
     */
    public String stem(String word) {
        word = word.toLowerCase();

        // check if we already know the word
        String stemmedword = (String) AllWords.get(word);
        if (stemmedword != null)
            return stemmedword; // return it if we already know it

        // don't check words with digits in them
        /*
         * if ( containsNumbers (word) == true ) stemmedword = null; else //
         * unknown word: try to stem it
         */
        stemmedword = stemWordWithWordNet(word);

        if (stemmedword != null) {
            // word was recognized and stemmed with wordnet:
            // add it to hashmap and return the stemmed word
            AllWords.put(word, stemmedword);
            return stemmedword;
        }

        // word could not be stemmed by wordnet,
        // thus it is no correct english word
        // just add it to the list of known words so
        // we won't have to look it up again
        AllWords.put(word, word);
        return word;
    }
}
