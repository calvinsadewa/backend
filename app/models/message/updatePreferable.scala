package models.message

import models.AnalysisType

/**
 * Created by calvin-pc on 6/12/2015.
 */
case class UpdatePreferable (username:String, password:String, id_analysis_list: Seq[String])
