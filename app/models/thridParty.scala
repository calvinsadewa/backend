package models

case class ThridParty(
                       _id: String,
                       username: String,
                       password: String,
                       preferable_analysis: Seq[String])