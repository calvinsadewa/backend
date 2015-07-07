package parser

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

import com.fasterxml.jackson.core.JsonParseException
import models.{AdditionalContent, Stream, AnalysisType}
import play.api.libs.json.{JsResult, Json}

/**
 * Created by calvin-pc on 7/3/2015.
 */

object FacebookParser {
  val FacebookDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  implicit val facebookCommentFormat = Json.format[FacebookComment]
  implicit val facebookCommentsFormat = Json.format[FacebookComments]
  implicit val facebookPostFormat = Json.format[FacebookPost]
}

/** Facebook string parser.
 *
 */
class FacebookParser extends Parser{
  //content priority : name > message
  import FacebookParser.facebookPostFormat
  def parse(
             content: String,
             maxvalidasi: Int,
             analysis: Seq[AnalysisType],
             idprovider: String,
             idrawstream: String): Option[Stream] =
    try Json.parse(content).validate[FacebookPost].asOpt.map { post =>
      val content:String =
        post.name.getOrElse(
          post.message.getOrElse("")
        )
      val processed:Seq[(String,Option[String])] =
        ("description",post.description) +:
          post.comments.map(
            _.data.map(
              _.map(c => ("comment",Some(c.message)))
            ).getOrElse(Nil)
          ).getOrElse(Nil)
      val additional:Seq[AdditionalContent] = processed.filter{
        case (c,o) => o.isDefined
      }.map{
        case (c,o) => AdditionalContent(c,o.get)
      }
      val dateTime = ZonedDateTime.parse(post.created_time,FacebookParser.FacebookDateFormat)
      val UTC = new Date(dateTime.toEpochSecond*1000).toString
      Stream(idrawstream,idprovider,maxvalidasi,content,UTC,0,analysis,Some(additional))
    }
   catch  {
      case ex: JsonParseException => None
      case e:Throwable => throw e
   }
}

case class FacebookPost(name:Option[String],
                        description:Option[String],
                        message:Option[String],
                        comments:Option[FacebookComments],
                        created_time:String)

case class FacebookComments(data:Option[Seq[FacebookComment]])

case class FacebookComment(message:String)