package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.templates.Html;
import models.MenuCategory

class Items extends Controller with ProvidesHeader {

  def detail(id: Long) = Action.async { implicit request =>
    WS.url("http://localhost:8080/bazzar_online/item/" + id + "/").get().map { response =>
      val item = Json.fromJson[MenuCategory](response.json \ "item").get
      val sidebar = views.html.item.sidebar(item)
      val breadcrumb = views.html.item.breadcrumb(item)
      Ok(views.html.item.detail(item, sidebar, breadcrumb))
    }
  }

}