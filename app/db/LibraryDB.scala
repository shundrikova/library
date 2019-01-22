package db


import models._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}

class LibraryDB {
  val codecRegistry = fromRegistries(fromProviders(classOf[Author], classOf[Book]), DEFAULT_CODEC_REGISTRY)

  //val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val mongoClient: MongoClient = MongoClient("mongodb://user:password1234@ds163694.mlab.com:63694/library")

  val database: MongoDatabase = mongoClient.getDatabase("library").withCodecRegistry(codecRegistry)

  val booksCollection: MongoCollection[Book] = database.getCollection("books")
  val authorsCollection: MongoCollection[Author] = database.getCollection("authors")
}