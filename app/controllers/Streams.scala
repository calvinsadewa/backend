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
 * The Users controllers encapsulates the Rest endpoints and the interaction with the MongoDB, via ReactiveMongo
 * play plugin. This provides a non-blocking driver for mongoDB as well as some useful additions for handling JSon.
 * @see https://github.com/ReactiveMongo/Play-ReactiveMongo
 */
@Singleton
class Streams extends Controller with MongoController {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Streams])

  /*
   * Get a JSONCollection (a Collection implementation that is designed to work
   * with JsObject, Reads and Writes.)
   * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
   * the collection reference to avoid potential problems in development with
   * Play hot-reloading.
   */
  def collection: JSONCollection = db.collection[JSONCollection](DBName.stream)
  def rawcollection: JSONCollection = db.collection[JSONCollection](DBName.rawStream)

  // ------------------------------------------ //
  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //

  import models.JsonFormats._
  import models._

  def getStream(id:String) = Action.async {
    collection.find(Json.obj("_id" -> id)).one[Stream].map( _.map( stream =>
        Ok(Json.toJson(stream))
      ) getOrElse BadRequest("Stream not found")
    )
  }

  def getRawStream(id:String) = Action.async {
    rawcollection.find(Json.obj("_id" -> id)).one[RawStream].map( _.map( raw =>
      Ok(Json.toJson(raw))
    ) getOrElse BadRequest("Raw Stream not found")
    )
  }
}
