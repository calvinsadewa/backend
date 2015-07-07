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
 * Controller for processing request Associated with analysis type.
 */
@Singleton
class AnalysisTypes extends Controller with MongoController {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[AnalysisTypes])

  /*
   * Analysis collection.
   */
  def collection: JSONCollection = db.collection[JSONCollection](DBName.analysisType)

  /**
   * return all analysis type ([[ReturnGetAllTypes]]).
   */
  def getAllTypes = Action.async {
    request =>
      collection.find(Json.obj()).cursor[AnalysisType].collect[Seq]().map( seq =>
        Ok(Json.toJson(ReturnGetAllTypes(seq)))
      )
  }

  /**
   * Return spesific analysis type.
   * get stream with spesific id.
   * return Ok with [[AnalysisType]] if sucess, BadRequest if fail
   */
  def getSpesificTypes(id: String) = Action.async {
    request =>
      collection.find(Json.obj("_id" -> id)).one[AnalysisType].map(
        _.map( analysisType =>
          Ok(Json.toJson(analysisType))
        ).getOrElse(BadRequest("Not Found"))
      )
  }
}
