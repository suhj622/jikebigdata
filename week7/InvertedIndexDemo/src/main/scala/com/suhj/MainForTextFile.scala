package com.suhj

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

/**
 * @ClassName MainForTextFile
 * @Author Haojie
 * @create 2022/4/15 13:12
 * 返回的RDD是所有行的集合，一个文件可能分散在多个partition，适合处理有大文件的场景
 */
object MainForTextFile {

  def main(args: Array[String]): Unit = {


    val appName = "InvertedIndex-suhj"
    // Only one SparkContext should be running in this JVM (see SPARK-2243)
    val conf = new SparkConf().setAppName(appName)
    val sc = new SparkContext(conf)

    //解析参数 -- 输入路径，目标路径
    val inputDir = args(0)
    val outputDir = args(1)

    var target:RDD[(String, String)] = sc.emptyRDD

    //获取整个目录下的文件列表
    val file_list = getHDFSFiles(inputDir)
    for(file <- file_list) {

      //根据绝对路径+文件名，解析出文件名（不带后缀）
      val file_name = file.split("/").last.split("\\.")(0)

      val rdd = sc.textFile("%s/%s".format(inputDir, file))
        .flatMap(line => line.split(" "))
        .map(x => ((x, file_name), 1))
        .reduceByKey(_+_)
        .map(x => (x._1._1, (x._1._2, x._2)))
        .map(x => (x._1, x._2.toString))

      target = rdd.union(target)
    }

    target.reduceByKey(_+", "+_)
      .map(x =>  "\"%s\":{%s}".format(x._1, x._2))
      .saveAsTextFile(outputDir)

  }

  /**
   * 给定hdfs目录返回目录下文件名的集合
   * @param hdfsDirectory
   * @return Array[String]
   *  文件名 不带路径？
   */
  def getHDFSFiles(hdfsDirectory:String): Array[String] ={
    val configuration:Configuration = new Configuration()
    //    configuration.set("fs.defaultFS", hdfsFileName)
    val fileSystem:FileSystem = FileSystem.get(configuration)
    val fsPath: Path = new Path(hdfsDirectory)
    val iterator = fileSystem.listFiles(fsPath, true)
    val list = new ListBuffer[String]
    while (iterator.hasNext) {
      val pathStatus = iterator.next()
      val hdfsPath = pathStatus.getPath
      val fileName = hdfsPath.getName
      list += fileName // list.append(fileName)
    }
    fileSystem.close()
    list.toArray
  }


}

