package models

case class Orders(id: Option[Long] = None, sessionNumber: Option[String] = None, ip: Option[String] = None, detail: List[OrderDetail] = Nil)
{

}