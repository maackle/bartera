package core

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB

case class LatLng(lat:Float, lng:Float) {

	def toWKT = s"POINT($lng $lat)"
	def geoString = s"ST_GeographyFromText('$toWKT')"
}

object LatLng {
	def fromWKT(wkt:String):LatLng = {
		val r = ("""POINT\(\s*(\S+)\s*(\S+)\s*\)""".r)
		wkt match {
		case r(lng, lat) =>
			LatLng(lat.toFloat, lng.toFloat)
		case oops =>
			throw new Exception("tried to parse invalid WKT: " + oops)
		}
	}

//	def fromZipcode(zip:String):LatLng = {
//		DB.withConnection { implicit c =>
//			val wkt = SQL(s"SELECT ST_AsText(latlng) FROM locations WHERE zipcode = '$zip'").as(scalar[String].single)
//			LatLng.fromWKT(wkt)
//		}
//	}
}
