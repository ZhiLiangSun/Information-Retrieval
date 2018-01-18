package QueryExpansion;

import Utils.Defs;
import Utils.FileUtils;
import WordNet.WordNet;
import CoOccurrence.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.TFIDFSimilarity;

import java.io.IOException;
import java.util.*;

public class QueryExpansion {

    /**
     * Indicates which method to use for QE
     */
    public static final String METHOD_FLD = "QE.method";
    public static final String SUN_METHOD = "Sun";
    public static final String ROCCHIO_METHOD = "Rocchio";
    public static final String ORIGINAL_METHOD = "Original";
    /**
     * how much importance of document decays as doc rank gets higher. decay = decay * rank 0 - no decay
     */
    public static final String DECAY_FLD = "QE.decay";
    /**
     * Number of documents to use
     */
    public static final String DOC_NUM_FLD = "QE.doc.num";
    /**
     * Number of terms to produce
     */
    public static final String TERM_NUM_FLD = "QE.term.num";
    /**
     * Rocchio Params
     */
    public static final String ROCCHIO_ALPHA_FLD = "rocchio.alpha";
    public static final String ROCCHIO_BETA_FLD = "rocchio.beta";
    public static final String ROCCHIO_GAMMA_FLD = "rocchio.gamma";

    private int querynumber;
    private IndexSearcher searcher;
    private Properties prop;
    private Analyzer analyzer;
    private TFIDFSimilarity similarity;
    private Vector<TermQuery> expandedTerms;
    private LinkedHashMap<String, Float> expansionList;

    public List<String> top20RDocList;
    public List<String> top20NRDocList;
    public List<Integer> top20RDocIdList;
    public List<Integer> top20NRDocIdList;
    public List<Integer> rDocOriginalRank;
    public List<Integer> nrDocOriginalRank;
    public List<String> stopWords;
    public List<String> rTermsLists;
    public List<Integer> docOriginalRank;

    String method;
    int termNum = 200; //Rocchio term number

    //boosts
    float coOccurrencePairBoost = 1f;

    public QueryExpansion(String method, int querynumber, IndexSearcher searcher, Properties prop, Analyzer analyzer, TFIDFSimilarity similarity) {

        this.method = method;
        this.querynumber = querynumber;
        this.searcher = searcher;
        this.prop = prop;
        this.analyzer = analyzer;
        this.similarity = similarity;

        top20RDocList = new ArrayList<>();
        top20NRDocList = new ArrayList<>();
        top20RDocIdList = new ArrayList<>();
        top20NRDocIdList = new ArrayList<>();
        rDocOriginalRank = new ArrayList<>();
        nrDocOriginalRank = new ArrayList<>();
        expandedTerms = new Vector<>();
        expansionList = new LinkedHashMap<>();
        stopWords = FileUtils.getStopWords();
        rTermsLists = new ArrayList<>();

    }

