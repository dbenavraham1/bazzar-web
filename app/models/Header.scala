package models

import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

case class Header(menu: Option[List[MenuCategory]])

object Header {
  implicit val headerReads: Reads[Header] = (__ \ 'category).readNullable(
    Reads.list(__.read[MenuCategory])).map(tags => Header(tags))
}