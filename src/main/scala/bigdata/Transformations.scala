package bigdata

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.Row

object Transformations {

  def csvToClickEvent(row: Row): ClickEvent = {
    ClickEvent(
      row.getString(0), // user_id
      row.getString(1), // session_id
      row.getLong(2),   // timestamp
      row.getInt(3),    // step
      row.getString(4), // action_type
      row.getString(5), // reference
      row.getString(6), // platform
      row.getString(7), // city
      row.getString(8), // device
      if (row.getString(9) != null) {
        Some(row.getString(9).split("|").toList)
      } else {
        None
      }, // current_filters
      if (row.getString(10) != null) {
        val keys = row.getString(10).split("\\|").toList
        val vals = row.getString(11).split("\\|").map(Integer.parseInt).toList
        Some((keys zip vals).toMap)
      } else {
        None
      } // impression price map
    )
  }

  def toHbasePut(evt: ClickEvent): Put = {
    val json = evt.toJson()
    val key1 = longToEightBytes(evt.timestamp)
    val key2 = integerToFourBytes(evt.step)
    val key3 = evt.session_id.getBytes()
    val rowKey = Bytes.add(key1, key2, key3)

    val p = new Put(rowKey)
    p.addColumn("f1".getBytes, "event".getBytes(), json.getBytes())
    p
  }
  def integerToFourBytes(value: Int): Array[Byte] = {
    var result: Array[Byte] = new Array[Byte](4);
    result(0) = ((value >>> 24) & 0xFF).toByte
    result(1) = ((value >>> 16) & 0xFF).toByte
    result(2) = ((value >>> 8) & 0xFF).toByte
    result(3) = (value & 0xFF).toByte
    result
  }

  def longToEightBytes(value: Long): Array[Byte] = {
    val result: Array[Byte] = new Array[Byte](8)
    result(0) = ((value >>> 54) & 0xFF).toByte
    result(1) = ((value >>> 48) & 0xFF).toByte
    result(2) = ((value >>> 40) & 0xFF).toByte
    result(3) = ((value >>> 32) & 0xFF).toByte

    result(4) = ((value >>> 24) & 0xFF).toByte
    result(5) = ((value >>> 16) & 0xFF).toByte
    result(6) = ((value >>> 8) & 0xFF).toByte
    result(7) = (value & 0xFF).toByte
    result
  }


}
