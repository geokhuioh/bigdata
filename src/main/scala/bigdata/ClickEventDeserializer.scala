package bigdata

import java.io.ByteArrayInputStream
import java.util

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericDatumReader}
import org.apache.avro.io.{DatumReader, Decoder, DecoderFactory}
import org.apache.kafka.common.serialization.Deserializer

class ClickEventDeserializer extends Deserializer[ClickEvent] {

  val schema: Schema = new Schema.Parser().parse(Constant.CLICK_EVENT_SCHEMA_STRING)

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {

  }

  override def deserialize(topic: String, data: Array[Byte]): ClickEvent = {
    val datumReader : DatumReader[GenericData.Record] = new GenericDatumReader[GenericData.Record](schema)

    val in = new ByteArrayInputStream(data)
    val decoder: Decoder = DecoderFactory.get().binaryDecoder(in, null)
    val record: GenericData.Record = new GenericData.Record(schema);

    datumReader.read(record, decoder)

    ClickEvent(
      String.valueOf(record.get("user_id")),
      String.valueOf(record.get("session_id")),
      record.get("timestamp").asInstanceOf[Long],
      record.get("step").asInstanceOf[Int],
      String.valueOf(record.get("action_type")),
      String.valueOf(record.get("reference")),
      String.valueOf(record.get("platform")),
      String.valueOf(record.get("city")),
      String.valueOf(record.get("device")),
      Option.empty[List[String]],
      Option.empty[Map[String, Int]]
    )

  }

  override def close(): Unit = {
  }
}
