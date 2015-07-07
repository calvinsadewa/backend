package controllers

import java.util.Date

import models.DBName
import models.message.log._
import parser._
import javax.inject.{Inject, Singleton}

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
 * Controller for processing request associated with ThridParty.
 */
@Singleton
class ThridParties @Inject() (parserMatcher: ParserMatcher) extends Controller with MongoController {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[ThridParties])

  /** thrid party collection. */
  def collection: JSONCollection = db.collection[JSONCollection](DBName.thridParty)
  /** analysis collection. */
  def analysiscollection: JSONCollection = db.collection[JSONCollection](DBName.analysisType)
  /** stream collection. */
  def streamcollection: JSONCollection = db.collection[JSONCollection](DBName.stream)
  /** raw stream collection. */
  def rawcollection: JSONCollection = db.collection[JSONCollection](DBName.rawStream)
  /** log collection. */
  def logcollection: JSONCollection = db.collection[JSONCollection](DBName.log)
  def log(any:JsValue) = {
    logcollection.insert(any)
  }
  // ------------------------------------------ //
  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //
  import models.JsonFormats._
  import models._

  /** Create new Thrid Party.
    * request body should be [[AddThridParty]] in json format.
    * Thrid party username must be unique.
    * return Created if ok, BadRequest if fail.
    */
  def createThridParty = Action.async(parse.json) {
    request =>
      request.body.validate[AddThridParty].map {
        message =>
        // `user` is an instance of the case class `models.User`
          val future = findThridParty(message.username).flatMap( option =>
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
          future onFailure {
            case t => log(
              Json.toJson(
                UserLog(
                  UserContent(message.username,message.password,LogName.thridparty,t.getMessage),
                  LogName.createuser)))
          }
          future
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  /** check password.
    * request body should be [[CheckPassword]] in json format.
    * return Ok if right, BadRequest if wrong/fail
    */
  def checkPassword = Action.async(parse.json) {
    request =>
      request.body.validate[CheckPassword].map {
        message =>
          // `user` is an instance of the case class `models.User`
          val future = checkPasswords(message.username, message.password).flatMap{
            isTrue: Boolean =>
            if (isTrue) {
              Future.successful(Ok)
            }
            else {
              Future.successful(BadRequest("username does not exist"))
            }
          }
          future onFailure {
            case t => log(
              Json.toJson(
                UserLog(
                  UserContent(message.username,message.password,LogName.thridparty,t.getMessage),
                  LogName.checkpassword)))
          }
          future
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  /** update password.
    * request body should be [[UpdatePassword]] in json format.
    * return Ok if right, BadRequest if wrong/fail
    */
  def updatePassword = Action.async(parse.json) {
    request =>
      request.body.validate[UpdatePassword].map {
        message: UpdatePassword =>
          // find our user by first name and last name
          val future = checkPasswords(message.username,message.oldPass).flatMap (
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
          future onFailure {
            case t => log(
              Json.toJson(
                UpdatePasswordLog(
                  UpdatePasswordContent(message.username,message.oldPass,
                    message.newPass,LogName.thridparty,t.getMessage))))
          }
          future
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  /** add raw stream and parse it into stream then add to database.
    * request body should be [[AddRawStreamMessage]] in json format.
    * return Ok if success, BadRequest if fail.
    */
  def addRawStream = Action.async(parse.json) {
    request =>
      request.body.validate[AddRawStreamMessage].map { message =>
        val future = findThridParty(message.username).flatMap( opt => opt.map( thridParty =>
          if (thridParty.password == message.password) parserMatcher.getParser(message.filetype).map( prsr =>
            getAnalysisTypeDetail(thridParty.preferable_analysis).flatMap( listAnalysisType => {
              val id = BSONObjectID.generate.stringify
              prsr.parse(message.content, message.maxvalidasi, listAnalysisType, thridParty._id, id).map { stream =>
                rawcollection.insert(RawStream(id, message.content, message.filetype))
                streamcollection.insert(stream)
                Future.successful(Ok)
              } getOrElse Future.successful(BadRequest("content doesn;t match file type"))
            })
          ) getOrElse Future.successful(BadRequest("filetype not supported"))
          else Future.successful(BadRequest("Password doesn't match"))
        ) getOrElse Future.successful(BadRequest("user doesn't exist"))
        )
        future onFailure {
          case t => log(
            Json.toJson(
              AddRawStreamLog(
                AddRawStreamContent(t.getMessage,message))))
        }
        future
      } getOrElse Future.successful(BadRequest("invalid json"))
  }

  /** Get stream id list of a thrid party.
    * request body should be [[GetStreamIdList]] in json format.
    * return Ok with [[ReturnGetStreamIdList]] if success, BadRequest if fail.
    */
  def getStreamIdList = Action.async(parse.json) {
    request =>
      request.body.validate[GetStreamIdList].map { message =>
        val future = findThridParty(message.username).flatMap( opt => opt.map( thridParty =>
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
        ) getOrElse Future.successful(BadRequest("User doesn't exist"))
        )
        future onFailure {
          case t => log(
            Json.toJson(
              UserLog(
                UserContent(message.username,message.password,LogName.thridparty,t.getMessage),LogName.getstreamidlist)))
        }
        future
      } getOrElse Future.successful(BadRequest("invalid json format"))
  }

  /** Update preferable analysis.
    * request body should be [[UpdatePreferable]] in json format.
    * return Ok if success, BadRequest if fail.
    */
  def updatePreferable = Action.async(parse.json) {
    request =>
      request.body.validate[UpdatePreferable].map( message => {
        val future = findThridParty(message.username).flatMap( opt => opt.map( thridParty =>
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
        ) getOrElse Future.successful(BadRequest("User doesn't exist"))
        )
        future onFailure {
          case t => log(
            Json.toJson(
              UpdatePreferableLog(
                UpdatePreferableContent(message,t.getMessage))))
        }
        future
      }) getOrElse Future.successful(BadRequest("invalid format"))
  }

  /** Get all preferable analysis.
    * request body should be [[GetPreferableAnalysis]] in json format.
    * return Ok with [[ReturnGetPreferableAnalysis]] if success, BadRequest if fail.
    */
  def getPreferable = Action.async(parse.json) {
    request =>
      request.body.validate[GetPreferableAnalysis].map( message => {
        val future = findThridParty(message.username).flatMap( opt => opt.map( thridParty =>
          if (thridParty.password == message.password){
            val returnMessage = ReturnGetPreferableAnalysis(thridParty.preferable_analysis)
            Future.successful(Ok(Json.toJson(returnMessage)))
          }
          else
            Future.successful(BadRequest("Password wrong"))
        ) getOrElse Future.successful(BadRequest("User doesn't exist")))
        future onFailure {
          case t => log(
            Json.toJson(
              UserLog(
                UserContent(message.username,message.password,LogName.thridparty,t.getMessage),
                LogName.getpreferable)))
        }
        future
      }) getOrElse Future.successful(BadRequest("invalid format"))
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