    public Query expandQuerySun(String queryStr, TopDocs hits, boolean flag) throws QueryNodeException, IOException, ParseException {

        docOriginalRank = new ArrayList<>();

        //get top 20 relevant document and top 20 non-relevant document
        getTopDoc(hits, flag);

        //get Docs to be used in query expansion
        Vector<Document> vHits = getDocs(hits, docOriginalRank);

        //Load Necessary Values from Properties
        float alpha = Float.valueOf(prop.getProperty(QueryExpansion.ROCCHIO_ALPHA_FLD)).floatValue();
        float decay = Float.valueOf(prop.getProperty(QueryExpansion.DECAY_FLD, "0.0")).floatValue();
        int docNum = Integer.valueOf(prop.getProperty(QueryExpansion.DOC_NUM_FLD)).intValue();

        //Create combine documents term vectors - sum (rel term vectors)
        Vector<QueryTermVector> docsTermVector = getDocsTerms(vHits, docNum);

        //setBoost of docs terms
        Vector<TermQuery> docsTerms = setBoost(docsTermVector, alpha, decay);

        //setBoost of query terms
        //Get queryTerms from the query
        QueryTermVector queryTermsVector = new QueryTermVector(queryStr, analyzer);
        Vector<TermQuery> queryTerms = setBoost(queryTermsVector, alpha);

        //combine weights according to expansion formula
        //combine queryTerm and docsTerm boosts
        Vector<TermQuery> expandedQueryTerms = combineOrNot(queryTerms, docsTerms, flag);

        //store relevant terms
        if (flag) {
            for (int i = 0; i < expandedQueryTerms.size(); i++) {
                String[] rterm = expandedQueryTerms.get(i)
                        .toString(Defs.FIELD).replace("^", ",").split(",");
                rTermsLists.add(rterm[0]);
            }
            //put querystring into rTermsLists
            String querytmp = queryStr.toLowerCase();
            String[] queryStringSplit = querytmp.split(" ");
            for (int j = 0; j < queryStringSplit.length; j++) {
                if (!rTermsLists.contains(queryStringSplit[j])) {
                    rTermsLists.add(queryStringSplit[j]);
                }
            }
        }
        //remove relevant terms from non-relevant terms
        else {
            for (int i = 0, size = expandedQueryTerms.size(); i < size; i++) {
                String[] nrterm = expandedQueryTerms.get(i)
                        .toString(Defs.FIELD).replace("^", ",").split(",");
                if (rTermsLists.contains(nrterm[0])) {
                    expandedQueryTerms.remove(i);
                    i--;
                    size--;
                }
            }
        }

        Comparator<Object> comparator = new QueryBoostComparator();
        Collections.sort(expandedQueryTerms, comparator);

        setExpandedTerms(expandedQueryTerms);

        //get top 20 terms
        boolean reExport = false;
        if (reExport) {
            ExportTerm ept = new ExportTerm();
            if (flag)
                ept.exportxt(expandedQueryTerms, this.querynumber + "r");
            else
                ept.exportxt(expandedQueryTerms, this.querynumber + "nr");
        }

        Query expandedQuery = mergeQueriesSun(expandedQueryTerms, flag);
        return expandedQuery;
    }

    public Query mergeQueriesSun(Vector<TermQuery> termQueries, boolean flag) throws QueryNodeException, IOException, ParseException {

        expansionList = new LinkedHashMap<>();
        LinkedHashMap<String, Float> Final_W2v;

        if (flag) {
            //Original query expansion
            System.out.println("-------------------------------------");
            System.out.println("------Original query Expansion-------");
            System.out.println("-------------------------------------");

            ExportTerm ept = new ExportTerm();
            LinkedHashMap<String, Float> original_w2v = ept.syn_Query(this.expandedTerms, this.querynumber);

            //DocTerm expansion
            System.out.println("-------------------------------------");
            System.out.println("------DocTerm query Expansion-------");
            System.out.println("-------------------------------------");

            LinkedHashMap<String, Float> docTerm_w2v = ept.syn_docTerm(querynumber);
            docTerm_w2v.putAll(original_w2v);
            Final_W2v = docTerm_w2v;


            // TFIDF expansion
            System.out.println("-------------------------------------");
            System.out.println("-----------TFIDF Expansion-----------");
            System.out.println("-------------------------------------");

            int threshold = getThreshold(flag);
            int index = 0;

            while (true) {
                TermQuery termQuery = termQueries.elementAt(index);
                Term term = termQuery.getTerm();
                String termString = term.text().toLowerCase();

                if (stopWords.contains(termString)) {
                    continue;
                }

                if (termQuery.getBoost() > threshold) {
                    addExpandedTerms(termString, termQuery.getBoost());
                    System.out.println(termString + " " + termQuery.getBoost());
                }

                if (termQuery.getBoost() < threshold)
                    break;

                index++;
            }

            //WordNet Expansion
            System.out.println("-------------------------------------");
            System.out.println("**********WordNet Expansion**********");
            System.out.println("-------------------------------------");

            int count = expansionList.size();
            List<String> list = new ArrayList<String>();
            for (Map.Entry<String, Float> entry : expansionList.entrySet()) {
                String key = entry.getKey();
                list.add(key);
            }

            for (int i = 0; i < count; i++) {
                expansionList = WordNet.showSynset(expansionList, list.get(i));
            }

            //Co-occurrence Expansion
            System.out.println("-------------------------------------");
            System.out.println("Co-occurrence Expansion from Documents");
            System.out.println("-------------------------------------");

            CoOccurrence coOccurrence = new CoOccurrence(querynumber, stopWords);
            List<Pair> coOccurrencePairs;

            coOccurrencePairs = coOccurrence.getCoOccurrencePairsFromDocuments(10,
                    top20RDocIdList, searcher);
            calculateCoOccurrencePairSemanticRelation(coOccurrencePairs);

            System.out.println("-------------------------------------");
            System.out.println("Co-occurrence Expansion from Phrases");
            System.out.println("-------------------------------------");

            coOccurrencePairs = coOccurrence.getCoOccurrencePairsFromPhrases(10, top20RDocList);
            calculateCoOccurrencePairSemanticRelation(coOccurrencePairs);

            expansionList = filterExpandedTerms(Final_W2v);

            //append to buffer and toString()
            String targetStr = MaptoBuffer(this.expansionList);
            setExpandedTerms(targetStr);

            Query query = new QueryParser(Defs.FIELD, analyzer).parse(targetStr);
            return query;
        } else {
            Query query = new QueryParser(Defs.FIELD, analyzer).parse(" ");
            return query;
        }
    }

