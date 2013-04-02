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
import java.util.UUID

object Application extends Controller with ProvidesHeader {

  def index = Action { implicit request =>
    Ok(views.html.index("Bazzar Store"))
  }

}

trait ProvidesHeader extends Controller {

  implicit def common[A](implicit request: Request[A]): Common = {
    session.get("token").map { token =>
      // Do nothing
    }.getOrElse {
      session + ("token" -> UUID.randomUUID().toString())
    }

    val result: Future[JsValue] =
      WS.url("http://localhost:8080/bazzar_online/menu/category/")
        .withTimeout(2000)
        .get
        .map { response => response.json }
    val menu: JsValue = Await.result(result, Duration.Inf) \ "category"
    Common(Header(menu), Sidebar(request.path))
  }

}

case class Common(header: Header, sidebar: Sidebar)
case class Header(menu: JsValue)
case class Sidebar(menu: String)
