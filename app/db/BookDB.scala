package db

import models._
import org.mongodb.scala.{Completed, MongoClient, MongoCollection, MongoDatabase, Observer}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates


class BookDB {
  val codecRegistry = fromRegistries(fromProviders(classOf[Book]), DEFAULT_CODEC_REGISTRY)

  //val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val mongoClient: MongoClient = MongoClient("mongodb://user:password1234@ds163694.mlab.com:63694/library")

  val database: MongoDatabase = mongoClient.getDatabase("library").withCodecRegistry(codecRegistry)

  val booksCollection: MongoCollection[Book] = database.getCollection("books")

  def insert(book: Book) ={
    booksCollection.insertOne(book).toFuture()
  }

  def update(id: String, book:Book, authorList: List[String]) = {
    booksCollection.updateOne(
      equal("_id", new ObjectId(id)),
      Updates.combine(
        Updates.set("title", book.title),
        Updates.set("year", book.year),
        Updates.set("authors", authorList)
      )
    ).toFuture()
  }

  def delete(id:String) = {
    booksCollection.deleteOne(equal("_id", new ObjectId(id))).toFuture()
  }

  def find(id: String) = {
    booksCollection.find(equal("_id",new ObjectId(id))).head()
  }

}
