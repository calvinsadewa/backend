package models.message

import models.FinishedAnalysis

/**
 * Created by calvin-pc on 6/22/2015.
 */
case class ValidateStream(username: String, password:String, id_stream: String, analysis: Seq[FinishedAnalysis])
