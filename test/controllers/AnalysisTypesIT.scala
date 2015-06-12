package controllers

import java.util.concurrent.TimeUnit

import models.AnalysisType
import models.message.ReturnGetAllTypes

import scala.concurrent._
import duration._
import org.specs2.mutable._

import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit
import models.JsonFormats._

/**
 * Created by calvin-pc on 6/12/2015.
 */
class AnalysisTypesIT extends Specification{

  //Use with test database (dump in folder test)

  var spesificType:AnalysisType = _

  val timeout: FiniteDuration = FiniteDuration(5, TimeUnit.SECONDS)
  import models.JsonFormats._
  "AnalysisTypes" should {
    "Return all analysis type" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/analysis/all")
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
        val message = contentAsJson(Future.successful(result)).asOpt[ReturnGetAllTypes]
        message.isDefined mustEqual true
        message.get.content.length mustEqual 2
        message.get.content.forall(
          el =>
            el.name == "Sentimen" || el.name == "Demography"
        )
        spesificType = message.get.content.head
        true
      }
    }
    "Return a analysisType" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/analysis/get/" + spesificType._id)
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(OK)
        val message = contentAsJson(Future.successful(result)).asOpt[AnalysisType]
        message.isDefined mustEqual true
        message.get mustEqual spesificType
      }
    }
    "Return bad request because id not found" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/analysis/get/1")
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(BAD_REQUEST)
      }
    }
  }
}
