package models

import org.mongodb.scala.bson.ObjectId

case class Author(
  _id: ObjectId = new ObjectId(),
  name: String,
  books: List[String]
)
