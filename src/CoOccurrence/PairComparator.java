package CoOccurrence;

import java.util.Comparator;

public class PairComparator implements Comparator<Pair> {
    @Override
    public int compare(Pair e1, Pair e2) {
        return (e2.getCoOccurrenceCount() - e1.getCoOccurrenceCount());
    }
}
