package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.templates.Html;
import models.MenuCategory

class Products extends Controller with ProvidesHeader {

  def detail(id: Long) = Action.async { implicit request =>
    WS.url("http://localhost:8080/bazzar_online/menu/product/" + id + "/").get().map { response =>
      val product = Json.fromJson[MenuCategory](response.json \ "product").get
      val breadcrumb = views.html.product.breadcrumb(product)
      Ok(views.html.product.detail(product, Html(""), breadcrumb))
    }
  }

}