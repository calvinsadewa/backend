package models

case class User( _id: String,
                 username: String,
                 password: String,
                 visited_streams: Seq[VisitedStream]
                 )

case class VisitedStream ( id_stream: String,
                 analysis: Seq[FinishedAnalysis]
                 )

case class FinishedAnalysis (analysis_name:String,value:String)