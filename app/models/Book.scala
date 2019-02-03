package models

import org.mongodb.scala.bson.ObjectId

case class Book(
  _id: ObjectId = new ObjectId(),
  title: String,
  year: Int,
  authors: List[String]
)

