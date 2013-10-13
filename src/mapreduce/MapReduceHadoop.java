package mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MapReduceHadoop {

    public static class TokenizerMapper
            extends Mapper<Object, Text, IntWritable, IntWritable> {
        static IntWritable one = new IntWritable(1);

        public int doMap(int a) {
            int b = 16;
            while (b != 0) {
                int t = b;
                b = a % t;
                a = t;
            }
            return a;
        }

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            int inputValue = Integer.valueOf(value.toString());
            int result = doMap(inputValue);
            context.write(one, new IntWritable(result));
        }
    }

    public static class IntSumReducer
            extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {

        static IntWritable one = new IntWritable(1);

        public int doReduce(int a, int b) {
            return Math.max(a, b);
        }

        public void reduce(IntWritable key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int result = 0;
            for (IntWritable value : values) {
                result = doReduce(result, value.get());
            }
            context.write(one, new IntWritable(result));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new GenericOptionsParser(args).getConfiguration();
        Cluster cluster = new Cluster(configuration);
        Job job = Job.getInstance(cluster);
        job.setJobName("mapreducetest");
        job.setJarByClass(MapReduceHadoop.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path("hdfs://master/tests/presentation20130611/input.dat"));
        FileOutputFormat.setOutputPath(job, new Path("hdfs://master/tests/presentation20130611/output-" + new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date()) + ".dat"));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
