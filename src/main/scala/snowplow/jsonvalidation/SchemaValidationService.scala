package snowplow.jsonvalidation

import cats.MonadError
import cats.effect.Effect
import io.circe.Json
import io.circe._
import io.circe.parser._
import cats.syntax.functor._
import cats.syntax.applicative._

import scala.io.Source

class SchemaValidationService[F[_]: Effect](schemaStore: SchemaStore[F]) {

  val metaSchema = parse(Source.fromResource("meta-schema.json").mkString).right.get //assume is valid

  def getSchema(id: String): F[Json] = schemaStore.find(id)

  def storeSchema(id: String, schema: Json): F[Seq[String]] = {
    val errorList = JsonValidator.validate(metaSchema)(schema)
    if (errorList.isEmpty)
      schemaStore.store(id, schema).map{ _ => Seq() }
    else
      errorList.pure
  }
}
