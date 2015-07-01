package controllers

import java.util.concurrent.TimeUnit

import models.{FinishedAnalysis, Stream}
import models.message._
import org.specs2.mutable.Specification
import play.{api, Logger}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}

/**
 * Created by calvin-pc on 6/10/2015.
 */
class Performance extends Specification{

  //Use test database (dump in folder test)
  import models.JsonFormats._
  val timeout: FiniteDuration = FiniteDuration(10000, TimeUnit.SECONDS)

  "Performance" should {
    "test speed" in {
      running(FakeApplication()) {
        {
          api.Logger.info("Starting performance test")
          val request = FakeRequest.apply(GET, "/stream/557a7c0a7e26b84302120c92")
          val response = route(request)
          val result = Await.result(response.get, timeout)
        }
        {api.Logger.info("AnalysisType start")}
        {
          val request = FakeRequest.apply(GET, "/analysis/all")
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] get all analysis type " + (t2 - t1) + "ms")
          true
        }
        {
          val request = FakeRequest.apply(GET, "/analysis/get/" + "557a56037e26b8ab003c3f97")
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] get analysis id " + (t2 - t1) + "ms")
          true
        }
        {api.Logger.info("AnalysisType end")}
        {api.Logger.info("Stream start")}
        {
          val request = FakeRequest.apply(GET, "/stream/557a7c9d7e26b84302120c93")
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] get stream id " + (t2 - t1) + "ms")
          true
        }
        {
          val request = FakeRequest.apply(GET, "/raw/557a7c9d7e26b84302120c93")
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] get raw stream id " + (t2 - t1) + "ms")
          true
        }
        {api.Logger.info("Stream end")}
        {api.Logger.info("ThridParties start")}
        {
          val request = FakeRequest.apply(GET, "/thridparty/check_password").withJsonBody(Json.obj(
            "username" -> "coba",
            "password" -> "benar"))
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] check thrid party password " + (t2 - t1) + "ms")
          true
        }
        {
          val request = FakeRequest.apply(POST, "/thridparty/add_user").withJsonBody(Json.obj(
            "username" -> "abcd",
            "password" -> "abcd"))
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] add thridparty " + (t2 - t1) + "ms")
        }
        {
          var message = models.message.UpdatePassword("abcd","abcd","efgh")
          var request = FakeRequest.apply(POST, "/thridparty/update_password")
            .withJsonBody(Json.toJson(message))
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] update thrid party password " + (t2 - t1) + "ms")
        }
        {
          val message = models.message.GetPreferableAnalysis("coba","benar")
          val request = FakeRequest.apply(GET, "/thridparty/get_preferable")
            .withJsonBody(Json.toJson(message))
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] get preferable analysis " + (t2 - t1) + "ms")
        }
        {
          val message = models.message.GetStreamIdList("abcd", "efgh")
          val request = FakeRequest.apply(GET, "/thridparty/get_all_stream_id")
            .withJsonBody(Json.toJson(message))
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] get all stream id " + (t2 - t1) + "ms")
        }
        {
          var message = models.message.AddRawStreamMessage("abcd", "efgh", 10, "raw", "ini coba - coba")
          var request = FakeRequest.apply(POST, "/thridparty/add_stream")
            .withJsonBody(Json.toJson(message))
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] add stream " + (t2 - t1) + "ms")
        }
        {
          val message = models.message.UpdatePreferable("abcd", "efgh", Seq("557a56037e26b8ab003c3f97"))
          val request = FakeRequest.apply(POST, "/thridparty/update_preferable")
            .withJsonBody(Json.toJson(message))
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] get update preferable " + (t2 - t1) + "ms")
        }
        {api.Logger.info("ThridParties end")}
        {api.Logger.info("User start")}
        {
          val json = Json.toJson(CheckPassword("coba","benar"))
          val request = FakeRequest.apply(GET, "/user/check_password").withJsonBody(json)
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] get check user password " + (t2 - t1) + "ms")
        }
        {
          val json = Json.toJson(AddUser("abcd","abcd"))
          val request = FakeRequest.apply(POST, "/user/add_user").withJsonBody(json)
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] add user " + (t2 - t1) + "ms")
        }
        {
          val json = Json.toJson(UpdatePassword("abcd","abcd","efgh"))
          val request = FakeRequest.apply(POST, "/user/update_password").withJsonBody(json)
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] update user password " + (t2 - t1) + "ms")
        }
        {
          var json = Json.toJson(GetRandomStream("coba","benar"))
          val request = FakeRequest.apply(GET, "/user/get_random_stream").withJsonBody(json)
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] get random stream " + (t2 - t1) + "ms")
        }
        {
          val seqAnalysis = Seq(FinishedAnalysis("Sentimen","Negatif"),FinishedAnalysis("Demography","10-19"))
          var json = Json.toJson(ValidateStream("abcd","efgh","557a7c9d7e26b84302120c93",seqAnalysis))
          var request = FakeRequest.apply(POST, "/user/validate_stream").withJsonBody(json)
          val t1 = System.currentTimeMillis()
          val response = route(request)
          val result = Await.result(response.get, timeout)
          val t2 = System.currentTimeMillis()
          api.Logger.info("[PF] validate stream " + (t2 - t1) + "ms")
        }
        {api.Logger.info("User end")}
        true
      }
    }
  }


}