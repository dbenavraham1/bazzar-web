package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.ValidationError
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.UUID
import models.CartDetail
import models.Cart
import models.Item
import scala.collection.mutable.ListBuffer

class CartController extends Controller with ProvidesHeader with JsonConverters {

  def index = Action { implicit request =>
    val uuid = getUuid()
    val cart = getCartByUuid(uuid)

    Ok(views.html.cart.detail(cart, fillQuantityForm(cart.details))).withCookies(
      Cookie("token", uuid))
  }

  def getItem(id: Long) = {
    val result: Future[Option[JsValue]] =
      WS.url("http://localhost:8080/bazzar_online/item/" + id)
        .withTimeout(2000)
        .get
        .map { response => response.json.asOpt[JsValue] }
    val itemJson: Option[JsValue] = Await.result(result, Duration.Inf)
    Json.fromJson[Item]((itemJson.get \ "item")).get
  }

  def detail(id: Long) = Action { implicit request =>
    val uuid = getUuid()
    val cartJson = getCartJsonByUuid(uuid)

    val item: Item = getItem(id)
    if (cartJson.isEmpty) {
      val cart = Cart(sessionNumber = Some(uuid), ip = Some(request.remoteAddress), shoppingCartSubTotal = item.listedPrice,
        itemCount = Some(1), details = Some(List(CartDetail(itemId = item.id, price = item.listedPrice, qty = item.quantity, subject = item.subject))))
      updateCart(cart, uuid)
    } else {
      val cart = Json.fromJson[Cart]((cartJson.get \ "cart")).get
      cart.shoppingCartSubTotal = Some(cart.shoppingCartSubTotal.get + item.listedPrice.get)
      var addItem = true
      cart.details.get.foreach { detail =>
        if (detail.id.get == item.id.get) {
          detail.qty = Some(detail.qty.get + 1)
          addItem = false
        }
      }
      
      if (addItem) {
        val cartDetails = ListBuffer[CartDetail]()
        cart.details.get.foreach { cartDetail => cartDetails += cartDetail }
        cartDetails += new CartDetail(itemId = item.id, price = item.listedPrice, qty = Some(1), subject = item.subject)
        cart.details = Some(cartDetails.toList)
      }
      
      updateCart(cart, uuid)
    }
  }

  def updateCart(cart: Cart, uuid: String)(implicit common: Common) = {
    val updatedCartResult: Future[Option[JsValue]] =
      WS.url("http://localhost:8080/bazzar_online/cart/")
        .withTimeout(2000)
        .put(Json.toJson(cart))
        .map { response => response.json.asOpt[JsValue] }
    val updatedCartJson: Option[JsValue] = Await.result(updatedCartResult, Duration.Inf)
    val updatedCart = Json.fromJson[Cart]((updatedCartJson.get \ "cart")).get

    Ok(views.html.cart.detail(updatedCart, fillQuantityForm(updatedCart.details))).withCookies(
      Cookie("token", uuid))
  }

  def fillQuantityForm(details: Option[List[CartDetail]]) = {
    quantityForm.fill(Cart(details = Option(details.get)))
  }

  def delete(id: Long, detailId: Long) = Action { implicit request =>
    val result: Future[Option[JsValue]] =
      WS.url("http://localhost:8080/bazzar_online/cart/" + id + "/detail/" + detailId)
        .withTimeout(2000)
        .delete
        .map { response => response.json.asOpt[JsValue] }
    val cartJson: Option[JsValue] = Await.result(result, Duration.Inf)
    val cart = Json.fromJson[Cart]((cartJson.get \ "cart")).get
    Ok(views.html.cart.detail(cart, fillQuantityForm(cart.details)))
  }

  val quantityForm = Form(
    mapping(
      "id" -> optional(longNumber(min = 0)),
      "details" -> optional(list(
        mapping("id" -> optional(longNumber(min = 0)),
          "qty" -> optional(number(min = 0)))((id, qty) => CartDetail(id = id, qty = qty))((cartDetail: CartDetail) => Some(cartDetail.id, cartDetail.qty)))))((id, details) => Cart(id = id, details = details))((cart: Cart) => Some(cart.id, cart.details)))

  def updateQuantity = Action { implicit request =>
    val uuid = getUuid()
    val cart = getCartByUuid(uuid)

    quantityForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.cart.detail(cart, formWithErrors)),
      cartData => updateQuantities(cart, cartData))
  }

  def updateQuantities(cart: Cart, cartData: Cart)(implicit common: Common) = {

    val details: Seq[JsValue] = cartData.details.get.map { detail =>
      Json.obj("id" -> detail.id,
        "qty" -> detail.qty)
    }

    val result: Future[Option[JsValue]] =
      WS.url("http://localhost:8080/bazzar_online/cart/" + cart.id.get + "/update/quantity")
        .withTimeout(2000)
        .put(Json.obj("details" -> JsArray(details)))
        .map { response =>
          response.json.asOpt[JsValue]
        }
    val updatedCartJson: Option[JsValue] = Await.result(result, Duration.Inf)
    val updatedCart = Json.fromJson[Cart]((updatedCartJson.get \ "cart")).get

    Ok(views.html.cart.detail(updatedCart, fillQuantityForm(updatedCart.details)))
  }

  def getUuid()(implicit request: RequestHeader) = {
    session.get("token").map { token =>
      token
    }.getOrElse {
      request.cookies.get("token").map { token =>
        token.value
      }.getOrElse {
        val uuid: String = UUID.randomUUID().toString()
        session + ("token" -> uuid)
        uuid
      }
    }
  }

  def getCartJsonByUuid(uuid: String) = {
    val result: Future[Option[JsValue]] =
      WS.url("http://localhost:8080/bazzar_online/cart/find/session/" + uuid)
        .withTimeout(2000)
        .get
        .map { response => response.json.asOpt[JsValue] }
    val cartJson: Option[JsValue] = Await.result(result, Duration.Inf)
    if (cartJson.isEmpty) {
      BadRequest("Expecting Cart Json data")
    }
    cartJson
  }

  def getCartByUuid(uuid: String) = {
    val cartJson = getCartJsonByUuid(uuid);
    Json.fromJson[Cart]((cartJson.get \ "cart")).get
  }
}
