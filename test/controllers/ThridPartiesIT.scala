package controllers

import java.util.concurrent.TimeUnit

import com.github.athieriot._
import models.Stream
import models.message.{ReturnGetStreamIdList, ReturnGetPreferableAnalysis}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.{FakeRequest, FakeApplication}
import play.api.test.Helpers._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.FiniteDuration
/**
 * Created by calvin-pc on 6/10/2015.
 */
class ThridPartiesIT extends Specification{

  //Use test database (dump in folder test)
  import models.JsonFormats._
  val timeout: FiniteDuration = FiniteDuration(5, TimeUnit.SECONDS)

  "ThridParties" should {

    "Return OK status when correct check password" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/thridparty/check_password").withJsonBody(Json.obj(
          "username" -> "coba",
          "password" -> "benar"))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
      }
    }

    "Return Bad Request status when check password if not valid json" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/thridparty/check_password").withJsonBody(Json.obj(
          "username" -> "coba",
          "password" -> 9))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "Return Bad Request status when check password if wrong" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/thridparty/check_password").withJsonBody(Json.obj(
          "username" -> "coba",
          "password" -> "coba"))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "add person with a valid json" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(POST, "/thridparty/add_user").withJsonBody(Json.obj(
          "username" -> "munca",
          "password" -> "whoknow"))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(CREATED)
        val request2 = FakeRequest.apply(GET, "/thridparty/check_password").withJsonBody(Json.obj(
          "username" -> "munca",
          "password" -> "whoknow"))
        val response2 = route(request2)
        response2.isDefined mustEqual true
        val result2 = Await.result(response2.get, timeout)
        result2.header.status must equalTo(OK)
      }
    }

    "fail when add person without valid json" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(POST, "/thridparty/add_user").withJsonBody(Json.obj(
          "user" -> "munca",
          "pass" -> "whoknow"))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "fail when add person with same username" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(POST, "/thridparty/add_user").withJsonBody(Json.obj(
          "username" -> "munca",
          "password" -> "mozart"))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
        val response2 = route(request)
        response2.isDefined mustEqual true
        val result2 = Await.result(response2.get, timeout)
        result2.header.status must equalTo(BAD_REQUEST)
      }
    }

    "Return OK status when update password correct" in {
      running(FakeApplication()) {
        var message = models.message.UpdatePassword("munca","whoknow","helloworld")
        var request = FakeRequest.apply(POST, "/thridparty/update_password")
          .withJsonBody(Json.toJson(message))
        var response = route(request)
        response.isDefined mustEqual true
        var result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)

        request = FakeRequest.apply(GET, "/thridparty/check_password").withJsonBody(Json.obj(
          "username" -> "munca",
          "password" -> "helloworld"))
        response = route(request)
        response.isDefined mustEqual true
        result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
      }
    }

    "Return Bad Request status when update password old password wrong" in {
      running(FakeApplication()) {
        val message = models.message.UpdatePassword("munca","wawawa","hello")
        val request = FakeRequest.apply(POST, "/thridparty/update_password")
          .withJsonBody(Json.toJson(message))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "Return BAD status when update password not valid json format" in {
      running(FakeApplication()) {
        val message = models.message.CheckPassword("mega","mozart")
        val request = FakeRequest.apply(POST, "/thridparty/update_password")
          .withJsonBody(Json.toJson(message))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "get preferable analysis" in {
      running(FakeApplication()) {
        val message = models.message.GetPreferableAnalysis("coba","benar")
        val request = FakeRequest.apply(GET, "/thridparty/get_preferable")
          .withJsonBody(Json.toJson(message))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
        val option = contentAsJson(response.get).asOpt[ReturnGetPreferableAnalysis]
        option.isDefined mustEqual true
        val returnMessage = option.get
        returnMessage.id_analysis_list.length mustEqual 2
        returnMessage.id_analysis_list.forall( string => { println("\n\n\n\n\n[Hello]" + string)
          string == "557a56037e26b8ab003c3f97" || string == "557a56b27e26b8b6003c3f98"}) mustEqual true
      }
    }

    "return bad request when get preferable analysis because invalid json" in {
      running(FakeApplication()) {
        val message = models.message.ReturnGetPreferableAnalysis(Seq())
        val request = FakeRequest.apply(GET, "/thridparty/get_preferable")
          .withJsonBody(Json.toJson(message))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "return bad request when get preferable analysis because incorrect password" in {
      running(FakeApplication()) {
        val message = models.message.GetPreferableAnalysis("coba","coba")
        val request = FakeRequest.apply(GET, "/thridparty/get_preferable")
          .withJsonBody(Json.toJson(message))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "return bad request when get preferable analysis because user doen;t exist" in {
      running(FakeApplication()) {
        val message = models.message.GetPreferableAnalysis("xxxxxxxx","coba")
        val request = FakeRequest.apply(GET, "/thridparty/get_preferable")
          .withJsonBody(Json.toJson(message))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }

    "update preferable" in {
      running(FakeApplication()) {
        val message = models.message.UpdatePreferable("munca", "helloworld" , Seq("557a56037e26b8ab003c3f97"))
        val request = FakeRequest.apply(POST, "/thridparty/update_preferable")
          .withJsonBody(Json.toJson(message))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)

        val message2 = models.message.GetPreferableAnalysis("munca", "helloworld")
        val request2 = FakeRequest.apply(GET, "/thridparty/get_preferable")
          .withJsonBody(Json.toJson(message2))
        val response2 = route(request2)
        response2.isDefined mustEqual true
        val result2 = Await.result(response2.get, timeout)
        result2.header.status must equalTo(OK)
        val option = contentAsJson(response2.get).asOpt[ReturnGetPreferableAnalysis]
        option.isDefined mustEqual true
        val returnMessage = option.get
        returnMessage.id_analysis_list.length mustEqual 1
        returnMessage.id_analysis_list.head mustEqual "557a56037e26b8ab003c3f97"
      }
    }

    "get Stream id list" in {
      running(FakeApplication()) {
        val message = models.message.GetStreamIdList("coba", "benar")
        val request = FakeRequest.apply(GET, "/thridparty/get_all_stream_id")
          .withJsonBody(Json.toJson(message))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
        val option = contentAsJson(response.get).asOpt[ReturnGetStreamIdList]
        option.isDefined mustEqual true
        val returnMessage = option.get
        returnMessage.id_list.length mustEqual 2
        returnMessage.id_list.forall( string => {
          string == "557a7c0a7e26b84302120c92" || string == "557a7c9d7e26b84302120c93"}) mustEqual true
      }
    }

    "add raw Stream and parse it" in {
      running(FakeApplication()) {
        var id:String  = {
          "Hallo"
        }
        { //Add
          var message = models.message.AddRawStreamMessage("munca", "helloworld", 10, "raw", "ini coba - coba")
          var request = FakeRequest.apply(POST, "/thridparty/add_stream")
            .withJsonBody(Json.toJson(message))
          var response = route(request)
          response.isDefined mustEqual true
          var result = Await.result(response.get, timeout)
          result.header.status must equalTo(OK)
        }
        { //check with get all id stream
          val message = models.message.GetStreamIdList("munca", "helloworld")
          val request = FakeRequest.apply(GET, "/thridparty/get_all_stream_id")
            .withJsonBody(Json.toJson(message))
          val response = route(request)
          response.isDefined mustEqual true
          val result = Await.result(response.get, timeout)
          result.header.status must equalTo(OK)
          val option = contentAsJson(response.get).asOpt[ReturnGetStreamIdList]
          option.isDefined mustEqual true
          val returnMessage = option.get
          returnMessage.id_list.length mustEqual 1
          id = returnMessage.id_list.head
        }
        {//check the detail with getstream
          val request = FakeRequest.apply(GET, "/stream/" + id)
          val response = route(request)
          response.isDefined mustEqual true
          val result = Await.result(response.get, timeout)
          result.header.status must equalTo(OK)
          val option = contentAsJson(Future.successful(result)).asOpt[Stream]
          option.isDefined mustEqual true
          option.get._id mustEqual id
          option.get.content mustEqual "ini coba - coba"
          option.get.analysis.length mustEqual 1
          option.get.analysis.head._id mustEqual "557a56037e26b8ab003c3f97"
          option.get.max_validasi mustEqual 10
        }
      }
    }

    "fail to add raw Stream because file type not supported" in {
      running(FakeApplication()) {
        { //Add
        var message = models.message.AddRawStreamMessage("munca", "helloworld", 10, "magic", "ini coba - coba")
          var request = FakeRequest.apply(POST, "/thridparty/add_stream")
            .withJsonBody(Json.toJson(message))
          var response = route(request)
          response.isDefined mustEqual true
          var result = Await.result(response.get, timeout)
          result.header.status must equalTo(BAD_REQUEST)
        }
      }
    }
  }
}