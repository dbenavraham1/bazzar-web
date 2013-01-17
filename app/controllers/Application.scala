package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def navbar(id: Long) = Action {
    Async {
      WS.url("http://localhost:9000/clients/" + id + "/categories").get().map { response =>
        Ok(views.html.navbar(response.json))
      }
    }
  }

}