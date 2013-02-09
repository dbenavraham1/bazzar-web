package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global

class SubCategories extends Controller with ProvidesHeader {

  def detail(id: Long) = Action { implicit request =>
    Async {
      WS.url("http://yben.no-ip.org:8080/bazzar_base/subCategory/" + id + "/").get().map { response =>
        val menu: JsValue = response.json \ "menu"
        Ok(views.html.subcategory.detail(menu, views.html.subcategory.sidebar(menu \ "product")))
      }
    }
  }

}