    private void calculateCoOccurrencePairSemanticRelation(List<Pair> coOccurrencePairs) throws IOException {
        float ngdExpectation = 0.7f;

        for (int i = 0; i < coOccurrencePairs.size(); i++) {
            String term1 = coOccurrencePairs.get(i).getTerm1();
            String term2 = coOccurrencePairs.get(i).getTerm2();

            if (expansionList.containsKey(term1) || expansionList.containsKey(term2)) {
                addExpandedTerms(term1, coOccurrencePairBoost);
                addExpandedTerms(term2, coOccurrencePairBoost);
            } else {
                // calculate NGD
                for (Map.Entry<String, Float> entry : expansionList.entrySet()) {
                    String key = entry.getKey();
                    if (GoogleSearch.calculateDistance(term1, key) < ngdExpectation) {
                        addExpandedTerms(term1, coOccurrencePairBoost);
                        addExpandedTerms(term2, coOccurrencePairBoost);
                        break;
                    }
                    if (GoogleSearch.calculateDistance(term2, key) < ngdExpectation) {
                        addExpandedTerms(term1, coOccurrencePairBoost);
                        addExpandedTerms(term2, coOccurrencePairBoost);
                        break;
                    }
                }
            }
        }
    }

    public Vector<Document> getDocs(TopDocs hits, List<Integer> DocOriginalRank) throws IOException {
        Vector<Document> vHits = new Vector<>();
        // Extract only as many docs as necessary
        int docNum = Integer.valueOf(prop.getProperty(QueryExpansion.DOC_NUM_FLD)).intValue();

        // Convert Hits -> Vector
        for (int i = 0; ((i < docNum) && (i < hits.scoreDocs.length) && (i < DocOriginalRank.size())); i++) {
            vHits.add(searcher.doc(hits.scoreDocs[DocOriginalRank.get(i)].doc));
        }

        return vHits;
    }

    public void getTopDoc(TopDocs hits, boolean flag) throws IOException {

        ScoreDoc[] scoreDocs = hits.scoreDocs;
        List<String> rList;
        rList = FileUtils.getRelevantDocList(querynumber);

        Document doc;
        String docNo;
        if (flag) {
            for (int i = 0; i < scoreDocs.length; i++) {
                doc = searcher.doc(scoreDocs[i].doc);
                docNo = doc.get("DOCNO");

                if (rList.contains(docNo) && top20RDocIdList.size() < 20) {
                    this.top20RDocList.add(docNo);
                    this.top20RDocIdList.add(scoreDocs[i].doc);
                    this.rDocOriginalRank.add(i);
                } else if (!rList.contains(docNo) && top20NRDocIdList.size() < 20) {
                    this.top20NRDocList.add(docNo);
                    this.top20NRDocIdList.add(scoreDocs[i].doc);
                    this.nrDocOriginalRank.add(i);
                }
            }
            docOriginalRank = rDocOriginalRank;
        } else
            docOriginalRank = nrDocOriginalRank;
    }

