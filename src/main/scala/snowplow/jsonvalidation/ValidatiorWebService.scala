package snowplow.jsonvalidation

import cats.effect.{Async, Effect, IO}
import doobie.util.transactor.Transactor
import fs2.StreamApp
import io.circe.Json
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import cats.syntax.functor._, cats.syntax.flatMap._
import org.http4s.circe._
import io.circe.generic.auto._, io.circe.syntax._

import scala.concurrent.ExecutionContext


/*
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`

 */

/*
{
"action": "uploadSchema",
"id": "config-schema",
"status": "success"
}

{
"action": "uploadSchema",
"id": "config-schema",
"status": "error",
"message": "Invalid JSON"
}
 */
/*
        {
    "action": "validateDocument",
    "id": "config-schema",
    "status": "success"
}

{
    "action": "validateDocument",
    "id": "config-schema",
    "status": "error",
    "message": "Property '/root/timeout' is required"
}
         */



object Response {

  case class ResponseMessage( action: String, id: String, status: String, message: String )

  private def messageWithError(action: String, id :String, error: Seq[String]) =
    ResponseMessage( action, id, if (error.isEmpty) Success else Error, error.mkString )

  def ofValidateDocument(id: String, error: Seq[String]): ResponseMessage = messageWithError(ValidateDocument, id, error)
  def ofUploadSchema(id: String, error: Seq[String]): ResponseMessage = messageWithError(UploadSchema, id, error)

  private val Success = "success"
  private val Error = "error"

  private val ValidateDocument = "validateDocument"
  private val UploadSchema = "uploadSchema"


}

class ValidationWebService[F[_]: Effect](svSrvice: SchemaValidationService[F]) extends Http4sDsl[F] {

  val service: HttpService[F] = {
    HttpService[F] {
      case GET -> Root / "schema" / schemaId  =>
        Ok { svSrvice.getSchema(schemaId) }

      case req @ POST -> Root / "schema" / schemaId  =>
        val storeResult = for {
          json <- req.as[Json]
          errors <- svSrvice.storeSchema(schemaId, json)
        } yield  {
          Response.ofUploadSchema(schemaId, errors ).asJson
        }
        Ok { storeResult }


      case req @ POST -> Root / "validate" / schemaId  =>
        val validationResult= for {
          jsonToValidate <- req.as[Json]
          schema <- svSrvice.getSchema(schemaId)
        } yield {
          val errors = JsonValidator.validate(schema)(jsonToValidate)
          Response.ofValidateDocument(schemaId, errors).asJson
        }
        Ok { validationResult }
        
    }
  }

}

object UserTrackServer extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  org.h2.tools.Server.createTcpServer().start()

  def stream(args: List[String], requestShutdown: IO[Unit]) = ServerStream.stream[IO]
}

object ServerStream {

  def schemaStore[F[_]: Effect: Async] = {
    val xa = Transactor.fromDriverManager[F](
      "org.h2.Driver", // driver classname
      "jdbc:h2:mem:user-track;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'src/main/resources/createdb.sql'",
      "sa", // user
      "" // password
    )
    new SchemaStore[F](xa)
  }

  def validationWebService[F[_]: Effect] = new ValidationWebService[F](new SchemaValidationService(schemaStore)).service

  def stream[F[_]: Effect](implicit ec: ExecutionContext) = {
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(validationWebService, "/")
      .serve
  }
}
