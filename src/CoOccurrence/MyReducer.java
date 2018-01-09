package CoOccurrence;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class MyReducer extends Reducer<TextPair, IntWritable, TextPair, IntWritable> {
    private IntWritable result = new IntWritable();

    @Override
    public void reduce(TextPair inKey, Iterable<IntWritable> inValues, Context context)
            throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable val : inValues) {
            sum += val.get();
        }
        result.set(sum);
        context.write(inKey, result);
    }
}
