package models

/**
 * Created by calvin-pc on 6/10/2015.
 */
case class Stream(
                   _id: String, // Sama dengan id Raw Stream
                   id_provider: String, // id thrid party
                   max_validasi: Int, // batas validasi
                   content: String ,
                   date: String,
                   total_validate: Int,
                   analysis: Seq[AnalysisType]
)
