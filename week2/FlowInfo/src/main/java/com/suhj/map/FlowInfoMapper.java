package com.suhj.map;


import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * 重写Mapper
 */
public class FlowInfoMapper extends Mapper< LongWritable, Text, Text, Text > {
    @Override
    protected void map(LongWritable key, Text value, Mapper< LongWritable, Text, Text, Text >.Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String[] item = line.split("\t");
        String phone = item[1];
        String uploadFlow = item[8];
        String downloadFlow = item[9];
        context.write(new Text(phone), new Text(uploadFlow + "_" + downloadFlow));
    }
}
