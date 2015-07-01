package controllers

import com.github.athieriot.EmbedConnection
import models.{VisitedStream, FinishedAnalysis, Stream}
import models.message._

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
class UsersIT extends Specification{
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

    "get random stream" in {
      running(FakeApplication()) {
        var gettedStreamId: Seq[String] = Seq()
        var i = 0
        for (i <- 1 to 50)
        {
          var json = Json.toJson(GetRandomStream("coba","benar"))
          val request = FakeRequest.apply(GET, "/user/get_random_stream").withJsonBody(json)
          val response = route(request)
          response.isDefined mustEqual true
          val result = Await.result(response.get, timeout)
          result.header.status must equalTo(OK)
          val option = contentAsJson(Future.successful(result)).asOpt[Stream]
          option.isDefined mustEqual true
          if (!gettedStreamId.contains(option.get._id)) {
            gettedStreamId = gettedStreamId :+ option.get._id
          }
        }
        gettedStreamId.contains("557a7c9d7e26b84302120c93") mustEqual true
        gettedStreamId.contains("557a7c0a7e26b84302120c92") mustEqual true
        //Case for when validate is over max validate
        gettedStreamId.contains("5587c7337e26b8170211c34d") mustEqual false
      }
    }

    "validate stream" in {
      running(FakeApplication()) {
        val seqAnalysis = Seq(FinishedAnalysis("Sentimen","Negatif"),FinishedAnalysis("Demography","10-19"))
        var json = Json.toJson(ValidateStream("coba","benar","557a7c9d7e26b84302120c93",seqAnalysis))
        var request = FakeRequest.apply(POST, "/user/validate_stream").withJsonBody(json)
          var response = route(request)
          response.isDefined mustEqual true
          var result = Await.result(response.get, timeout)
          result.header.status must equalTo(OK)
        //Check the stream properly validate
        val request2 = FakeRequest.apply(GET, "/stream/557a7c9d7e26b84302120c93")
        response = route(request2)
        response.isDefined mustEqual true
        result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
        var option = contentAsJson(Future.successful(result)).asOpt[Stream]
        option.isDefined mustEqual true
        option.get._id mustEqual "557a7c9d7e26b84302120c93"
        option.get.total_validate mustEqual 1
        seqAnalysis.forall( userAnalysis => {
          option.get.analysis.exists( analysis => analysis.name == userAnalysis.analysis_name
            && analysis.possible_values.exists( valueNumber => valueNumber.value_name == userAnalysis.value
            && valueNumber.number_validate == 1))
        }) mustEqual true
      }
      //TODO more comprehensive test
      //Cek kalau sudah pernah di validasi
      //cek kalau id stream ada
      //Cek kalau analysis name dan analysis value valid
      //Cek kalau semua analysis name telah di submit
      //Cek kalau tidak ada analysis yang double
    }

    "get visited streams" in {
      var json = Json.toJson(GetVisitedStreams("coba","benar"))
      running(FakeApplication()) {
        var request = FakeRequest.apply(GET, "/user/visited_streams").withJsonBody(json)
        var response = route(request)
        response.isDefined mustEqual true
        var result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
        val option = contentAsJson(Future.successful(result)).asOpt[Seq[VisitedStream]]
        option.isDefined mustEqual true
        option.get.length mustEqual 1
        option.get(0).id_stream mustEqual "557a7c9d7e26b84302120c93"
      }
      //TODO more comprehensive test
    }
  }
}