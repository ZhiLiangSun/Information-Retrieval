package TRECParser;

public class Path {
    public static String Project_Path = System.getProperty("user.dir");
    // /Users/zlsun/IdeaProjects/Information-Retrieval
    public static String Data_Path = System.getProperty("user.dir") + "/TREC";
    // /Users/zlsun/IdeaProjects/Information-Retrieval/TREC
    public static String Test_Path = Project_Path + "/res/testing";

    public static void main(String[] args) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("Working Directory = " + System.getProperty("user.home"));
        System.out.println("Working Directory = " + Data_Path);
    }
}

