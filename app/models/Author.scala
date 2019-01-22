package models

import org.mongodb.scala.bson.ObjectId

case class Author (
  _id: ObjectId,
  name: String,
  books: List[String]
)

object Author {
  def apply(name: String, books: List[String]): Author = Author(new ObjectId(), name, books)
}
