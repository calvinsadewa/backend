package models

import models.message._

/**
 * Created by calvin-pc on 6/10/2015.
 */
object JsonFormats {
  import play.api.libs.json.Json

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val finishedAnalysisFormat = Json.format[FinishedAnalysis]
  implicit val directJsonRawStreamFormat = Json.format[AddRawStreamMessage]
  implicit val visitedStreamFormat = Json.format[VisitedStream]
  implicit val addUserFormat = Json.format[AddUser]
  implicit val userFormat = Json.format[User]
  implicit val thridPartyFormat = Json.format[ThridParty]
  implicit val updatePasswordFormat = Json.format[UpdatePassword]
  implicit val checkPasswordFormat = Json.format[CheckPassword]
  implicit val addThridPartyFormat = Json.format[AddThridParty]
  implicit val analysisValueNumberFormat = Json.format[AnalysisValueNumber]
  implicit val analysisTypeFormat = Json.format[AnalysisType]
  implicit val returnGetAllTypesFormat = Json.format[ReturnGetAllTypes]
  implicit val rawStreamFormat = Json.format[RawStream]
  implicit val streamFormat = Json.format[Stream]
  implicit val getStreamIdListFormat = Json.format[GetStreamIdList]
  implicit val returnGetStreamIdListFormat = Json.format[ReturnGetStreamIdList]
  implicit val getPreferableAnalysisFormat = Json.format[GetPreferableAnalysis]
  implicit val returnGetPreferableAnalysisFormat = Json.format[ReturnGetPreferableAnalysis]
  implicit val updatePreferableFormat = Json.format[UpdatePreferable]
  implicit val getRandomStreamFormat = Json.format[GetRandomStream]
  implicit val validateStreamFormat = Json.format[ValidateStream]
  implicit val getVisitedStreams = Json.format[GetVisitedStreams]
}
