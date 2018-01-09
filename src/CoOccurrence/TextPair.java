package CoOccurrence;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TextPair implements WritableComparable<TextPair> {
    private Text first;
    private Text second;

    public TextPair() {
        set(new Text(), new Text());
    }

    public TextPair(String left, String right) {
        set(new Text(left), new Text(right));
    }

    public TextPair(Text left, Text right) {
        set(left, right);
    }

    public void set(Text left, Text right) {
        String l = left.toString();
        String r = right.toString();
        int cmp = l.compareTo(r);
        if (cmp <= 0) {
            this.first = left;
            this.second = right;
        } else {
            this.first = right;
            this.second = left;
        }
    }

    public Text getFirst() {
        return first;
    }

    public Text getSecond() {
        return second;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        first.readFields(in);
        second.readFields(in);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        first.write(out);
        second.write(out);
    }

    @Override
    public int hashCode() {
        return first.hashCode() * 163 + second.hashCode();// May be some trouble here. why 163?
        // sometimes 157
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TextPair) {
            TextPair tp = (TextPair) o;
            return first.equals(tp.first) && second.equals(tp.second);
        }
        return false;
    }

    @Override
    public String toString() {
        return first + "\t" + second;
    }

    @Override
    public int compareTo(TextPair tp) {
        int cmp = first.compareTo(tp.first);
        if (cmp != 0)
            return cmp;
        return second.compareTo(tp.second);
    }

    // A Comparator that com.pares serialized StringPair.
    public static class Comparator extends WritableComparator {
        private static final Text.Comparator TEXT_COMPARATOR = new Text.Comparator();

        public Comparator() {
            super(TextPair.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            try {
                int firstl1 = WritableUtils.decodeVIntSize(b1[s1]) + readVInt(b1, s1);
                int firstl2 = WritableUtils.decodeVIntSize(b2[s2]) + readVInt(b2, s2);
                int cmp = TEXT_COMPARATOR.compare(b1, s1, firstl1, b2, s2, firstl2);
                if (cmp != 0)
                    return cmp;
                return TEXT_COMPARATOR.compare(b1, s1 + firstl1, l1 - firstl1, b2, s2 + firstl2, l1 - firstl2);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    static { // register this comparator
        WritableComparator.define(TextPair.class, new Comparator());
    }

    // Compare only the first part of the pair, so that reduce is called once for each value of
    // the first part.
    public static class FirstComparator extends WritableComparator {
        private static final Text.Comparator TEXT_COMPARATOR = new Text.Comparator();

        public FirstComparator() {
            super(TextPair.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            try {
                int firstl1 = WritableUtils.decodeVIntSize(b1[s1]) + readVInt(b1, s1);
                int firstl2 = WritableUtils.decodeVIntSize(b2[s2]) + readVInt(b2, s2);
                return TEXT_COMPARATOR.compare(b1, s1, firstl1, b2, s2, firstl2);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
