package TRECParser;

import IRLit.FileLoader;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Parser {
    protected String content;
    protected Pattern discrimanationPattern;
    //protected Vector<String[]> result = new Vector<String[]>();

    public Parser(File textFile) {
        FileLoader loader = new FileLoader(textFile);
        try {
            init(loader.loadContent());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Parser(String content) {
        init(content);
    }

    protected void init(String content) {
        this.content = content;
        this.ParseImplement();
    }

    protected abstract String regularExp();

    protected abstract String[] ResultImplement(Matcher matcher);


    protected void ParseImplement() {
        String regularExp = this.regularExp();
        this.discrimanationPattern = Pattern.compile(regularExp, Pattern.CASE_INSENSITIVE);
    }

    public Vector<String[]> getParsedResult() {
        Vector<String[]> result = new Vector<String[]>();
        Matcher matcher = this.discrimanationPattern.matcher(this.content);
        //System.out.println(matcher.matches());
        while (matcher.find()) {
            result.add(this.ResultImplement(matcher));
        }

        return result;
    }

}
