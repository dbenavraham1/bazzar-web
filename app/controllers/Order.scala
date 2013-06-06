package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.ValidationError
import play.api.data.validation.Constraints._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.UUID

class Order extends Controller with ProvidesHeader {

  implicit val orderDetalReads = (
    (__ \ "id").read[Long] ~
    (__ \ "itemId").read[Long] ~
    (__ \ "price").read[Float] ~
    (__ \ "qty").read[Int])(OrderDetail)

  implicit val orderReads = (
    (__ \ "id").read[Long] ~
    (__ \ "sessionNumber").read[String] ~
    (__ \ "ip").read[String] ~
    (__ \ "detail").lazyRead(List[OrderDetail](orderDetalReads)))(Orders)

  val customerForm = Form(
    mapping(
      "id" -> optional(longNumber(min = 0)),
      "firstName" -> text.verifying(nonEmpty),
      "lastName" -> text.verifying(nonEmpty),
      "phone" -> optional(text verifying (pattern(
        """[0-9.+]+""".r,
        error = "A valid phone number is required"))))(CustomerData.apply)(CustomerData.unapply))

  def fillOrderForm(customer: Option[JsValue]) = {
    def phones: Option[Seq[JsValue]] = (customer.get \ "phone").asOpt[Seq[JsValue]]
    def phone = phones.get.find { p => ((p \ "phoneType").asOpt[String] == "H") }
    if (!customer.isEmpty) {
      customerForm.fill(CustomerData((customer.get \ "id").asOpt[Long], (customer.get \ "firstName").as[String],
        (customer.get \ "lastName").as[String], (phone.get \ "phoneType").asOpt[String]))
    } else {
      customerForm
    }
  }

  def createOrUpdate(cartSessionNumber: String) = Action { implicit request =>

    val cartResult: Future[JsValue] =
      WS.url("http://localhost:8080/bazzar_online/cart/find/session/" + cartSessionNumber)
        .withTimeout(2000)
        .get
        .map { response => response.json }
    val cart: JsValue = Await.result(cartResult, Duration.Inf) \ "cart"
    Logger.info("Cart is @#!E@$!R$#@@@@@@#$@!: " + cart)

    val orderResult: Future[Option[JsValue]] =
      WS.url("http://localhost:8080/bazzar_online/order/find/session/" + cartSessionNumber)
        .withTimeout(2000)
        .get
        .map { response => response.json.asOpt[JsValue] }
    val order: Option[JsValue] = Await.result(orderResult, Duration.Inf)
    Logger.info("Order is @#!E@$!R$#@@@@@@#$@!: " + (order.get \ "order"))
    val result: JsResult[Orders] = order.get.validate[Orders]
    Logger.info("result is @#!E@$!R$#@@@@@@#$@!: " + result)

    if (order.isEmpty || (order.get \ "order").toString().equals("null")) {
      val newOrder = Json.obj(
        "sessionNumber" -> cartSessionNumber,
        "ip" -> request.remoteAddress,
        "detail" -> JsArray((cart \ "detail").as[Seq[JsValue]].map { item =>
          Json.obj("itemId" -> (item \ "itemId"),
            "price" -> (item \ "price"), "qty" -> (item \ "qty"))
        }))

      Logger.info("New Order is: " + newOrder)
      val result: Future[JsValue] =
        WS.url("http://localhost:8080/bazzar_online/order/")
          .withTimeout(2000)
          .post(newOrder)
          .map { response => response.json }
      val updatedOrder: JsValue = Await.result(result, Duration.Inf)

      Ok(views.html.order.detail((updatedOrder \ "order"), fillOrderForm((updatedOrder \ "customer").asOpt[JsValue])))
    } else {
      val detailTransformer = ((__ \ 'detail).json.update(__.read[JsArray].map {
        case JsArray(items) => JsArray((cart \ "detail").as[Seq[JsValue]].map { item =>
          Json.obj("itemId" -> (item \ "itemId"),
            "price" -> (item \ "price"), "qty" -> (item \ "qty"))
        })
      }))

      val existingOrder = order.get.transform(detailTransformer).get
      val result: Future[JsValue] =
        WS.url("http://localhost:8080/bazzar_online/order/")
          .withTimeout(2000)
          .post(existingOrder)
          .map { response => response.json }
      val updatedOrder: JsValue = Await.result(result, Duration.Inf)

      Ok(views.html.order.detail((updatedOrder \ "order"), fillOrderForm((updatedOrder \ "customer").asOpt[JsValue])))
    }

  }

}

case class CustomerData(id: Option[Long], firstName: String, lastName: String, phone: Option[String])
case class OrderDetail(id: Long, itemId: Long, price: Float, qty: Int)
case class Orders(id: Long, sessionNumber: String, ip: String, detail: List[OrderDetail] = Nil)