package bigdata

object Constant {

  val CLICK_EVENTS_TOPIC = "click-events"

  val CSV_FILENAME = "/home/ubuntu/Datasets/trivago-recsys-challenge/train.csv"

  val HBASE_TABLE_NAME = "bigdata"

  val CLICK_EVENT_SCHEMA_STRING =
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

}
