package db

import models._
import Helpers._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.model.Filters.{equal, size}
import org.mongodb.scala.model.{UpdateOptions, Updates}

class AuthorDB {
  val codecRegistry = fromRegistries(fromProviders(classOf[Author]), DEFAULT_CODEC_REGISTRY)

  //val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val mongoClient: MongoClient = MongoClient("mongodb://user:password1234@ds163694.mlab.com:63694/library")

  val database: MongoDatabase = mongoClient.getDatabase("library").withCodecRegistry(codecRegistry)

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

  def deleteEmpty ={
    authorsCollection.deleteMany(size("books", 0)).headResult()
  }
}