    public Vector<QueryTermVector> getDocsTerms(Vector<Document> hits, int docsRelevantCount) throws IOException {
        Vector<QueryTermVector> docsTerms = new Vector<QueryTermVector>();

        // Process each of the documents
        for (int i = 0; ((i < docsRelevantCount) && (i < hits.size())); i++) {
            Document doc = hits.elementAt(i);
            // Get text of the document and append it
            StringBuffer docTxtBuffer = new StringBuffer();
            String[] docTxtFlds = doc.getValues(Defs.FIELD);
            if (docTxtFlds.length == 0)
                continue;
            for (int j = 0; j < docTxtFlds.length; j++) {
                docTxtBuffer.append(docTxtFlds[j] + " ");
            }

            // Create termVector and add it to vector
            QueryTermVector docTerms = new QueryTermVector(docTxtBuffer.toString(), analyzer);
            docsTerms.add(docTerms);
        }

        return docsTerms;
    }

    /**
     * Sets boost of terms. boost = weight = factor(tf * idf)
     */
    public Vector<TermQuery> setBoost(QueryTermVector termVector, float factor) throws IOException {
        Vector<QueryTermVector> v = new Vector<QueryTermVector>();
        v.add(termVector);

        return setBoost(v, factor, 0);
    }

    public Vector<TermQuery> setBoost(Vector<QueryTermVector> docsTerms, float factor, float decayFactor)
            throws IOException {
        Vector<TermQuery> terms = new Vector<TermQuery>();

        // setBoost for each of the terms of each of the docs
        for (int g = 0; g < docsTerms.size(); g++) {
            QueryTermVector docTerms = docsTerms.elementAt(g);
            String[] termsTxt = docTerms.getTerms();
            int[] termFrequencies = docTerms.getTermFrequencies();

            // Increase decay
            float decay = decayFactor * g;

            // Populate terms: with TermQuries and set boost
            for (int i = 0; i < docTerms.size(); i++) {
                // Create Term
                String termTxt = termsTxt[i];
                Term term = new Term(Defs.FIELD, termTxt);

                // Calculate weight
                float tf = termFrequencies[i];
                float idf = similarity.idf((long) tf, docTerms.size());
                float weight = tf * idf;
                // Adjust weight by decay factor
                weight = weight - (weight * decay);

                // Create TermQuery and add it to the collection
                TermQuery termQuery = new TermQuery(term);
                // Calculate and set boost
                termQuery.setBoost(factor * weight);
                terms.add(termQuery);
            }
        }

        // Get rid of duplicates by merging termQueries with equal terms
        merge(terms);

        return terms;
    }

    public Vector<TermQuery> combineOrNot(Vector<TermQuery> queryTerms, Vector<TermQuery> docsTerms, boolean flag) {
        Vector<TermQuery> terms = new Vector<TermQuery>();
        //Add Terms from the docsTerms
        terms.addAll(docsTerms);
        if (flag) {
            //Add Terms from queryTerms: if term already exists just increment its boost
            for (int i = 0; i < queryTerms.size(); i++) {
                TermQuery qTerm = queryTerms.elementAt(i);
                TermQuery term = find(qTerm, terms);
                //Term already exists update its boost (temporary not)
                if (term != null) {
                    float weight = qTerm.getBoost() + term.getBoost();
                    term.setBoost(weight);
                }
                //Term does not exist; add it
                else {
                    terms.add(qTerm);
                }
            }
        }

        return terms;
    }

