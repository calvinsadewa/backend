package controllers

import javax.inject.Singleton

import models.{AnalysisType, DBName}
import models.message.ReturnGetAllTypes
import models.JsonFormats._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future

/**
 * The Users controllers encapsulates the Rest endpoints and the interaction with the MongoDB, via ReactiveMongo
 * play plugin. This provides a non-blocking driver for mongoDB as well as some useful additions for handling JSon.
 * @see https://github.com/ReactiveMongo/Play-ReactiveMongo
 */
@Singleton
class AnalysisTypes extends Controller with MongoController {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[AnalysisTypes])

  /*
   * Get a JSONCollection (a Collection implementation that is designed to work
   * with JsObject, Reads and Writes.)
   * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
   * the collection reference to avoid potential problems in development with
   * Play hot-reloading.
   */
  def collection: JSONCollection = db.collection[JSONCollection](DBName.analysisType)

  // ------------------------------------------ //
  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //

  def getAllTypes = Action.async {
    request =>
      collection.find(Json.obj()).cursor[AnalysisType].collect[Seq]().map( seq =>
        Ok(Json.toJson(ReturnGetAllTypes(seq)))
      )
  }

  def getSpesificTypes(id: String) = Action.async {
    request =>
      collection.find(Json.obj("_id" -> id)).one[AnalysisType].map(
        _.map( analysisType =>
          Ok(Json.toJson(analysisType))
        ).getOrElse(BadRequest("Not Found"))
      )
  }
}
