package parser

import com.fasterxml.jackson.core.JsonParseException
import models.{Stream, AnalysisType}
import play.api.libs.json.Json
import play.api.mvc.BodyParsers

/** Twitter string parser.
 * Created by calvin-pc on 6/30/2015.
 */
class TwiterParser extends Parser{
  def parse(content: String,
            maxvalidasi: Int,
            analysis: Seq[AnalysisType],
            idprovider:String,
            idrawstream:String): Option[Stream] =
  try Json.fromJson(Json.parse(content)).asOpt.map( tweet =>
      Stream(idrawstream,
        idprovider,
        maxvalidasi,
        tweet.text,
        tweet.created_at,
        0,
        analysis)
    )
    catch{
      case ex: JsonParseException => None
      case e: Throwable => throw e
    }

  implicit val tweetFormat = Json.format[Tweet]

  case class Tweet(created_at: String, text: String)
}
