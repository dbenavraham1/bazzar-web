package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global

class Items extends Controller with ProvidesHeader {

  def detail(id: Long) = Action { implicit request =>
    Async {
      WS.url("http://localhost:8080/bazzar_online/item/" + id + "/").get().map { response =>
        Ok(views.html.item.detail(response.json \ "item"))
      }
    }
  }

}