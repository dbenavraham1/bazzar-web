package models

import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

case class MenuCategory(id: Option[Long],
  attribute: Option[String],
  active: Option[Boolean],
  parent: Option[MenuCategory],
  children: Option[List[MenuCategory]])

object MenuCategory {
    implicit lazy val menuCategoryReads: Reads[MenuCategory] = (
      (__ \ 'id).readNullable[Long] and
      (__ \ 'attribute).readNullable[String] and
      (__ \ 'active).readNullable[Boolean] and
      (__ \ 'parent).lazyReadNullable(menuCategoryReads) and
      (__ \ 'children).lazyReadNullable(Reads.list(__.lazyRead(menuCategoryReads))))(MenuCategory.apply _)

    implicit val menuCategoryWrites: Writes[MenuCategory] = (
      (__ \ 'id).writeNullable[Long] ~
      (__ \ 'attribute).writeNullable[String] ~
      (__ \ 'active).writeNullable[Boolean] ~
      (__ \ 'parent).lazyWriteNullable(menuCategoryWrites) ~
      (__ \ 'children).lazyWriteNullable(Writes.traversableWrites[MenuCategory](menuCategoryWrites)))(unlift(MenuCategory.unapply))
}