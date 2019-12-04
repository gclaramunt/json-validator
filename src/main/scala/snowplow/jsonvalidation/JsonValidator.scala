package snowplow.jsonvalidation

import com.github.fge.jsonschema.main.JsonSchemaFactory
import io.circe.Json
import io.circe.jackson._

import scala.collection.JavaConverters._

trait JsonValidator {
  def validate(schema: Json)(json: Json): Seq[String]
}

object JsonValidator extends  JsonValidator {

  val validator = JsonSchemaFactory.byDefault.getValidator

  override def validate(schema: Json)(json: Json): Seq[String] = {
    iterableAsScalaIterable(validator.validate(circeToJackson(schema), circeToJackson(json))).map(_.getMessage).toSeq
  }
}