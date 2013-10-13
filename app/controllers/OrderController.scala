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
import scala.collection.mutable.ListBuffer
import models.Cart
import models.CustomerData
import models.OrderDetail
import models.Orders

class OrderController extends Controller with ProvidesHeader with JsonConverters {

  //  implicit val orderDetailReads = (
  //    (__ \ "id").read[Long] ~
  //    (__ \ "itemId").read[Long] ~
  //    (__ \ "price").read[Float] ~
  //    (__ \ "qty").read[Int])(OrderDetail)
  //
  //  implicit val orderReads = (
  //    (__ \ "id").read[Long] ~
  //    (__ \ "sessionNumber").read[String] ~
  //    (__ \ "ip").read[String] ~
  //    (__ \ "detail").lazyRead(List[OrderDetail](orderDetailReads)))(Orders)

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

    val cartResult: Future[Option[JsValue]] =
      WS.url("http://localhost:8080/bazzar_online/cart/find/session/" + cartSessionNumber)
        .withRequestTimeout(2000)
        .get
        .map { response => response.json.asOpt[JsValue] }
    val cartJson: Option[JsValue] = Await.result(cartResult, Duration.Inf)
    if (cartJson.isEmpty) {
      BadRequest("Expecting Cart Json data")
    }
    val cart = Json.fromJson[Cart]((cartJson.get \ "cart")).get

    val orderResult: Future[Option[JsValue]] =
      WS.url("http://localhost:8080/bazzar_online/order/find/session/" + cartSessionNumber)
        .withRequestTimeout(2000)
        .get
        .map { response => response.json.asOpt[JsValue] }
    val orderJson: Option[JsValue] = Await.result(orderResult, Duration.Inf)
    val existingOrder = Json.fromJson[Orders]((orderJson.get \ "order")).getOrElse(Orders())
    if (existingOrder.id.isEmpty) {
      val newOrderDetails = ListBuffer[OrderDetail]()
      cart.details.get.foreach { cartDetail => newOrderDetails += new OrderDetail(itemId = cartDetail.itemId, price = cartDetail.price, qty = cartDetail.qty) }
      val newOrder = Orders(sessionNumber = Some(cartSessionNumber), ip = Some(request.remoteAddress), detail = newOrderDetails.toList)
      val result: Future[JsValue] =
        WS.url("http://localhost:8080/bazzar_online/order/")
          .withRequestTimeout(2000)
          .post(Json.toJson(newOrder))
          .map { response => response.json }
      val updatedOrder: JsValue = Await.result(result, Duration.Inf)

      Ok(views.html.order.detail(Json.fromJson[Orders]((updatedOrder \ "order")).get, fillOrderForm((updatedOrder \ "customer").asOpt[JsValue])))
    } else {
//      val result: Future[JsValue] =
//        WS.url("http://localhost:8080/bazzar_online/order/")
//          .withRequestTimeout(2000)
//          .post(Json.toJson(existingOrder))
//          .map { response => response.json }
//      val updatedOrder: JsValue = Await.result(result, Duration.Inf)
//
//      Ok(views.html.order.detail(Json.fromJson[Orders]((updatedOrder \ "order")).get, fillOrderForm((updatedOrder \ "customer").asOpt[JsValue])))
      Ok(views.html.order.detail(existingOrder, fillOrderForm((orderJson.get \ "customer").asOpt[JsValue])))
    }
  }

}
