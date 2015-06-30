package controllers

import java.util.concurrent.TimeUnit

import models.Stream
import models.message.{ReturnGetPreferableAnalysis, ReturnGetStreamIdList}
import org.specs2.mutable.Specification
import play.{api, Logger}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}

/**
 * Created by calvin-pc on 6/10/2015.
 */
class ThridPartiesPT extends Specification{

  //Use test database (dump in folder test)
  import models.JsonFormats._
  val timeout: FiniteDuration = FiniteDuration(5, TimeUnit.SECONDS)

  "ThridPartiesPT" should {

    "get Stream id list" in {
      running(FakeApplication()) {
        val t1 = System.currentTimeMillis()
        val message = models.message.GetStreamIdList("coba", "benar")
        val request = FakeRequest.apply(GET, "/thridparty/get_all_stream_id")
          .withJsonBody(Json.toJson(message))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        val t2 = System.currentTimeMillis()
        api.Logger.info("[PF] get stream id " + (t2-t1) +"ms")
        true
      }
    }
  }
}