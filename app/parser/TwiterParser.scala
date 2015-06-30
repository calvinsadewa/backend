package parser

import models.{Stream, AnalysisType}
import play.api.libs.json.Json

/**
 * Created by calvin-pc on 6/30/2015.
 */
class TwiterParser extends Parser{
  def parse(content: String,
            maxvalidasi: Int,
            analysis: Seq[AnalysisType],
            idprovider:String,
            idrawstream:String): Option[Stream] =
    Json.fromJson(Json.parse(content)).asOpt.map( tweet =>
      Stream(idrawstream,
        idprovider,
        maxvalidasi,
        tweet.text,
        tweet.created_at,
        0,
        analysis)
    )

  implicit val tweetFormat = Json.format[Tweet]

  case class Tweet(created_at: String, text: String)
}
