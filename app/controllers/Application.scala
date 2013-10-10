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
import models.CartDetail
import models.Cart
import models.CustomerData
import models.OrderDetail
import models.Orders
import models.Item
import models.MenuCategory
import scala.collection.mutable.ListBuffer
import models.Header

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
    val categories: JsValue = Await.result(result, Duration.Inf)
    val header = Json.fromJson[Header](categories).get
    Common(header)
  }

}

trait JsonConverters {

  implicit val orderDetailReads = Json.reads[OrderDetail]
  implicit val orderReads = Json.reads[Orders]
  implicit val cartDetailReads = Json.reads[CartDetail]
  implicit val cartReads = Json.reads[Cart]
  implicit val itemReads = Json.reads[Item]

  implicit val orderDetailWrites = Json.writes[OrderDetail]
  implicit val orderWrites = Json.writes[Orders]
  implicit val cartDetailWrites = Json.writes[CartDetail]
  implicit val cartWrites = Json.writes[Cart]
  implicit val itemWrites = Json.writes[Item]

}

case class Common(header: Header)
