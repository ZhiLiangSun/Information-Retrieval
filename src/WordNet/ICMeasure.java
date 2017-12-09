package WordNet;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * An abstract class that adds information content based methods to the
 * top level similarity measure class but doesn't itself define a
 * similarity measure.
 * @author Mark A. Greenwood
 */
public abstract class ICMeasure extends PathMeasure
{
    /**
     * This map stores the synset IDs and there associated frequencies
     * as read from the supplied information content file.
     */
    private Map<String, Double> freq = new HashMap<String, Double>();

    protected void config(Map<String, String> params) throws IOException {
        super.config(Boolean.parseBoolean(params.remove("root")));

        loadInfoContent(params.remove("infocontent"),
                params.get("encoding"));
    }

    /**
     * Loads the supplied infocontent file into this similarity measure.
     * @param infoContent The file to use.
     * @param encoding The encoding of the file - will default to UTF-8 if null.
     * @throws IOException IF the file couldn't be loaded or the file version doesn't math wordnet version
     */
    public void loadInfoContent(String infoContent, String encoding) throws IOException {
        //a handle to the infocontent file
        BufferedReader in = null;

        try
        {
            URL url = new URL(infoContent);

            if (encoding == null) encoding = "UTF-8";

            //open the info content file for reading
            in = new BufferedReader(new InputStreamReader(url.openStream(), encoding));

            //get the first line from the file (should be the WordNet version info)
            String line = in.readLine();

            //Check that what we have is actually a file of IC values
            if (line == null || !line.startsWith("wnver::")) throw new IOException("Malformed InfoContent file");

            //Check that the IC file is meant for use with the version
            //of WordNet we are currently using
            if (!line.endsWith("::" + dict.getVersion().getNumber())) throw new IOException("InfoContent file version doesn't match WordNet version");

            //Initially set the IC values of the noun and verb roots to 0
            freq.put("n", 0d);
            freq.put("v", 0d);

            //Get the first line of real data ready for use
            line = in.readLine();

            while (line != null && !line.equals(""))
            {
                //while there is still data in the file to process...

                //split the line on the whitespace
                String[] data = line.split("\\s+");

                //store the frequency (2nd column) against the synset ID (1st column)
                freq.put(data[0], new Double(data[1]));

                if (data.length == 3 && data[2].equals("ROOT"))
                {
                    //if there are three columns on this line and the
                    //last one is ROOT then...

                    //get the POS tag of the synset
                    String pos = data[0].substring(data[0].length() - 1);

                    //updated the node frequency for the POS tag
                    freq.put(pos, Double.parseDouble(data[1]) + freq.get(pos));
                }

                //read in the next line from the file ready for processing
                line = in.readLine();
            }
        }
        finally
        {
            //if we managed to open the file then close it
            if (in != null) in.close();
        }

    }


    /**
     * Generates the key to access the frequency count data loaded
     * from the information content file.
     * @param synset the synset for which to generate the key.
     * @return the key to access the frequency count map.
     */
    protected String getFreqKey(Synset synset)
    {
        //the keys used by the infomation content files are simply
        //the offsets in the wordnet database (minus leading zeros)
        //followed by the single character POS tag. So simply build
        //a key of this type...

        return synset.getOffset() + synset.getPOS().getKey();
    }

    /**
     * Gets the Information Content (IC) value associated with the given synset.
     * @param synset the synset for which to calcualte IC.
     * @return the IC of the given synset.
     */
    protected double getIC(Synset synset)
    {
        //get the POS tag of this synset
        POS pos = synset.getPOS();

        //Information Content is only defined for nouns and verbs
        //so return 0 if the POS tag is something else
        if (!pos.equals(POS.NOUN) && !pos.equals(POS.VERB)) return 0;

        //Get the frequency of this synset from the storred data
        Double synFreq = freq.get(getFreqKey(synset));

        //if the frequency isn't defined or it's 0 then simlpy return 0
        if (synFreq == null || synFreq.doubleValue() == 0) return 0;

        //Get the frequency of the root node for this POS tage
        Double rootFreq = freq.get(synset.getPOS().getKey());

        //calcualte the probability for this synset
        double prob = synFreq.doubleValue() / rootFreq.doubleValue();

        //if the probability is valid then use it to return the IC value
        if (prob > 0) return -Math.log(prob);

        //something went wrong so assume IC of 0
        return 0;
    }

    /**
     * Returns the frequency of the root node of the hierarchy for the
     * given POS tag.
     * @param pos the POS tag of the root node to access
     * @return the frequency of the root node for the given POS tag
     */
    protected double getFrequency(POS pos)
    {
        return freq.get(pos.getKey());
    }

    /**
     * Returns the frequency of the given synset.
     * @param synset the synset to retrieve the frequency of
     * @return the frequency of the supplied synset
     */
    protected double getFrequency(Synset synset)
    {
        Double f = freq.get(getFreqKey(synset));

        if (f == null || f.doubleValue() == 0) return 0;

        return f.doubleValue();
    }

    /**
     * Finds the lowerst common subsumer of the two synsets using information
     * content.
     * @param s1 the first synset
     * @param s2 the second synset
     * @return the lowest common subsumer of the two provided synsets
     * @throws JWNLException if an error occurs accessing WordNet
     */
    protected Synset getLCSbyIC(Synset s1, Synset s2) throws JWNLException
    {
        //TODO Handle the different types of LCS handled by the perl version which are
        //   1) Largest IC value
        //   2) Results in shortest path
        //   3) Greatest depth (i.e. the LCS whose shortest path to root is longest)
        //Although in here we only need the IC based one
        @SuppressWarnings("unchecked") List<PointerTargetNodeList> trees1 = PointerUtils.getHypernymTree(s1).toList();

        @SuppressWarnings("unchecked") List<PointerTargetNodeList> trees2 = PointerUtils.getHypernymTree(s2).toList();

        Set<Synset> pLCS = new HashSet<Synset>();

        for (List<PointerTargetNode> t1 : trees1)
        {
            for (List<PointerTargetNode> t2 : trees2)
            {
                for (PointerTargetNode node : t1)
                {
                    if (contains(t2, node.getSynset()))
                    {
                        pLCS.add(node.getSynset());
                        break;
                    }
                }

                for (PointerTargetNode node : t2)
                {
                    if (contains(t1, node.getSynset()))
                    {
                        pLCS.add(node.getSynset());
                        break;
                    }
                }
            }
        }

        Synset lcs = null;
        double score = 0;

        for (Synset s : pLCS)
        {
            if (lcs == null)
            {
                lcs = s;
                score = getIC(s);
            }
            else
            {
                double ic = getIC(s);

                if (ic > score)
                {
                    score = ic;
                    lcs = s;
                }
            }
        }

        if (lcs == null && useSingleRoot())
        {
            //link the two synsets by a fake root node

            //TODO: Should probably create one of these for each POS tag and cache them so that we can always return the same one
            //lcs = new Synset(s1.getPOS(), 0l, new Word[0], new Pointer[0], "", new java.util.BitSet());
            lcs = new Synset(dict, s1.getPOS(), 0);
        }

        return lcs;
    }
}