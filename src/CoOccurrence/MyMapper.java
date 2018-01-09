package CoOccurrence;

import WordNet.WordNetStemmer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class MyMapper extends Mapper<LongWritable, Text, TextPair, IntWritable> {
    private final IntWritable one = new IntWritable(1);
    private Text word0 = new Text();
    private Text word1 = new Text();
    private String pattern = "[^a-zA-Z0-9 ]";
    private WordNetStemmer wordNetStemmer = new WordNetStemmer(false);

    @Override
    public void map(LongWritable inKey, Text inValue, Context context) throws IOException, InterruptedException {
        String line = inValue.toString();
        line = regular(line);
        line = line.replaceAll(pattern, "");
        line = line.toLowerCase();

        String[] str = line.split(" +");
        for (int i = 0; i < str.length - 1; i++) {
            word0.set(wordNetStemmer.stem(str[i]));
            word1.set(wordNetStemmer.stem(str[i + 1]));
            TextPair pair = new TextPair(word0, word1);
            context.write(pair, one);
        }
    }

    private String regular(String line) {
        if (line.startsWith("<DOC>") || line.startsWith("<DOCNO>") || line.startsWith("<DOCID>")
                || line.startsWith("<DATE>") || line.startsWith("<!--") || line.startsWith("<PARENT>")
                || line.startsWith("<BYLINE>") || line.startsWith("<AU>") || line.startsWith("<DATE1>")
                || line.startsWith("</") || line.startsWith("<HT> ") || line.startsWith("<F")
                || line.startsWith("<P>") || line.startsWith("<GRAPHIC>") || line.startsWith("<SECTION>")
                || line.startsWith("<LENGTH>") || line.startsWith("<CENTER><PRE>") || line.startsWith("<TYPE>")
                || line.startsWith("<FLD001>") || line.startsWith("<FLD002>") || line.startsWith("<FLD003>")
                || line.startsWith("<ABS>") || line.startsWith("Document Type:") || line.startsWith("Article Type:")
                || line.startsWith("Language:") || line.startsWith("<SO>") || line.startsWith("<TI>")
                || line.startsWith("Daily Report") || line.startsWith("BFN") || line.startsWith("<TABLE>")
                || line.startsWith("<PAGE>") || line.startsWith("<TIME>") || line.startsWith("<HEADER>")
                || line.startsWith("<PUB>") || line.startsWith("<PROFILE>") || line.startsWith("<XX>")
                || line.startsWith("<IN>") || line.startsWith("<CN>") || line.startsWith("<TP>")
                || line.startsWith("<FRFILING>") || line.startsWith("<SIGNJOB>") || line.startsWith("<SIGNER>")
                || line.startsWith("<BILLING>") || line.startsWith("<USDEPT>")
                //|| line.startsWith("<H3>") || line.startsWith("<H2>") || line.startsWith("<H4>") || line.startsWith("<H5>")
                || line.startsWith("<HEADLINE>") || line.startsWith("<DATELINE>") || line.startsWith("<CENTER>")
                || line.startsWith("<TEXT>") || line.startsWith("<TTL>")) {
            line = "";
        } else if (line.startsWith("<DOCTITLE>")) {
            line = line.substring(10);
            if (line.endsWith("</DOCTITLE>")) {
                line = line.substring(0, line.length() - 11);
            }
        } else if (line.startsWith("<HT><F P=107><PHRASE>")) {
            line = line.substring(21);
            line = line.substring(0, line.length() - 18);
        } else if (line.startsWith("FT"))
            line = line.substring(2);
        else if (line.startsWith("Document Title:"))
            line = line.substring(15);
        return line;
    }
}
