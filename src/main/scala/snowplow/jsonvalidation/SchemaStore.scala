package snowplow.jsonvalidation

import cats.effect.Effect
import com.fasterxml.jackson.databind.ObjectMapper
import doobie._
import doobie.implicits._
import io.circe.Json
import io.circe.jackson._
import io.circe.jackson.syntax._

class SchemaStore [F[_]:Effect](xa: Transactor[F]) {



  val mapper = new ObjectMapper

  def find(id: String): F[Json] = {
    sql"SELECT schema_definition FROM JSON_SCHEMA WHERE schema_id = $id"
      .query[String].map(s => jacksonToCirce(mapper.readTree(s))).unique.transact(xa)
  }

  def store(id: String, schema: Json): F[Int] = {
    sql"insert into JSON_SCHEMA (schema_id, schema_definition) values ($id, ${schema.jacksonPrint})"
      .update.run.transact(xa)
  }

}

