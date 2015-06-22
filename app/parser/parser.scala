package parser

import java.util.Date

import models.JsonFormats._
import models.{AnalysisType, Stream}
import play.api.libs.json._

/**
 * Created by calvin-pc on 6/11/2015.
 */
/**
 * Base class of Parser which transform content of rawstream
 */
abstract class Parser {
  def parse(content:String,
            maxvalidasi:Int,
            analysis: Seq[AnalysisType],
            idprovider:String = "",
            idrawstream:String = "") : Option[Stream]
}

class ParserMatcher{
  def getParser(filetype:String):Option[Parser] = {
    filetype match {
      case "raw" => Some(new DirectParser)
      case _ => None
    }
  }
}

class DirectParser extends Parser{
  def parse(content:String,
            maxvalidasi:Int,
            analysis: Seq[AnalysisType],
            idprovider:String = "",
            idrawstream:String = "") = {
    Option(Stream(idrawstream,idprovider,maxvalidasi,content,new Date().toString,0,analysis))
  }
}