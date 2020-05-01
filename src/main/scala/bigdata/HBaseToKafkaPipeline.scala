package bigdata

import java.io.ByteArrayOutputStream

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.avro.io.{BinaryEncoder, EncoderFactory}
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.SparkConf
import org.apache.spark.sql.types.{BinaryType, StringType, StructType}
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.JavaConverters._

object HBaseToKafkaPipeline {

  def main(args: Array[String]) {

    val schema =
      """
        |{
        |  "namespace": "ebac.bigdata",
        |  "type": "record",
        |  "name": "ClickEvent",
        |  "fields": [
        |    {"name": "user_id", "type": ["null", "string"], "default": null},
        |    {"name": "session_id", "type": ["null","string"], "default": null},
        |    {"name": "timestamp", "type": "long", "default": 0},
        |    {"name": "step", "type": "int", "default": 0},
        |    {"name": "action_type", "type": ["null", "string"], "default": null},
        |    {"name": "reference", "type": ["null", "string"], "default": null},
        |    {"name": "platform", "type": ["null", "string"], "default": null},
        |    {"name": "city", "type": ["null", "string"], "default": null},
        |    {"name": "device", "type": ["null", "string"], "default": null},
        |    {"name": "current_filters", "type": {"type": "array", "items": "string"}, "default": null},
        |    {"name": "impressions", "type": {"type": "map", "values": "int"}, "default": null}
        |  ]
        |}
        |""".stripMargin

    val sparkConf = new SparkConf()
      .setMaster("local[1]")
      .setAppName("Big Data Engineering")

    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    val sc = spark.sparkContext

    val hbaseConf = HBaseConfiguration.create

    val hbaseContext = new HBaseContext(sc, hbaseConf)

    val scan = new Scan()
    scan.addColumn("f1".getBytes(), "event".getBytes())
    scan.setCaching(1000)

    def hbaseRecordToAvroRecord(tuple: (ImmutableBytesWritable, Result)): Row = {
      val json = new String(tuple._2.getValue("f1".getBytes, "event".getBytes))
      val clickEvent = ClickEvent.fromJson(json)
      val sessionId = clickEvent.session_id

      val clickEventSchema = new Schema.Parser().parse(schema)
      val record : GenericRecord  = new GenericData.Record(clickEventSchema)
      record.put("user_id", clickEvent.user_id)
      record.put("session_id", clickEvent.session_id)
      record.put("step", clickEvent.step)
      record.put("timestamp", clickEvent.timestamp)
      record.put("action_type", clickEvent.action_type)
      record.put("reference", clickEvent.reference)
      record.put("platform", clickEvent.platform)
      record.put("city", clickEvent.city)
      record.put("device", clickEvent.device)

      record.put("current_filters", clickEvent.currentFilters
        .map { _.asJava }
        .getOrElse(java.util.Collections.emptyList()))

      record.put("impressions", clickEvent.impressions
        .map {_.asJava }
        .getOrElse(java.util.Collections.emptyMap()))

      val writer = new SpecificDatumWriter[GenericRecord](clickEventSchema)
      val out = new ByteArrayOutputStream()
      val encoder: BinaryEncoder = EncoderFactory.get().binaryEncoder(out, null)
      writer.write(record, encoder)
      encoder.flush()
      out.close()

      Row(sessionId, out.toByteArray)
    }

    val getRdd = hbaseContext.hbaseRDD(TableName.valueOf(Constant.HBASE_TABLE_NAME), scan, hbaseRecordToAvroRecord)

    val eventSchema = new StructType()
      .add("key", StringType,  nullable = true)
      .add("value", BinaryType, nullable = true)

    val df = spark.createDataFrame(getRdd, eventSchema)

    df.write.format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("topic", Constant.CLICK_EVENTS_TOPIC)
      .save()

    spark.stop()
  }
}
