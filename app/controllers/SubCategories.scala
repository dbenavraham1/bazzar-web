package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global

class SubCategories extends Controller with ProvidesHeader {

  def detail(id: Long) = Action { implicit request =>
    Async {
      WS.url("http://localhost:8080/bazzar_base/subCategory/" + id + "/").get().map { response =>
        val subCategory: JsValue = response.json \ "subcategory"
        Ok(views.html.subcategory.detail(subCategory, views.html.subcategory.sidebar(subCategory \ "product")))
      }
    }
  }

}