package controllers

import models.{DBName, message}
import models.message.{UpdatePassword, CheckPassword, AddUser}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future
import reactivemongo.api.Cursor
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.slf4j.{LoggerFactory, Logger}
import javax.inject.Singleton
import play.api.mvc._
import play.api.libs.json._

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

  def findUser(username: String): Future[Option[User]] = {
    val userSelector = Json.obj("username" -> username)
    collection.find(userSelector).one[User]
  }
}
