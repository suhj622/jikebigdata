package com.suhj.main;

import com.suhj.map.FlowInfoMapper;
import com.suhj.reducer.FlowInfoReducer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 执行类
 */
public class FlowInfoMain {
    public static void main(String[] args) throws Exception {
        //1. 参数检查
        if(args.length != 2){
            System.out.println("Usage: FlowInfo <input path> <output path>");
            System.exit(-1);
        }
        //2. 创建作业
        Job job = new Job();
        job.setJarByClass(FlowInfoMain.class);
        job.setJobName("Flow Info");
        //3. 设置输入输出路径
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        //3. 设置Mapper Reducer
        job.setMapperClass(FlowInfoMapper.class);
        job.setReducerClass(FlowInfoReducer.class);
        //4. 设置输出的数据类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //5. 任务执行成败
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
