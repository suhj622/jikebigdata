package com.suhj.reducer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * 重写Reducer
 */
public class FlowInfoReducer extends Reducer< Text, Text, Text, Text > {
    @Override
    protected void reduce(Text key, Iterable< Text > values, Reducer< Text, Text, Text, Text >.Context context) throws IOException, InterruptedException {
        long totalUploadFlow = 0L;
        long totalDownloadFlow = 0L;
        long totalFlow = 0L;
        for(Text value:values){
            String[] flows = value.toString().split("_");
            totalUploadFlow += Long.parseLong(flows[0]);
            totalDownloadFlow += Long.parseLong(flows[1]);
        }
        totalFlow = totalUploadFlow + totalDownloadFlow;
        context.write(key, new Text(totalUploadFlow + "\t" + totalDownloadFlow + "\t" + totalFlow));
    }
}
