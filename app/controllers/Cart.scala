package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.UUID

class Cart extends Controller with ProvidesHeader {

  def index = Action { implicit request =>
    val uuid: String = session.get("token").map { token =>
      Logger.info("Existing Token:  " + token)
      token
    }.getOrElse {
      val uuid: String = UUID.randomUUID().toString()
      Logger.info("New Token:  " + uuid)
      session + ("token" -> uuid)
      uuid
    }

    val result: Future[JsValue] =
      WS.url("http://localhost:8080/bazzar_online/cart/find/session/" + uuid)
        .withTimeout(2000)
        .get
        .map { response => response.json }
    val cart: JsValue = Await.result(result, Duration.Inf) \ "cart"

    Ok(views.html.cart.detail(cart))
  }

  def index(cart: JsValue) = Action { implicit request =>
    Ok(views.html.cart.detail(cart))
  }

  def getItem(id: Long) = {
    val result: Future[JsValue] =
      WS.url("http://localhost:8080/bazzar_online/item/" + id)
        .withTimeout(2000)
        .get
        .map { response => response.json }
    Await.result(result, Duration.Inf) \ "item"
  }

  def detail(id: Long) = Action { implicit request =>
    val uuid: String = session.get("token").map { token =>
      Logger.info("Existing Token:  " + token)
      token
    }.getOrElse {
      val uuid: String = UUID.randomUUID().toString()
      Logger.info("New Token:  " + uuid)
      session + ("token" -> uuid)
      uuid
    }

    val result: Future[JsValue] =
      WS.url("http://localhost:8080/bazzar_online/cart/find/session/" + uuid)
        .withTimeout(2000)
        .get
        .map { response => response.json }
    var cart: JsValue = Await.result(result, Duration.Inf) \ "cart"
    val item: JsValue = getItem(id)

    val detailTransformer = ((__ \ 'detail).json.update(__.read[JsArray].map {
      case JsArray(items) => JsArray(prepItems(items, item))
    }) andThen (__ \ 'shoppingCartSubTotal).json.update(__.read[JsNumber].map { case JsNumber(total) => JsNumber(total + (item \ "listedPrice").as[Double]) }))

    if (!(cart \ "detail").asOpt[Seq[JsValue]].isEmpty) {
      cart = cart.transform(detailTransformer).get
    } else {
      cart = Json.obj(
        "sessionNumber" -> uuid,
        "ip" -> request.remoteAddress,
        "shoppingCartSubTotal" -> (item \ "listedPrice"),
        "itemCount" -> 1,
        "detail" -> Json.arr(Json.obj(
          "itemId" -> id,
          "price" -> (item \ "listedPrice"),
          "qty" -> 1,
          "subject" -> (item \ "subject"))))
    }

    Async {
      WS.url("http://localhost:8080/bazzar_online/cart/").put(cart).map { response =>
        Ok(views.html.cart.detail(response.json \ "cart"))
      }
    }
  }

  def prepItems(items: Seq[JsValue], itemModel: JsValue) = {
    val qtyTransformer = (__ \ 'qty).json.update(
      __.read[JsNumber].map { case JsNumber(qty) => JsNumber(qty + 1) })

    var foundItem: Boolean = false
    var itemsUpdated: Seq[JsValue] = items.map { item =>
      if ((item \ "itemId").as[Long] == (itemModel \ "id").as[Long]) {
        foundItem = true
        item.transform(qtyTransformer).get
      } else {
        item
      }
    }

    if (foundItem != true) {
      itemsUpdated = itemsUpdated :+ Json.obj(
        "itemId" -> (itemModel \ "id"),
        "price" -> (itemModel \ "listedPrice"),
        "qty" -> 1,
        "subject" -> (itemModel \ "subject"))
    }

    itemsUpdated
  }

  def delete(id: Long, detailId: Long) = Action { implicit request =>
    val result: Future[JsValue] =
      WS.url("http://localhost:8080/bazzar_online/cart/" + id + "/detail/" + detailId)
        .withTimeout(2000)
        .delete
        .map { response => response.json }
    var cart: JsValue = Await.result(result, Duration.Inf) \ "cart"
    Redirect(routes.Cart.index)
  }
}