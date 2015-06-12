package models

/**
 * Created by calvin-pc on 6/10/2015.
 */

case class AnalysisValueNumber (value_name: String, number_validate: Int)
case class AnalysisType(_id: String, name: String, possible_values: Seq[AnalysisValueNumber])
