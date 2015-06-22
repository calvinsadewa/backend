package controllers

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
 * The Users controllers encapsulates the Rest endpoints and the interaction with the MongoDB, via ReactiveMongo
 * play plugin. This provides a non-blocking driver for mongoDB as well as some useful additions for handling JSon.
 * @see https://github.com/ReactiveMongo/Play-ReactiveMongo
 */
@Singleton
class Users extends Controller with MongoController {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Users])

  /*
   * Get a JSONCollection (a Collection implementation that is designed to work
   * with JsObject, Reads and Writes.)
   * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
   * the collection reference to avoid potential problems in development with
   * Play hot-reloading.
   */
  def collection: JSONCollection = db.collection[JSONCollection](DBName.user)
  def streamcollection: JSONCollection = db.collection[JSONCollection](DBName.stream)

  // ------------------------------------------ //
  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //

  import models._
  import models.JsonFormats._

  def createUser = Action.async(parse.json) {
    request =>
    /*
     * request.body is a JsValue.
     * There is an implicit Writes that turns this JsValue as a JsObject,
     * so you can call insert() with this JsValue.
     * (insert() takes a JsObject as parameter, or anything that can be
     * turned into a JsObject using a Writes.)
     */
      request.body.validate[AddUser].map {
        message =>
        // `user` is an instance of the case class `models.User`
          val user = User(BSONObjectID.generate.stringify,message.username,message.password,Seq())
          findUser(message.username).flatMap(option =>
            if (option.isEmpty)
              collection.insert(user).map {
                lastError =>
                  logger.debug(s"Successfully inserted with LastError: $lastError")
                  Created(s"User Created")
              }
            else
              Future.successful(BadRequest("User exist"))
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
          findUser(message.username).map(
            option =>
              if (!option.isEmpty)
                if (option.get.password == message.password) Ok
                else BadRequest("Wrong Password")
              else BadRequest("User doesn't exist")
          )
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def updatePassword = Action.async(parse.json) {
    request =>
      /*
       * request.body is a JsValue.
       * There is an implicit Writes that turns this JsValue as a JsObject,
       * so you can call insert() with this JsValue.
       * (insert() takes a JsObject as parameter, or anything that can be
       * turned into a JsObject using a Writes.)
       */
      request.body.validate[UpdatePassword].map {
        message =>
          // `user` is an instance of the case class `models.User`
          findUser(message.username).flatMap(
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
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def getRandomStream = Action.async(parse.json) {
    request =>
      /*
       * wrapper for checking format, user, password
       */
      request.body.validate[GetRandomStream].map {
        message =>
          // `user` is an instance of the case class `models.User`
          findUser(message.username).flatMap(
            option =>
              if (!option.isEmpty)
                if (option.get.password == message.password) {
                  //Body
                  val query = Json.obj( "$where" -> "this.max_validasi > this.total_validate")
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
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def validateStream = Action.async(parse.json) {
    request =>
      /*
       * wrapper for checking format, user, password
       */
      request.body.validate[ValidateStream].map {
        message =>
          // `user` is an instance of the case class `models.User`
          findUser(message.username).flatMap(
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
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def VisitedStreams = Action.async(parse.json) {
    request =>
      /*
       * wrapper for checking format, user, password
       */
      request.body.validate[GetVisitedStreams].map {
        message =>
          // `user` is an instance of the case class `models.User`
          findUser(message.username).flatMap(
            option =>
              if (!option.isEmpty)
                if (option.get.password == message.password) {
                  //Body
                  Future.successful(Ok(Json.toJson(option.get.visited_streams)))
                }
                else Future.successful(BadRequest("Wrong Password"))
              else Future.successful(BadRequest("User doesn't exist"))
          )
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def test = Action.async(parse.anyContent) {
    req =>
      val test = Stream(BSONObjectID.generate.stringify,"557a71727e26b89e01c953dd",3,"test lebih validasi", "Sekarang", 6, Seq())
      collection.insert(Json.toJson(test))
      Future.successful(Ok(Json.toJson(test)))
  }

  def findUser(username: String): Future[Option[User]] = {
    val userSelector = Json.obj("username" -> username)
    collection.find(userSelector).one[User]
  }
}
