package db

import db.Helpers._
import models._
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters.{equal, size}
import org.mongodb.scala.model.{UpdateOptions, Updates}

class AuthorDB extends CommonDB {
  val authorsCollection: MongoCollection[Author] = database.getCollection("authors")

  def upsert(authorName: String, book: Book) = {
    authorsCollection.updateOne(
      equal("name", authorName),
      Updates.combine(
        Updates.setOnInsert("name", authorName),
        Updates.addToSet("books", book.title)
      ),
      new UpdateOptions().upsert(true)
    ).headResult()
  }

  def delete(book: Book) = {
    authorsCollection.updateMany(equal("books", book.title), Updates.pull("books", book.title)).headResult()
  }

  def deleteEmpty = {
    authorsCollection.deleteMany(size("books", 0)).headResult()
  }
}
