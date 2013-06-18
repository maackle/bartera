package models

import org.squeryl.KeyedEntity

case class Zipcode(
							zipcode:Int
							) extends KeyedEntity[Long] {
	val id:Long = zipcode.toLong
}

object Zipcode extends MetaModel[Zipcode] {
	val table = Schema.zipcodes
}
