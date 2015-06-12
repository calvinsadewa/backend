package controllers

import models.{RawStream, Stream}
import java.util.concurrent.TimeUnit

import com.github.athieriot.EmbedConnection
import models.JsonFormats._
import models.message.{AddUser, CheckPassword, UpdatePassword}
import org.specs2.mutable._
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent._
import scala.concurrent.duration._


/**
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class StreamsIT extends Specification{

  //Use with test database (dump in test folder)
  val timeout: FiniteDuration = FiniteDuration(5, TimeUnit.SECONDS)

  "Streams" should {
    "retrive stream with specified id" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/stream/557a7c0a7e26b84302120c92")
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
        val option = contentAsJson(Future.successful(result)).asOpt[Stream]
        option.isDefined mustEqual true
        option.get._id mustEqual "557a7c0a7e26b84302120c92"
        option.get.content mustEqual "hari ini biru"
      }
    }

    "fail when retrieve stream if id not exist" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/stream/5")
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "retrive raw stream with specified id" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/raw/557a7c0a7e26b84302120c92")
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
        val option = contentAsJson(Future.successful(result)).asOpt[RawStream]
        option.isDefined mustEqual true
        option.get._id mustEqual "557a7c0a7e26b84302120c92"
        option.get.content mustEqual "hari ini biru"
      }
    }

    "fail when retrieve stream if id not exist" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/raw/5")
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }
  }
}