package CoOccurrence;

import Utils.Defs;
import Utils.Path;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.common.io.Files;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

import java.io.*;
import java.util.*;

public class CoOccurrence {
    String fromPhrasesDir = Path.Co_Path + "FromPhrases/";
    String fromDocumentsDir = Path.Co_Path + "FromDocuments/";
    String fromSentencesDir = Path.Co_Path + "FromSentences/";
    int queryNumber;
    List<String> stopWords;

    public CoOccurrence(int queryNumber, List<String> stopWords) {
        this.queryNumber = queryNumber;
        this.stopWords = stopWords;
    }

    public List<Pair> getCoOccurrencePairsFromDocuments(int threshold, List<Integer> top20RelevantDocIdList, IndexSearcher searcher) {
        File file = new File(fromDocumentsDir + queryNumber);
        boolean isFileExists = file.exists();

        List<Table<String, String, Integer>> tables = new ArrayList<Table<String, String, Integer>>();

        if (!isFileExists) {
            // 20 matrices
            for (int i = 0; i < top20RelevantDocIdList.size(); i++) {
                try {
                    tables.add(getMatrixFromDocument(searcher, top20RelevantDocIdList.get(i)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return getCoOccurrencePairs(isFileExists, threshold, tables);
    }

    /**
     * get Co-Occurrence pairs from phrases
     *
     * @param threshold
     * @return Co-Occurrence pairs
     */
    public List<Pair> getCoOccurrencePairsFromPhrases(int threshold, List<String> top20RelevantDocList) {
        File file = new File(fromPhrasesDir + queryNumber);
        boolean isFileExists = file.exists();

        if (!isFileExists) {
            generateCoOccurrencePairsFromPhrases(top20RelevantDocList);
        }

        List<Pair> coOccurrencePairs = new ArrayList<Pair>();
        String pattern = "[A-Za-z]+$";

        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String line = null;
            String[] splitString;
            String term1;
            String term2;
            int count;

            while ((line = br.readLine()) != null) {
                splitString = line.split("\t");
                term1 = splitString[0];
                term2 = splitString[1];
                count = Integer.valueOf(splitString[2]);

                // if co-occurrence count is greater than threshold then add co-occurrence pair to list
                if (count >= threshold) {
                    // remove stop words and numbers
                    if (!stopWords.contains(term1) && !stopWords.contains(term2) &&
                            !containsPair(coOccurrencePairs, term1, term2) && !containsPair(coOccurrencePairs, term2, term1) &&
                            term1.matches(pattern) && term2.matches(pattern)) {
                        coOccurrencePairs.add(new Pair(term1, term2, count));
                    }
                }
            }

            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // sort coCurrencePairs by co-occurrence count
        Collections.sort(coOccurrencePairs, new PairComparator());
        for (int i = 0; i < coOccurrencePairs.size(); i++) {
            System.out.println(coOccurrencePairs.get(i).getTerm1() + ", " + coOccurrencePairs.get(i).getTerm2() + " " + coOccurrencePairs.get(i).getCoOccurrenceCount());
        }

        return coOccurrencePairs;
    }

    /**
     * generate Co-Occurrence pairs from phrases
     */
    public void generateCoOccurrencePairsFromPhrases(List<String> top20RelevantDocList) {
        Configuration conf = new Configuration();
        //conf.set("mapreduce.output.basename", "xxxx"); // part-r-00000 to xxxx-r-00000

        Job job;
        try {
            job = new Job(conf, "Co-Occurrence");
            job.setJarByClass(CoOccurrence.class);

            job.setMapperClass(MyMapper.class);
            job.setMapOutputKeyClass(TextPair.class);
            job.setMapOutputValueClass(IntWritable.class);

            job.setCombinerClass(MyReducer.class);

            // the reduce output is Text, IntWritable
            job.setReducerClass(MyReducer.class);
            job.setOutputKeyClass(TextPair.class);
            job.setOutputValueClass(IntWritable.class);

            File source = new File(Path.Co_Answer_Path + queryNumber);
            File[] docs = source.listFiles();
            for (int i = 0; i < docs.length; i++) {
                if (top20RelevantDocList.contains(docs[i].getName())) {
                    FileInputFormat.addInputPath(job, new org.apache.hadoop.fs.Path(docs[i].getAbsolutePath()));
                }
            }

            FileOutputFormat.setOutputPath(job, new org.apache.hadoop.fs.Path(Path.Test_Path));
            job.waitForCompletion(true);

            // rename the file
            File oldFile = new File(Path.Test_Path + "/part-r-00000");
            File newFile = new File(oldFile.getParent(), String.valueOf(queryNumber));
            Files.move(oldFile, newFile);
            Files.move(newFile, new File(fromPhrasesDir + queryNumber));

            File test = new File(Path.Test_Path);

            // delete unnecessary files
            File[] files = test.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
            test.delete();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<Pair> getCoOccurrencePairs(Boolean isFileExists, int threshold, List<Table<String, String, Integer>> tables) {
        List<Pair> coOccurrencePairs = new ArrayList<Pair>();

        if (!isFileExists) {
            Table<String, String, Integer> matrix = TreeBasedTable.create();

            for (Table<String, String, Integer> table : tables) {
                for (String r : table.rowKeySet()) {
                    for (Map.Entry<String, Integer> c : table.row(r).entrySet()) {
                        if (!matrix.contains(r, c.getKey())) {
                            matrix.put(r, c.getKey(), c.getValue());
                        } else {
                            if (r.equals(c.getKey())) {
                                matrix.put(r, c.getKey(), 0);
                            } else {
                                matrix.put(r, c.getKey(), matrix.get(r, c.getKey()) + 1);
                            }
                        }
                    }
                }
            }

            // write to txt
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(fromDocumentsDir + queryNumber));
                StringBuffer sb = new StringBuffer();

                int count = 0;
                for (String r : matrix.rowKeySet()) {
                    for (Map.Entry<String, Integer> c : matrix.row(r).entrySet()) {
                        if (!containsPair(coOccurrencePairs, r, c.getKey())) {
                            sb.append(r + " " + c.getKey() + " " + c.getValue() + "\n");

                            count++;
                            if (count == 2000000) {
                                count = 0;
                                bw.append(sb);
                                sb.delete(0, sb.length());
                                System.gc();
                            }
                        }
                    }
                }

                bw.append(sb);
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // read co-occurrence pairs
        try {
            BufferedReader br = new BufferedReader(new FileReader(fromDocumentsDir + queryNumber));
            String line = br.readLine();
            String[] pair;
            while (line != null) {
                pair = line.split(" ");
                // if co-occurrence count is greater than threshold then add co-occurrence pair to list
                if (Integer.parseInt(pair[2]) >= threshold) {
                    if (!stopWords.contains(pair[0]) && !stopWords.contains(pair[1])
                            && !containsPair(coOccurrencePairs, pair[0], pair[1]) && !containsPair(coOccurrencePairs, pair[1], pair[0])) {
                        coOccurrencePairs.add(new Pair(pair[0], pair[1], Integer.parseInt(pair[2])));
                        //System.out.println(pair[0] + " " + pair[1] + " " + pair[2]);
                    }
                }

                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // sort coCurrencePairs by co-occurrence count
        Collections.sort(coOccurrencePairs, new PairComparator());
        for (int i = 0; i < coOccurrencePairs.size(); i++) {
            System.out.println(coOccurrencePairs.get(i).getTerm1() + ", " + coOccurrencePairs.get(i).getTerm2() + " " + coOccurrencePairs.get(i).getCoOccurrenceCount());
        }

        return coOccurrencePairs;
    }

    private Table<String, String, Integer> getMatrixFromDocument(IndexSearcher searcher, int docNo) throws IOException {
        Table<String, String, Integer> table = TreeBasedTable.create();

        IndexReader indexReader = searcher.getIndexReader();
        Bits liveDocs = MultiFields.getLiveDocs(indexReader);
        TermsEnum termEnum = MultiFields.getTerms(indexReader, Defs.FIELD).iterator(null);
        BytesRef bytesRef;

        while ((bytesRef = termEnum.next()) != null) {
            if (termEnum.seekExact(bytesRef)) {
                DocsEnum docsEnum = termEnum.docs(liveDocs, null);
                if (docsEnum != null) {
                    int docn;

                    while ((docn = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                        if (docNo == docn) {
                            String term = bytesRef.utf8ToString();
                            //int freq = docsEnum.freq();

                            if (!table.containsColumn(term)) {
                                table.put(term, term, 0);
                            }
                            //System.out.println(term + " in doc " + docn + ": " + freq);
                        }
                    }
                }
            }
        }

        for (String r : table.rowKeySet()) {
            for (String c : table.rowKeySet()) {
                table.put(r, c, 1);
                //System.out.println(count++);
            }
        }

        for (String r : table.rowKeySet()) {
            table.put(r, r, 0);
        }

        // Deprecated
        //for (String r : table.rowKeySet()) {
        //    for (String c : table.columnKeySet()) {
        //        if (r.equals(c)) {
        //            System.out.println(r +" "+ c +" "+ 0);
        //            table.put(r, c, 0);
        //        }
        //        else {
        //            System.out.println(r +" "+ c +" "+ 1);
        //            table.put(r, c, 1);
        //        }
        //    }
        //}

        return table;
    }

    private boolean containsPair(Collection<Pair> pairs, String term1, String term2) {
        for (Pair pair : pairs) {
            if ((pair.getTerm1().equals(term1) && pair.getTerm2().equals(term2))
                    || (pair.getTerm1().equals(term2) && pair.getTerm2().equals(term1))) {
                return true;
            }
        }
        return false;
    }

}


