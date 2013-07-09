package models

import play.api.db.DB
import anorm._
import scala.Some
import anorm.SqlParser._
import anorm.~
import scala.Some
import core.{LatLng}
import SQ._
import play.api.mvc.Request
import controllers.dev.Settings

/**
 * This model is created manually through anorm
 */

case class Location(zipcode:Option[String], latlng:LatLng, id:Long) {

}

object Location extends core.Common {

	def get(id:Long):Location = {
		DB.withConnection { implicit c =>
			val tup:(String, String, Long) = SQL(s"SELECT zipcode, ST_AsText(latlng) as geom, id FROM locations WHERE id = '$id'").as((str("zipcode") ~ str("geom") ~ long("id")).map(flatten) single)
			Location(Some(tup._1), LatLng.fromWKT(tup._2), tup._3)
		}
	}

	def fromZipcode(zip:String):Location = {
		DB.withConnection { implicit c =>
			val tup:Option[(String, String, Long)] = SQL(s"SELECT zipcode, ST_AsText(latlng) as geom, id FROM locations WHERE zipcode = '$zip'").as((str("zipcode") ~ str("geom") ~ long("id")).map(flatten) singleOpt)
			tup.map { tup =>
				Location(Some(tup._1), LatLng.fromWKT(tup._2), tup._3)
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

	def currentZipcode(implicit request:Request[_]) = {
		request.session.get("zipcode")
	}

	def randomZipcode:String = {
		val zips = Settings.zipcodeSubset.toVector
		zips((math.random*zips.size).toInt)
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
