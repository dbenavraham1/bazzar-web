package models

case class CartDetail(id: Option[Long] = None, itemId: Option[Long] = None, price: Option[Double] = None, var qty: Option[Int] = None, subject: Option[String] = None)
{

}