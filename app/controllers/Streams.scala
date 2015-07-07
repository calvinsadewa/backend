package controllers

import javax.inject.Singleton

import models.DBName
import models.message.{AddThridParty, CheckPassword, UpdatePassword}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

/**
 * Controller for processing request associated with streams.
 */
@Singleton
class Streams extends Controller with MongoController {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Streams])

  /** Stream collection. */
  def collection: JSONCollection = db.collection[JSONCollection](DBName.stream)

  /** Raw Stream collection. */
  def rawcollection: JSONCollection = db.collection[JSONCollection](DBName.rawStream)

  // ------------------------------------------ //
  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //

  import models.JsonFormats._
  import models._

  /**
   *  get stream with spesific id.
   *  return Ok with [[Stream]] if sucess, BadRequest if fail.
   */
  def getStream(id:String) = Action.async {
    collection.find(Json.obj("_id" -> id)).one[Stream].map( _.map( stream =>
        Ok(Json.toJson(stream))
      ) getOrElse BadRequest("Stream not found")
    )
  }

  /**
   *  get raw stream with spesific id.
   *  return Ok with [[RawStream]] if sucess, BadRequest if fail
   */
  def getRawStream(id:String) = Action.async {
    rawcollection.find(Json.obj("_id" -> id)).one[RawStream].map( _.map( raw =>
      Ok(Json.toJson(raw))
    ) getOrElse BadRequest("Raw Stream not found")
    )
  }
}
