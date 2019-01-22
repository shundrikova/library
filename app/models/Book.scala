package models

import org.mongodb.scala.bson.ObjectId

case class Book (
  _id: ObjectId,
  title: String,
  year: Int,
  authors: List[String]
)

object Book {
  def apply(title: String, age: Int, authors: List[String]): Book = Book(new ObjectId(), title, age, authors)
}

