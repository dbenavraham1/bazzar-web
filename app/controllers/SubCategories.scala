package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import models.MenuCategory

class SubCategories extends Controller with ProvidesHeader {

  def detail(id: Long) = Action.async { implicit request =>
    WS.url("http://localhost:8080/bazzar_online/menu/subCategory/" + id + "/").get().map { response =>
      val subCategory = Json.fromJson[MenuCategory](response.json \ "subcategory").get
      val sidebar = views.html.subcategory.sidebar(subCategory)
      val breadcrumb = views.html.subcategory.breadcrumb(subCategory)
      Ok(views.html.subcategory.detail(subCategory, sidebar, breadcrumb))
    }
  }

}