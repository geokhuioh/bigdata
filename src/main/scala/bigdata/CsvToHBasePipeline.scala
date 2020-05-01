package bigdata

import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{IntegerType, LongType, StringType, StructType}

object CsvToHBasePipeline {

  def main(args: Array[String]) {

    val schema = new StructType()
      .add("user_id", StringType, nullable = true)
      .add("session_id", StringType, nullable = true)
      .add("timestamp", LongType, nullable = true)
      .add("step", IntegerType, nullable = true)
      .add("action_type", StringType, nullable = true)
      .add("reference", StringType, nullable = true)
      .add("platform", StringType, nullable = true)
      .add("city", StringType, nullable = true)
      .add("device", StringType, nullable = true)
      .add("current_filters", StringType, nullable = true)
      .add("impressions", StringType, nullable = true)
      .add("prices", StringType, nullable = true)

    val sparkConf = new SparkConf()
      .setMaster("local[1]")
      .setAppName("Big Data Engineering")

    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    val sc = spark.sparkContext

    val rdd = spark.read.format("csv")
      .option("header", value = true)
      .schema(schema)
      .load(Constant.CSV_FILENAME)
      .limit(100000)
      .rdd
      .map(Transformations.csvToClickEvent)

    val hbaseConf = HBaseConfiguration.create

    val hbaseContext : HBaseContext = new HBaseContext(sc, hbaseConf)

    hbaseContext.bulkPut(rdd, TableName.valueOf(Constant.HBASE_TABLE_NAME), Transformations.toHbasePut)

    spark.stop()
  }

}


