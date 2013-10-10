package models

import java.util.Date

case class Item(id: Option[Long] = None,
	  attribute: Option[String] = None,
	  description: Option[String] = None,
	  specialOfferPrice: Option[Double] = None,
	  specialPriceStart: Option[Date] = None,
	  specialPriceEnd: Option[Date] = None,
	  specialPriceActive: Option[Boolean] = None,
	  salePrice: Option[Double] = None,
	  listedPrice: Option[Double] = None,
	  quantity: Option[Int] = None,
	  rebate: Option[Boolean] = None,
	  inStock: Option[Boolean] = None,
	  manufactureModelNumber: Option[String] = None,
	  barCode: Option[String] = None,
	  pageLocator: Option[String] = None,
	  active: Option[Boolean]
)
{

}