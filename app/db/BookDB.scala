package db

import db.Helpers._
import models._
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates

class BookDB extends CommonDB {
  val booksCollection: MongoCollection[Book] = database.getCollection("books")

  def insert(book: Book) = {
    booksCollection.insertOne(book).headResult()
  }

  def update(id: String, book: Book, authorList: List[String]) = {
    booksCollection.updateOne(
      equal("_id", new ObjectId(id)),
      Updates.combine(
        Updates.set("title", book.title),
        Updates.set("year", book.year),
        Updates.set("authors", authorList)
      )
    ).headResult()
  }

  def delete(id: String) = {
    booksCollection.deleteOne(equal("_id", new ObjectId(id))).headResult()
  }

  def find(id: String) = {
    booksCollection.find(equal("_id", new ObjectId(id))).headResult()
  }

}
