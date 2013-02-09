package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global

class Products extends Controller with ProvidesHeader {

  def detail(id: Long) = Action { implicit request =>
    Async {
      WS.url("http://yben.no-ip.org:8080/bazzar_base/product/" + id + "/").get().map { response =>
        Ok(views.html.product.detail(response.json \ "menu"))
      }
    }
  }

}