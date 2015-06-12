package controllers

import java.util.Date

import models.DBName
import parser._
import javax.inject.Singleton

import models.message._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Iteratee
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

/**
 * The Users controllers encapsulates the Rest endpoints and the interaction with the MongoDB, via ReactiveMongo
 * play plugin. This provides a non-blocking driver for mongoDB as well as some useful additions for handling JSon.
 * @see https://github.com/ReactiveMongo/Play-ReactiveMongo
 */
@Singleton
class ThridParties extends Controller with MongoController {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[ThridParties])

  /*
   * Get a JSONCollection (a Collection implementation that is designed to work
   * with JsObject, Reads and Writes.)
   * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
   * the collection reference to avoid potential problems in development with
   * Play hot-reloading.
   */
  def collection: JSONCollection = db.collection[JSONCollection](DBName.thridParty)
  def analysiscollection: JSONCollection = db.collection[JSONCollection](DBName.analysisType)
  def streamcollection: JSONCollection = db.collection[JSONCollection](DBName.stream)
  def rawcollection: JSONCollection = db.collection[JSONCollection](DBName.rawStream)
  // ------------------------------------------ //
  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //

  import models.JsonFormats._
  import models._

  def createThridParty = Action.async(parse.json) {
    request =>
    /*
     * request.body is a JsValue.
     * There is an implicit Writes that turns this JsValue as a JsObject,
     * so you can call insert() with this JsValue.
     * (insert() takes a JsObject as parameter, or anything that can be
     * turned into a JsObject using a Writes.)
     */
      request.body.validate[AddThridParty].map {
        message =>
        // `user` is an instance of the case class `models.User`
          findThridParty(message.username).flatMap( option =>
            option.map ( thridParty => Future.successful(BadRequest("username exist"))).getOrElse[Future[Result]]( {
              val thridParty =
                ThridParty(
                  BSONObjectID.generate.stringify
                  ,message.username
                  ,message.password
                  ,Seq())
              collection.insert(thridParty).map {
                lastError =>
                  logger.debug(s"Successfully inserted with LastError: $lastError")
                  Created(s"Thrid Party Created")
              }}
            )
          )
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def checkPassword = Action.async(parse.json) {
    request =>
      /*
       * request.body is a JsValue.
       * There is an implicit Writes that turns this JsValue as a JsObject,
       * so you can call insert() with this JsValue.
       * (insert() takes a JsObject as parameter, or anything that can be
       * turned into a JsObject using a Writes.)
       */
      request.body.validate[CheckPassword].map {
        message =>
          // `user` is an instance of the case class `models.User`
          checkPasswords(message.username, message.password).flatMap{
            isTrue: Boolean =>
            if (isTrue) {
              Future.successful(Ok)
            }
            else {
              Future.successful(BadRequest("username does not exist"))
            }
          }
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def updatePassword = Action.async(parse.json) {
    request =>
      request.body.validate[UpdatePassword].map {
        message: UpdatePassword =>
          // find our user by first name and last name
          checkPasswords(message.username,message.oldPass).flatMap (
            isTrue =>
              if (isTrue) findThridParty(message.username).flatMap (
                thrid => {
                  val updateQuery = Json.obj("$set" -> Json.obj("password" -> message.newPass))
                  val usernameSelector = Json.obj("username" -> message.username)
                  collection.update(usernameSelector, updateQuery).flatMap {
                    lastError =>
                      logger.debug(s"Successfully updated with LastError: $lastError")
                      Future.successful(Ok(s"Thrid Party Updated"))
                  }}
              )
              else Future.successful(BadRequest("username/password wrong"))
          )
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def addRawStream = Action.async(parse.json) { //To Test
    request =>
      request.body.validate[AddRawStreamMessage].map { message =>
        findThridParty(message.username).flatMap( opt => opt.map( thridParty =>
          if (thridParty.password == message.password) Parser.getParser(message.filetype).map( prsr =>
            getAnalysisTypeDetail(thridParty.preferable_analysis).flatMap( listAnalysisType => {
              val id = BSONObjectID.generate.stringify
              prsr.parse(message.content, message.maxvalidasi, listAnalysisType, thridParty._id, id).map( stream =>
                rawcollection.insert(RawStream(id,message.content,message.filetype)).flatMap { lastError =>
                  logger.debug(s"Successfully inserted to raw stream with LastError: $lastError")
                  streamcollection.insert(stream).map { err =>
                    logger.debug(s"Successfully inserted to raw stream with LastError: $lastError")
                    Ok
                  }
                }
              ) getOrElse Future.successful(BadRequest("content doesn;t match file type"))
            })
          ) getOrElse Future.successful(BadRequest("filetype not supported"))
          else Future.successful(BadRequest("Password doesn't match"))
        ) getOrElse Future.successful(BadRequest("user doesn't exist")))
      } getOrElse Future.successful(BadRequest("invalid json"))
  }

  def getStreamIdList = Action.async(parse.json) { //To Test
    request =>
      request.body.validate[GetStreamIdList].map { message =>
        findThridParty(message.username).flatMap( opt => opt.map( thridParty =>
          if (thridParty.password == message.password){
              val future = streamcollection
                .find(Json.obj("id_provider" -> thridParty._id))
                .cursor[Stream]
                .collect[Seq]().map (
                _.map (_._id)
              )
              future.flatMap( seqId =>
                Future.successful(Ok(Json.toJson(ReturnGetStreamIdList(seqId))))
              )
          }
          else
            Future.successful(BadRequest("Password wrong"))
        ) getOrElse Future.successful(BadRequest("User doesn't exist")))
      } getOrElse Future.successful(BadRequest("invalid json format"))
  }

  def updatePreferable = Action.async(parse.json) { // To Test
    request =>
      request.body.validate[UpdatePreferable].map( message => {
        findThridParty(message.username).flatMap( opt => opt.map( thridParty =>
          if (thridParty.password == message.password){
            collection
              .update(
                Json.obj("_id" -> thridParty._id),
                Json.obj("$set" -> Json.obj("preferable_analysis" -> message.id_analysis_list)))
              .flatMap( error => {
              logger.debug(s"Successfully updated with LastError: $error")
              Future.successful(Ok)
            }
              )
          }
          else
            Future.successful(BadRequest("Password wrong"))
        ) getOrElse Future.successful(BadRequest("User doesn't exist")))
      }) getOrElse Future.successful(BadRequest("invalid format"))
  }

  def getPreferable = Action.async(parse.json) { // To Test
    request =>
      request.body.validate[GetPreferableAnalysis].map( message => {
        findThridParty(message.username).flatMap( opt => opt.map( thridParty =>
          if (thridParty.password == message.password){
            val returnMessage = ReturnGetPreferableAnalysis(thridParty.preferable_analysis)
            Future.successful(Ok(Json.toJson(returnMessage)))
          }
          else
            Future.successful(BadRequest("Password wrong"))
        ) getOrElse Future.successful(BadRequest("User doesn't exist")))
      }) getOrElse Future.successful(BadRequest("invalid format"))
  }

  def test = Action.async(parse.anyContent) {
    req =>
      val id = BSONObjectID.generate.stringify
      var test = RawStream("557a7c0a7e26b84302120c92","hari ini biru","raw")
      rawcollection.insert (test)
      test = RawStream("557a7c9d7e26b84302120c93","H4XXOR L4st 4evr","raw")
      rawcollection.insert (test)
      Future.successful(Ok)
  }

  def checkPasswords(username: String, password: String): Future[Boolean] = {
    val usernameSelector = Json.obj("username" -> username,"password" -> password)
    collection.find(usernameSelector).one[ThridParty].map( thridParty =>
      thridParty.isDefined
    )
  }

  def findThridParty(username: String): Future[Option[ThridParty]] = {
    val usernameSelector = Json.obj("username" -> username)
    collection.find(usernameSelector).one[ThridParty]
  }

  def getAnalysisTypeDetail (listIdType: Seq[String]): Future[Seq[AnalysisType]] = {
    val query = Json.obj( "_id" -> Json.obj( "$in" -> listIdType))
    analysiscollection.find(query).cursor[AnalysisType].collect[Seq]()
  }
}
