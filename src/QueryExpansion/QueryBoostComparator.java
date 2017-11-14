package QueryExpansion;

import org.apache.lucene.search.Query;

import java.util.Comparator;

public class QueryBoostComparator implements Comparator<Object> {

    /**
     * Creates a new instance of QueryBoostComparator
     */
    public QueryBoostComparator() {
    }

    /**
     * Compares queries based on their boost Since want to be sorted in descending order;
     * comparison will be reversed
     */
    public int compare(Object obj1, Object obj2) {
        Query q1 = (Query) obj1;
        Query q2 = (Query) obj2;

        if (q1.getBoost() > q2.getBoost())
            return -1;
        else if (q1.getBoost() < q2.getBoost())
            return 1;
        else
            return 0;
    }
}