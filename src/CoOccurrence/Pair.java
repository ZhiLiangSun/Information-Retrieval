package CoOccurrence;

public class Pair {

    private String term1;
    private String term2;
    private int coOccurrenceCount;
    private double ngdDistance;

    /**
     * Co-Occurrence count of term1 and term2
     *
     * @param term1
     * @param term2
     * @param coOccurrenceCount - co-occurrence count of term1 and term2
     */
    public Pair(String term1, String term2, int coOccurrenceCount) {
        this.term1 = term1;
        this.term2 = term2;
        this.coOccurrenceCount = coOccurrenceCount;
    }

    /**
     * NGD distance of term1 and term2
     *
     * @param term1
     * @param term2
     * @param ngdDistance - NGD distance of term1 and term2
     */
    public Pair(String term1, String term2, double ngdDistance) {
        this.term1 = term1;
        this.term2 = term2;
        this.ngdDistance = ngdDistance;
    }

    /**
     * return term1
     *
     * @return term1
     */
    public String getTerm1() {
        return term1;
    }

    /**
     * return term2
     *
     * @return term2
     */
    public String getTerm2() {
        return term2;
    }

    /**
     * return co-occurrence count of term1 and term2
     *
     * @return coOccurrenceCount
     */
    public int getCoOccurrenceCount() {
        return coOccurrenceCount;
    }

    /**
     * return NGD distance of term1 and term2
     *
     * @return ngdDistance
     */
    public double getNGDDistance() {
        return ngdDistance;
    }

}
