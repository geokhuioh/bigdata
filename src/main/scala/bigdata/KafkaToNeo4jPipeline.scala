package bigdata

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.Row
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.neo4j.driver._


object KafkaToNeo4jPipeline {

  def main(args: Array[String]): Unit = {

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
      .setMaster("local[4]")
      .setAppName("Kafka To Neo4J Pipeline")
      .set("spark.neo4j.bolt.password", "neo4j1")

    /* val spark = SparkSession.builder().config(sparkConf).getOrCreate() */

    val sc = new SparkContext(sparkConf);

    val streamingContext = new StreamingContext(sc, Seconds(5))

    val kafkaParams = Map[String, Object] (
      "bootstrap.servers" -> "localhost:9092",
      "group.id" -> "bigdata",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[ClickEventDeserializer],
      "enable.auto.commit" -> java.lang.Boolean.TRUE,
      "auto.offset.reset" -> "earliest"
    )

    val topic = Array(Constant.CLICK_EVENTS_TOPIC)


    def doSaveGraph(session: Session, evt: ClickEvent): Unit = {

      try {
        session.writeTransaction(new TransactionWork[Unit] {
          override def execute(tx: Transaction): Unit = {
            val q1 =
              """
                |MERGE (u:User {userId: $userId})
                |
                |MERGE (s:Session {sessionId: $sessionId,
                |                  platform: $platform,
                |                  device: $device})
                |
                |MERGE (u)-[:LOGIN]->(s)
                |
                |MERGE(
                |  (s)
                |  -[:INTERACT {step: $step}]->
                |  (a:Action {timestamp: $timestamp,
                |            type: $actionType}))
                """.stripMargin

            val q2 = evt.action_type match {
              case "clickout item" => {
                """
                  |MERGE (i:Item {itemId: $reference})
                  |
                  |MERGE (a)-[:CLIKCOUT_TARGET]->(i)
                  |
                  |""".stripMargin
              }
              case "interaction item rating" => {
                """
                  |MERGE (i:Item {itemId: $reference})
                  |
                  |MERGE (a)-[:RATING_OF]->(i)
                  |
                  |""".stripMargin
              }
              case "interaction item info" => {
                """
                  |MERGE (i:Item {itemId: $reference})
                  |
                  |MERGE (a)-[:INFO_ABOUT]->(i)
                  |
                  |""".stripMargin
              }
              case "interaction item image" => {
                """
                  |MERGE (i:Item{itemId: $reference})
                  |
                  |MERGE (a)-[:IMAGE_OF]->(i)
                  |
                  |""".stripMargin
              }
              case "interaction item deals" => {
                """
                  |MERGE (i:Item {itemId: $reference})
                  |
                  |MERGE (a)-[:DEAL_ON]->(i)
                  |
                  |""".stripMargin
              }
              case "change of sort order" => {
                """
                  |SET a.sortOrder = $reference
                  |""".stripMargin
              }
              case "filter selection" => {
                """
                  |SET a.filter = $reference
                  |""".stripMargin
              }
              case "search for item" => {
                """
                  |MERGE((i:Item {itemId: $reference}))
                  |MERGE((a)-[:SEARCH_HOTEL]->(i))
                  |""".stripMargin
              }
              case "search for destination" => {
                """
                  |MERGE (c:City {name: $reference})
                  |MERGE (a)-[:SEARCH_TOPIC]->(c)
                  |""".stripMargin
              }
              case "search for poi" => {
                """
                  |MERGE (p:PlaceOfInterest {name: $reference})
                  |MERGE (a)-[:REFERENCE]->(p)
                  |""".stripMargin
              }
            }

            val q3 = q1 + " " + q2
            val r: Result = tx.run(q3, Values.parameters("userId", evt.user_id, "sessionId", evt.session_id,
              "device", evt.device,
              "platform", evt.platform,
              "step", Integer.valueOf(evt.step),
              "actionType", evt.action_type,
              "timestamp", java.lang.Long.valueOf(evt.timestamp),
              "reference", evt.reference))

            tx.commit()
          }
        })
      } finally {
//        if (session != null) {
//          session.close();
//        }
      }
    }

    def save(iter:Iterator[ClickEvent]) : Unit = {
      val driver = GraphDatabase.driver("bolt://127.0.0.1:7687", AuthTokens.basic("neo4j", "neo4j1"));
      val session = driver.session()
      while(iter.hasNext) {
        doSaveGraph(session, iter.next())
      }
      session.close()
      driver.close()
    }

    val stream = KafkaUtils.createDirectStream[String, ClickEvent](
      streamingContext,
      PreferConsistent,
      Subscribe[String, ClickEvent](topic, kafkaParams)
    )

    stream.map(record => record.value)
    .foreachRDD(rdd => {
      rdd.foreachPartition(save)
    })

    streamingContext.start()
    streamingContext.awaitTermination()
  }

  def toClickEvent(row: Row): ClickEvent = {
    val r = row.getAs[Row](0)

    ClickEvent(
      r.getAs[String]("user_id"),
      r.getAs[String]("session_id"),
      r.getAs[Long]("timestamp"),
      r.getAs[Integer]("step"),
      r.getAs[String]("action_type"),
      r.getAs[String]("reference"),
      r.getAs[String]("platform"),
      r.getAs[String]("city"),
      r.getAs[String]("device"),
      //Option(r.getAs[WrappedArray[String]]("current_filters").toList),
      //Option(r.getAs[Map[String,Int]]("impressions")),
      Option.empty[List[String]],
      Option.empty[Map[String, Int]]
    )
  }

}
