package controllers

import com.github.athieriot.EmbedConnection
import models.message.{UpdatePassword, CheckPassword, AddUser}

import scala.concurrent._
import duration._
import org.specs2.mutable._

import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit
import models.JsonFormats._


/**
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class UsersIT extends Specification with EmbedConnection{
  sequential
  val timeout: FiniteDuration = FiniteDuration(5, TimeUnit.SECONDS)

  "Users" should {
    "return Ok when check password" in {
      running(FakeApplication()) {
        val json = Json.toJson(CheckPassword("coba","benar"))
        val request = FakeRequest.apply(GET, "/user/check_password").withJsonBody(json)
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
      }
    }

    "fail when check password with false password" in {
      running(FakeApplication()) {
        var json = Json.toJson(CheckPassword("coba","test"))
        var request = FakeRequest.apply(GET, "/user/check_password").withJsonBody(json)
        var response = route(request)
        response.isDefined mustEqual true
        var result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "fail when check password with not vaild json" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/user/check_password").withJsonBody(Json.obj(
          "username" -> "coba",
          "password" -> 98))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "add user with a valid json" in {
      running(FakeApplication()) {
        val json = Json.toJson(AddUser("budi","hujan"))
        val request = FakeRequest.apply(POST, "/user/add_user").withJsonBody(json)
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(CREATED)
      }
    }

    "fail add user with a non valid json" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(POST, "/user/add_user").withJsonBody(Json.obj(
          "username" -> 98,
          "password" -> "London"))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status mustEqual BAD_REQUEST
      }
    }

    "fail add user if user already exist" in {
      running(FakeApplication()) {
        val json = Json.toJson(AddUser("budi","hujan"))
        val request = FakeRequest.apply(POST, "/user/add_user").withJsonBody(json)
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "update password" in {
      running(FakeApplication()) {
        var json = Json.toJson(CheckPassword("budi","hujan"))
        var request = FakeRequest.apply(GET, "/user/check_password").withJsonBody(json)
        var response = route(request)
        response.isDefined mustEqual true
        var result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)

        json = Json.toJson(UpdatePassword("budi","hujan","malam"))
        request = FakeRequest.apply(POST, "/user/update_password").withJsonBody(json)
        response = route(request)
        response.isDefined mustEqual true
        result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)

        json = Json.toJson(CheckPassword("budi","malam"))
        request = FakeRequest.apply(GET, "/user/check_password").withJsonBody(json)
        response = route(request)
        response.isDefined mustEqual true
        result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
      }
    }

    "fail when update password with not valid json" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(POST, "/user/update_password").withJsonBody(Json.obj(
          "username" -> "budi",
          "password" -> "malam"))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "fail when update password with whrong old password" in {
      var json = Json.toJson(UpdatePassword("budi","hujan","malam"))
      running(FakeApplication()) {
        val request = FakeRequest.apply(POST, "/user/update_password").withJsonBody(json)
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }
  }
}