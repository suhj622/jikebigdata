package com.suhj

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf


/**
 * 方案一：适用于有大量小文件的场景
 * 用 wholeTextFiles 接口接收的数据，其返回是 <key, value> 二元组，
 * key 是文件名，value 是整个文件的内容
 */
object MainForWholeTextFiles {

  def main(args: Array[String]): Unit = {

    //执行参数可以通过配置文件获取
    val appName = "InvertedIndex-suhj"
    val conf = new SparkConf().setAppName(appName)
    val sc = new SparkContext(conf)

    //解析参数
    val source_root_dir = args(0)
    val target_root_dir = args(1)

    sc.wholeTextFiles(source_root_dir)   //读取目录下的文件
      .map(x => (x._2.replace("\n"," "), x._1.split("/").last.split("\\.")(0)))
      .flatMap(x => x._1.split(" ").map(y =>( (y , x._2), 1))).reduceByKey(_+_)
      .map(x => (x._1._1, (x._1._2, x._2) ))
      .map(x => (x._1, x._2.toString)).reduceByKey(_+", "+_)
      .map(x => "\"%s\":{%s}".format(x._1, x._2))
      .saveAsTextFile(target_root_dir)

  }

}
