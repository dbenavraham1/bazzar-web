package models

case class OrderDetail(id: Option[Long] = None, itemId: Option[Long] = None, var price: Option[Double] = None, qty: Option[Int] = None)
{

}