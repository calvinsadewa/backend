package controllers

import java.util.concurrent.TimeUnit

import org.specs2.mutable._
import play.api.Logger
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent._
import scala.concurrent.duration._


/**
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class StreamsPT extends Specification{

  //Use with test database (dump in test folder)
  val timeout: FiniteDuration = FiniteDuration(5, TimeUnit.SECONDS)

  "StreamsP" should {
    "retrive streams with specified id" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(GET, "/stream/557a7c0a7e26b84302120c92")
        val response = route(request)
        response.isDefined mustEqual true
        val t1 = System.currentTimeMillis()
        val result = Await.result(response.get, timeout)
        val t2 = System.currentTimeMillis()
        Logger.info("[PF]retrive stream in " + (t2-t1) + "ms")
        response.isDefined mustEqual true
      }
    }
  }
}