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

    Ok(views.html.cart.detail(cart, fillQuantityForm((cart \ "detail").asOpt[Seq[JsValue]])))
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

    val updatedCartResult: Future[JsValue] =
      WS.url("http://localhost:8080/bazzar_online/cart/")
        .withTimeout(2000)
        .put(cart)
        .map { response => response.json }
    val updatedCart: JsValue = Await.result(updatedCartResult, Duration.Inf) \ "cart"

    Ok(views.html.cart.detail(updatedCart, fillQuantityForm((updatedCart \ "detail").asOpt[Seq[JsValue]])))
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

  def fillQuantityForm(details: Option[Seq[JsValue]]) = {
    var detailList = List[Detail]()
    details.get.map { detail =>
      detailList = detailList :+ Detail((detail \ "id").asOpt[Long], (detail \ "qty").asOpt[Int])
    }

    quantityForm.fill(CartData(Option(0), Option(detailList)))
  }

  def delete(id: Long, detailId: Long) = Action { implicit request =>
    val result: Future[JsValue] =
      WS.url("http://localhost:8080/bazzar_online/cart/" + id + "/detail/" + detailId)
        .withTimeout(2000)
        .delete
        .map { response => response.json }
    val cart: JsValue = Await.result(result, Duration.Inf) \ "cart"
    Ok(views.html.cart.detail(cart, fillQuantityForm((cart \ "detail").asOpt[Seq[JsValue]])))
  }

  val quantityForm = Form(
    mapping(
      "id" -> optional(longNumber(min = 0)),
      "details" -> optional(list(
        mapping("id" -> optional(longNumber(min = 0)),
          "quantity" -> optional(number(min = 0)))(Detail.apply)(Detail.unapply))))(CartData.apply)(CartData.unapply))

  def updateQuantity = Action { implicit request =>
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

    quantityForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.cart.detail(cart, formWithErrors)),
      cartData => updateQuantities(cart, cartData))
  }

  def updateQuantities(cart: JsValue, cartData: CartData)(implicit common: Common) = {

    val details: Seq[JsValue] = cartData.details.get.map { detail =>
      Json.obj("id" -> detail.id,
        "quantity" -> detail.quantity)
    }

    val result: Future[JsValue] =
      WS.url("http://localhost:8080/bazzar_online/cart/update/quantity")
        .withTimeout(2000)
        .put(Json.obj("cartId" -> (cart \ "id"), "details" -> JsArray(details)))
        .map { response => response.json }
    val updatedCart: JsValue = Await.result(result, Duration.Inf) \ "cart"

    Ok(views.html.cart.detail(updatedCart, fillQuantityForm((updatedCart \ "detail").asOpt[Seq[JsValue]])))

  }

  //  // defines a custom reads to be reused
  //  // a reads that verifies your value is not equal to a give value
  //  def notEqualReads[T](v: T)(implicit r: Reads[T]): Reads[T] = Reads.filterNot(ValidationError("validate.error.unexpected.value", v))(_ == v)
  //
  //  def skipReads(implicit r: Reads[String]): Reads[String] = r.map(_.substring(2))
  //
  //  val detailReads: Reads[Detail] = (
  //    (__ \ "id").read[Option[Long]] and
  //    (__ \ "quantity").read[Option[Int]])(Detail)
  //
  //  val detailWrites: Writes[Detail] = (
  //    (__ \ "id").write[Option[Long]] and
  //    (__ \ "quantity").write[Option[Int]])(unlift(Detail.unapply))
  //
  //  val cartDataReads: Reads[CartData] = (
  //    (__ \ "details").read[Option[List[Detail]]])(CartData)
  //
  //  val cartDataWrites: Writes[CartData] = (
  //    (__ \ "details").write[Option[List[Detail]]])(unlift(CartData.unapply))
  //
  //  implicit val detailFormat: Format[Detail] = Format(detailReads, detailWrites)
  //
  //  implicit val cartDataFormat: Format[CartData] = Format(cartDataReads, cartDataWrites)

}

case class CartData(id: Option[Long], details: Option[List[Detail]])
case class Detail(id: Option[Long], quantity: Option[Int])
