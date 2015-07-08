package controllers

import models.message.log._
import models.{DBName, message}
import models.message._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.core.commands.{Skip, Count}
import scala.collection.mutable
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.Future
import reactivemongo.api.{QueryOpts, Cursor}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.slf4j.{LoggerFactory, Logger}
import javax.inject.Singleton
import play.api.mvc._
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats

/**
 * Controller to handle request associated to user.
 */
@Singleton
class Users extends Controller with MongoController {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Users])

  /** user collection. */
  def collection: JSONCollection = db.collection[JSONCollection](DBName.user)
  /** stream collection. */
  def streamcollection: JSONCollection = db.collection[JSONCollection](DBName.stream)
  /** log collection. */
  def logcollection: JSONCollection = db.collection[JSONCollection](DBName.log)
  def log(any:JsValue) = {
    logcollection.insert(any)
  }

  // ------------------------------------------ //
  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //

  import models._
  import models.JsonFormats._

  /** Create new User.
    * request body should be [[AddUser]] in json format.
    * User username must be unique.
    * return Created if ok, BadRequest if fail.
    */
  def createUser = Action.async(parse.json) {
    request =>
      request.body.validate[AddUser].map {
        message =>
        // `user` is an instance of the case class `models.User`
          val user = User(BSONObjectID.generate.stringify,message.username,message.password,Seq())
          val future = findUser(message.username).flatMap(option =>
            if (option.isEmpty)
              collection.insert(user).map {
                lastError =>
                  logger.debug(s"Successfully inserted with LastError: $lastError")
                  Created(s"User Created")
              }
            else
              Future.successful(BadRequest("User exist"))
          )
          future onFailure {
            case t => log(
              Json.toJson(
                UserLog(
                  UserContent(message.username,message.password,LogName.user,t.getMessage),LogName.createuser)))
          }
          future
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  /** Check password.
    * request body should be [[CheckPassword]] in json format.
    * return Ok if success, BadRequest if fail.
    */
  def checkPassword = Action.async(parse.json) {
    request =>
      request.body.validate[CheckPassword].map {
        message =>
          // `user` is an instance of the case class `models.User`
          val future = findUser(message.username).map(
            option =>
              if (!option.isEmpty)
                if (option.get.password == message.password) Ok
                else BadRequest("Wrong Password")
              else BadRequest("User doesn't exist")
          )
          future onFailure {
            case t => log(
              Json.toJson(
                UserLog(
                  UserContent(message.username,message.password,LogName.user,t.getMessage),
                  LogName.checkpassword)))
          }
          future
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }


  /** Update password.
    * request body should be [[UpdatePassword]] in json format.
    * return Ok if success, BadRequest if fail.
    */
  def updatePassword = Action.async(parse.json) {
    request =>
      request.body.validate[UpdatePassword].map {
        message =>
          // `user` is an instance of the case class `models.User`
          val future = findUser(message.username).flatMap(
            option =>
              if (!option.isEmpty)
                if (option.get.password == message.oldPass) {
                  val updateQuery = Json.obj("$set" -> Json.obj("password" -> message.newPass))
                  val usernameSelector = Json.obj("username" -> message.username)
                  collection.update(usernameSelector, updateQuery).map {
                    lastError =>
                      logger.debug(s"Successfully updated with LastError: $lastError")
                      Ok(s"Thrid Party Updated")
                  }
                }
                else Future.successful(BadRequest("Wrong Password"))
              else Future.successful(BadRequest("User doesn't exist"))
          )
          future onFailure {
            case t => log(
              Json.toJson(
                UpdatePasswordLog(
                  UpdatePasswordContent(message.username,message.oldPass,message.newPass,LogName.user,t.getMessage))))
          }
          future
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  /** Get random stream.
    * request body should be [[GetRandomStream]] in json format.
    * return Ok with [[Stream]] if success, BadRequest if fail.
    */
  def getRandomStream = Action.async(parse.json) {
    request =>
      /*
       * wrapper for checking format, user, password
       */
      request.body.validate[GetRandomStream].map {
        message =>
          // `user` is an instance of the case class `models.User`
          val future = findUser(message.username).flatMap(
            option =>
              if (!option.isEmpty)
                if (option.get.password == message.password) {
                  //Body
                  val query = Json.obj(
                    "$where" -> "this.max_validasi > this.total_validate" ,
                    "_id" -> Json.obj( 
                        "$nin" -> (for {stream <- option.get.visited_streams} yield stream.id_stream)
                        ) 
                    )
                  db.command(
                    Count(
                      streamcollection.name,
                      Some(BSONFormats.toBSON(query).get.asInstanceOf[BSONDocument])
                    )).flatMap( count => {
                    val random = Math.floor(Math.random()*count).toInt
                    streamcollection.find(query).options(QueryOpts(random)).one[Stream].flatMap(
                      _.map( stream => Future.successful(Ok(Json.toJson(stream)))
                      ).getOrElse(Future.successful(BadRequest("No Stream Found")))
                    )
                  })
                }
                else Future.successful(BadRequest("Wrong Password"))
              else Future.successful(BadRequest("User doesn't exist"))
          )
          future onFailure {
            case t => log(
              Json.toJson(
                UserLog(
                  UserContent(message.username,message.password,LogName.user,t.getMessage),
                  LogName.getrandomstream)))
          }
          future
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  /** validate a stream.
    * request body should be [[ValidateStream]] in json format.
    * return Ok if success, BadRequest if fail.
    */
  def validateStream = Action.async(parse.json) {
    request =>
      /*
       * wrapper for checking format, user, password
       */
      request.body.validate[ValidateStream].map {
        message =>
          // `user` is an instance of the case class `models.User`
          val future = findUser(message.username).flatMap(
            option =>
              if (!option.isEmpty)
                if (option.get.password == message.password)
                  //Body
                  //Cek kalau stream pernah di validasi
                  if (option.get.visited_streams.forall(_.id_stream != message.id_stream)) {
                    //Cek id_stream ada
                    streamcollection.find(Json.obj("_id" -> message.id_stream)).one[Stream].flatMap(_.map{ stream =>
                      //Cek kalau semua analysis name telah di submit
                      val valid = stream.analysis.forall(
                        //Cek kalau analysis name dan analysis value valid
                        analysis => message.analysis.filter(
                          mesAnalysis =>
                            analysis.name == mesAnalysis.analysis_name
                              && analysis.possible_values.map(_.value_name).contains(mesAnalysis.value)
                        ).length == 1
                        //Cek kalau tidak ada analysis yang double
                      ) && stream.analysis.length == message.analysis.length

                      if (valid) {
                        val selectStreamQuery = Json.obj("_id" -> stream._id)
                        val selectUserQuery = Json.obj("_id" -> option.get._id)
                        val newAnalysis = stream.analysis.map( analysisType => {
                          val value = message.analysis.filter(p => p.analysis_name == analysisType.name)(0).value
                          val seqValueNumber = analysisType.possible_values.map( valueNumber =>
                            if (valueNumber.value_name == value) AnalysisValueNumber(value,valueNumber.number_validate + 1)
                            else valueNumber
                          )
                          AnalysisType(analysisType._id,analysisType.name,seqValueNumber)
                        })

                        val newStream = Stream(
                          stream._id,
                          stream.id_provider,
                          stream.max_validasi,
                          stream.content,
                          stream.date,
                          stream.total_validate+1,
                          newAnalysis)

                        val updateUserQuery = Json.obj(
                          "$push" -> Json.obj("visited_streams" -> Json.toJson(VisitedStream(message.id_stream,message.analysis))))
                        collection.update(selectUserQuery,updateUserQuery)
                        streamcollection.update(selectStreamQuery,Json.toJson(newStream)).map {
                          lastError =>
                            logger.debug(s"Successfully inserted with LastError: $lastError")
                            Ok
                        }
                      }
                      else Future.successful(BadRequest("Analysis name or value is wrong"))
                    } getOrElse Future.successful(BadRequest("Stream Not Found")))
                  }
                  else Future.successful(BadRequest("Stream Already Validated"))

                else Future.successful(BadRequest("Wrong Password"))
              else Future.successful(BadRequest("User doesn't exist"))
          )
          future onFailure {
            case t => log(
              Json.toJson(
                ValidateStreamLog(
                  ValidateStreamContent(message,t.getMessage))))
          }
          future
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  /** Get all visited stream.
    * request body should be [[GetVisitedStreams]] in json format.
    * return Ok if success, BadRequest if fail.
    */
  def VisitedStreams = Action.async(parse.json) {
    request =>
      /*
       * wrapper for checking format, user, password
       */
      request.body.validate[GetVisitedStreams].map {
        message =>
          // `user` is an instance of the case class `models.User`
          val future = findUser(message.username).flatMap(
            option =>
              if (!option.isEmpty)
                if (option.get.password == message.password) {
                  //Body
                  Future.successful(Ok(Json.toJson(option.get.visited_streams)))
                }
                else Future.successful(BadRequest("Wrong Password"))
              else Future.successful(BadRequest("User doesn't exist"))
          )
          future onFailure {
            case t => log(
              Json.toJson(
                UserLog(
                  UserContent(message.username,message.password,LogName.user,t.getMessage),
                  LogName.getvisitedstream)))
          }
          future
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def findUser(username: String): Future[Option[User]] = {
    val userSelector = Json.obj("username" -> username)
    collection.find(userSelector).one[User]
  }
}