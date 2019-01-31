package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import models._
import db._
import Helpers._

import scala.concurrent.Future
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.data.Form
import play.api.data.Forms.{ignored, mapping, nonEmptyText, number}
import play.api.data.Forms
import play.api.i18n.I18nSupport
import org.mongodb.scala.bson.ObjectId

@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  bdb: BookDB,
  adb: AuthorDB
) extends AbstractController(cc)
  with I18nSupport{

  def index() = Action { home }

  def home = Redirect(routes.HomeController.list())

  def list = Action { implicit request =>
    val bookList = bdb.booksCollection.find().results()
    Ok(views.html.list(bookList))
  }

  val bookForm = Form(
    mapping(
      "id" -> ignored(new ObjectId(): ObjectId),
      "title" -> nonEmptyText,
      "year" -> number,
      "authors" -> Forms.list(nonEmptyText))(Book.apply)(Book.unapply)
  )

  def create = Action { implicit request =>
    Ok(views.html.create(bookForm))
  }

  def save = Action.async { implicit request =>
    bookForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.create(formWithErrors))),
      book => {
        val authorList = book.authors.head.split(",").map(_.trim).toList
        bdb.insert(book.copy(_id = new ObjectId(), authors = authorList))

        authorList.map {author =>
          adb.upsert(author, book)
        }

        Future.successful(home)
      })
  }

  def edit(id: String) = Action { implicit request =>
    val book = bdb.find(id)
    Ok(views.html.edit(id, bookForm.fill(Book(book._id, book.title, book.year, List(book.authors.mkString(", "))))))
  }

  def update(id: String) = Action.async { implicit request =>
    bookForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.edit(id, formWithErrors))),
      book => {
        val authorList = book.authors.head.split(",").map(_.trim).toList

        val oldBook = bdb.find(id)

        bdb.update(id, book, authorList)

        adb.delete(oldBook)
        adb.deleteEmpty

        authorList.map {author =>
          adb.upsert(author, book)
        }

          Future.successful(home)
        })
  }

  def delete(id: String) = Action.async {
    val book = bdb.find(id)
    bdb.delete(id)

    adb.delete(book)
    adb.deleteEmpty

    Future.successful(home)
  }
}
