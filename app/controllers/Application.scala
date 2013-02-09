package controllers

import java.lang
import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Application extends Controller with ProvidesHeader {

  def index = Action { implicit request =>
    Ok(views.html.index("Bazzar Store"))
  }

}

trait ProvidesHeader extends Controller {

  implicit def common[A](implicit request: Request[A]): Common = {
    val result: Future[JsValue] =
      WS.url("http://yben.no-ip.org:8080/bazzar_base/categories/")
        .withTimeout(2000)
        .get
        .map { response => response.json }
    val menu: JsValue = Await.result(result, Duration.Inf) \ "menu"
    Common(Header(menu), Sidebar(request.path))
  }

}

case class Common(header: Header, sidebar: Sidebar)
case class Header(menu: JsValue)
case class Sidebar(menu: String)
