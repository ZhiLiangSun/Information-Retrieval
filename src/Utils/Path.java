package Utils;

public class Path {
    public static String Project_Path = System.getProperty("user.dir");
    // /Users/zlsun/IdeaProjects/Information-Retrieval
    public static String Data_Path = System.getProperty("user.dir") + "/TREC";
    // /Users/zlsun/IdeaProjects/Information-Retrieval/TREC
    public static String Answer_Path = Path.Data_Path + "/answer/parsed/";
    public static String Index_Path = Project_Path + "/res/Lucene/index/";
    public static String YIndex_Path = Project_Path + "/res/Lucene/Yindex/";

    public static String Stopwords_Path = Project_Path + "/res/stopwords.txt";
    public static String Test_Path = Project_Path + "/res/testing";
    public static String docTerm_Path = Project_Path + "/res/docTerm/";

    public static String word2vec_Path = "/Users/zlsun/PycharmProjects/Word2vec/res/";
    public static String word2vec_wPath = "C:/Users/Lab714/Desktop/Word2Vec/res/";

    public static String WORDNET_DIR_PATH = Project_Path + "/res/WordNet";

    public static void main(String[] args) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("Working Directory = " + System.getProperty("user.home"));
        System.out.println("Working Directory = " + Data_Path);
        System.out.println("Working Directory = " + Index_Path);
    }
}

