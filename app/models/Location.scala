package models

import play.api.db.DB
import anorm._
import scala.Some
import anorm.SqlParser._
import anorm.~
import scala.Some
import core.{LatLng}
import SQ._

/**
 * This model is created manually through anorm
 */

case class Location(geohash:String, zipcode:Option[String], latlng:LatLng, id:Long) {

}

object Location extends core.Common {

	def get(id:Long):Location = {
		DB.withConnection { implicit c =>
			val tup:(String, String, String, Long) = SQL(s"SELECT geohash, zipcode, ST_AsText(latlng) as geom, id FROM locations WHERE id = '$id'").as((str("geohash") ~ str("zipcode") ~ str("geom") ~ long("id")).map(flatten) single)
			Location(tup._1, Some(tup._2), LatLng.fromWKT(tup._3), tup._4)
		}
	}

	def fromZipcode(zip:String):Location = {
		DB.withConnection { implicit c =>
			val tup:Option[(String, String, String, Long)] = SQL(s"SELECT geohash, zipcode, ST_AsText(latlng) as geom, id FROM locations WHERE zipcode = '$zip'").as((str("geohash") ~ str("zipcode") ~ str("geom") ~ long("id")).map(flatten) singleOpt)
			tup.map { tup =>
				Location(tup._1, Some(tup._2), LatLng.fromWKT(tup._3), tup._4)
			}
			.getOrElse {
				throw new Exception("unknown zip code: " + zip)
//				SQL("INSERT INTO locations (geohash, zipcode, latlng) VALUES ({geohash}, {zipcode}, {latlng})").on(
//					"geohash" -> "XXX",
//					"zipcode" -> zip,
//
//				)
			}
		}
	}
}

trait HasLocation extends Model[HasLocation] {
	protected var location_id:Long = _

	def location = Location.get(location_id)

	def setZipcode(zipcode:String) = {
		val old_location_id = location_id
		val loc = Location.fromZipcode(zipcode)
		location_id = loc.id
		update(meta.table)( o => where(o.location_id === old_location_id) set(o.location_id := location_id))
	}
}
