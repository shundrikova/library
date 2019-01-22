package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models.{Book, Page, _}
import db._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.{AbstractController, ControllerComponents}
import collection._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport

import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model._

@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  ldb: LibraryDB,
) extends AbstractController(cc)
  with I18nSupport{

  def index() = Action { home }

  def home = Redirect(routes.HomeController.list())

  def list = Action.async {
    implicit request =>
      val bookList = ldb.booksCollection.find().toFuture()
      bookList.map(books => Ok(views.html.library(Page(books))))
  }

  val bookForm = Form(
    mapping(
      "id" -> ignored(new ObjectId(): ObjectId),
      "title" -> nonEmptyText,
      "year" -> number,
      "authors" -> play.api.data.Forms.list(nonEmptyText))(Book.apply)(Book.unapply)
  )

  def create = Action { implicit request: Request[AnyContent]=>
    Ok(views.html.create(bookForm))
  }

  def save = Action.async {
    implicit request =>
      bookForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(views.html.create(formWithErrors))),
        book => {
          val authorList = book.authors.head.split(",").map(_.trim).toList
          val futureInsertBook = ldb.booksCollection.insertOne(book.copy(_id = new ObjectId(), authors = authorList)).toFuture()

          for (author <- authorList) {
            val upsertResult = ldb.authorsCollection.updateOne(equal("name", author), Updates.combine(Updates.setOnInsert("name", author), Updates.addToSet("books", book.title)), new UpdateOptions().upsert(true)).head()
            upsertResult.map(result => println(result))
          }

          futureInsertBook.map {result => home}
        })
  }

  def edit(id: String) = Action.async {
    implicit request =>
      val futureBook = ldb.booksCollection.find(equal("_id",new ObjectId(id))).toFuture()
      futureBook.map {
        books: Seq[Book] => Ok(views.html.edit(id, bookForm.fill(Book(books.head._id, books.head.title, books.head.year, List(books.head.authors.mkString(", "))))))
      }
  }

  def update(id: String) = Action.async {
    implicit request =>
      bookForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(views.html.edit(id, formWithErrors))),
        book => {
          val authorList = book.authors.head.split(",").map(_.trim).toList
          val oldBook = ldb.booksCollection.find(equal("_id", new ObjectId(id))).head()
          val futureUpdateBook = ldb.booksCollection.updateOne(equal("_id", new ObjectId(id)), Updates.combine(Updates.set("title", book.title), Updates.set("year", book.year), Updates.set("authors", authorList))).toFuture()

          oldBook.map(oldBook => {
            val deleteBookResult = ldb.authorsCollection.updateMany(equal("books", oldBook.title), Updates.pull("books", oldBook.title)).head()
            deleteBookResult.map(result => println(result))
            val deleteAuthorResult = ldb.authorsCollection.deleteMany(size("books", 0)).head()
            deleteAuthorResult.map(result => println(result))
          })

          for (author <- authorList) {
            val upsertResult = ldb.authorsCollection.updateOne(equal("name", author), Updates.combine(Updates.setOnInsert("name", author), Updates.addToSet("books", book.title)), new UpdateOptions().upsert(true)).head()
            upsertResult.map(result => println(result))
          }

          futureUpdateBook.map { result => home }
        })
  }

  def delete(id: String) = Action.async {
    val book = ldb.booksCollection.find(equal("_id", new ObjectId(id))).head()
    val futureBook = ldb.booksCollection.deleteOne(equal("_id", new ObjectId(id))).toFuture()

    book.map(book => {
      val deleteBookResult = ldb.authorsCollection.updateMany(equal("books", book.title), Updates.pull("books", book.title)).head()
      deleteBookResult.map(result => println(result))
      val deleteAuthorResult = ldb.authorsCollection.deleteMany(size("books", 0)).head()
      deleteAuthorResult.map(result => println(result))
    })

    futureBook.map(i => home)
  }
}
