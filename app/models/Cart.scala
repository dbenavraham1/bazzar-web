package models

case class Cart(id: Option[Long] = None, sessionNumber: Option[String] = None, ip: Option[String] = None, customer_id: Option[String] = None, var shoppingCartSubTotal: Option[Double] = None, itemCount: Option[Int] = None, var details: Option[List[CartDetail]] = None)
{

}