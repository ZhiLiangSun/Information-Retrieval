package WordNet;

import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNode;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * An abstract class that adds path based methods to the top level similarity
 * measure class but doesn't itself define a similarity measure.
 *
 * @author Mark A. Greenwood
 */
public abstract class PathMeasure extends SimilarityMeasure {
    /**
     * If true then a fake root node is used to joing the multiple
     * verb and noun hierarchies into one hierarchy per POS tag
     */
    private boolean root = true;

    /**
     * Should we use a single root node for each POS tag hierarchy
     *
     * @return true if we should use a single root node for each POS tag, false
     * otherwise
     */
    protected boolean useSingleRoot() {
        return root;
    }

    protected void config(Map<String, String> params) throws IOException {    //A protected constructor to force the use of the newInstance method
        if (params.containsKey("root")) this.root = Boolean.parseBoolean(params.remove("root"));
    }

    protected void config(boolean root) {
        this.root = root;
    }

    /**
     * Utility method to determine if the list of nodes contains a given synset
     *
     * @param l a list of nodes
     * @param s a synset
     * @return true if the synset is contained within the list of nodes, false
     * otherwise
     */
    protected static boolean contains(List<PointerTargetNode> l, Synset s) {
        for (PointerTargetNode node : l) {
            if (node.getSynset().equals(s)) return true;
        }

        return false;
    }
}