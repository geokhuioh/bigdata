package bigdata

import java.io.StringWriter

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object ClickEvent {

  def fromJson(s: String): ClickEvent = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.readValue(s, classOf[ClickEvent])
  }

}

case class ClickEvent (user_id : String,
                       session_id : String,
                       timestamp : Long,
                       step : Int,
                       action_type : String,
                       reference : String,
                       platform : String,
                       city : String,
                       device : String,
                       currentFilters: Option[List[String]],
                       impressions : Option[Map[String, Int]]) {

  def toJson(): String = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    val out = new StringWriter
    mapper.writeValue(out, this)
    out.toString
  }


}