    /**
     * Finds term that is equal
     *
     * @return term; if not found -> null
     */
    public TermQuery find(TermQuery term, Vector<TermQuery> terms) {
        TermQuery termF = null;

        Iterator<TermQuery> iterator = terms.iterator();
        while (iterator.hasNext()) {
            TermQuery currentTerm = iterator.next();
            if (term.getTerm().equals(currentTerm.getTerm())) {
                termF = currentTerm;
            }
        }

        return termF;
    }

    private void merge(Vector<TermQuery> terms) {
        for (int i = 0; i < terms.size(); i++) {
            TermQuery term = terms.elementAt(i);
            // Iterate through terms and if term is equal then merge: add the boost; and delete the term
            for (int j = i + 1; j < terms.size(); j++) {
                TermQuery tmpTerm = terms.elementAt(j);

                // If equal then merge
                if (tmpTerm.getTerm().text().equals(term.getTerm().text())) {
                    // Add boost factors of terms
                    term.setBoost(term.getBoost() + tmpTerm.getBoost());
                    // delete unnecessary term
                    terms.remove(j);
                    // decrement j so that term is not skipped
                    j--;
                }
            }
        }
    }

    private void addExpandedTerms(String term, float boost) {
        term = term.toLowerCase().trim();
        if (!expansionList.containsKey(term)) {
            expansionList.put(term, boost);
        }
    }

    private LinkedHashMap<String, Float> filterExpandedTerms(LinkedHashMap<String, Float> expansionMap) {

        for (Map.Entry<String, Float> tmp : this.expansionList.entrySet()) {
            if (expansionMap.get(tmp.getKey()) == null)
                expansionMap.put(tmp.getKey(), tmp.getValue());
        }

        LinkedHashMap<String, Float> sortedMap = new LinkedHashMap<>();
        expansionMap.entrySet().stream()
                .sorted(new Comparator<Map.Entry<String, Float>>() {
                    public int compare(Map.Entry<String, Float> a, Map.Entry<String, Float> b) {
                        return b.getValue().compareTo(a.getValue());
                    }
                })
                .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        return sortedMap;
    }

    private void setExpandedTerms(String str) {
        Vector<TermQuery> terms = new Vector<TermQuery>();

        String[] splitArray = str.split("\\s+");

        // setBoost for each of the terms of each of the docs
        for (int i = 0; i < splitArray.length; i++) {
            String termTxt = splitArray[i];
            Term term = new Term(Defs.FIELD, termTxt);

            // Create TermQuery and add it to the collection
            TermQuery termQuery = new TermQuery(term);
            terms.add(termQuery);
        }

        // Get rid of duplicates by merging termQueries with equal terms
        merge(terms);

        setExpandedTerms(terms);
    }

    private void setExpandedTerms(Vector<TermQuery> expandedTerms) {
        this.expandedTerms = expandedTerms;
    }

    /**
     * Returns <code> QueryExpansion.TERM_NUM_FLD </code> expanded terms from the most recent query
     *
     * @return
     */
    public Vector<TermQuery> getExpandedTerms() {

        if (termNum > expandedTerms.size())
            termNum = expandedTerms.size();

        Vector<TermQuery> terms = new Vector<TermQuery>();

        // Return only necessary number of terms
        if (method.equals(QueryExpansion.SUN_METHOD)) {
            terms.addAll(expandedTerms.subList(0, expandedTerms.size()));
        } else {
            terms.addAll(expandedTerms.subList(0, termNum));
        }

        return terms;
    }

    private int getThreshold(boolean flag) {
        int threshold;
        if (flag)
            threshold = 100;
        else
            threshold = 30;

        return threshold;
    }

    public String MaptoBuffer(LinkedHashMap<String, Float> tmpHashMap) {
        StringBuffer termBuffer = new StringBuffer();
        for (Map.Entry<String, Float> entry : tmpHashMap.entrySet()) {
            String key = entry.getKey();
            float value = entry.getValue();

            if (stopWords.contains(key)) {
                System.out.println("Remove: " + key);
                continue;
            }

            termBuffer.append(QueryParser.escape(key) + "^" + value + " ");
        }
        return termBuffer.toString();
    }
}
