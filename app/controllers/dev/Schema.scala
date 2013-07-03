package controllers.dev

import anorm._
import play.api.mvc.{Action, Controller}
import models.{Schema => S, Location, SQ, User}
import play.api.db.DB
import controllers.Common
import SQ._
import play.api.Play
import java.io.File
import core.LatLng

object Schema extends Common {

	def rebuild = Action {

		transaction {
			models.Schema.drop
			models.Schema.create
		}

		rawBuilding()

		initialize(Some(Settings.zipcodeSubset))
//		initialize(None)

		transaction {
			seed()
		}

		Ok("schema rebuilt")
	}

	private def rawBuilding() {

		DB.withConnection { implicit c =>

			SQL(
				"""
				  |DROP TABLE IF EXISTS locations;
				""".stripMargin).execute()
//
//			Seq(S.zipcodes, S.haves, S.wants).foreach { table =>
//				SQL(s"SELECT AddGeometryColumn('${table.name}', 'latlng', 4326, 'POINT', 2 );").execute()
//			}

			SQL(
				"""
				  |CREATE TABLE locations (
				  |	id SERIAL,
				  |	zipcode VARCHAR(5) NULL,
				  |	text VARCHAR(256) NULL,
				  |	latlng Geography(Point)
				  |);
				""".stripMargin).execute()
		}
	}

	def initialize(zipSubset:Option[Set[String]] = None) = DB.withConnection { implicit c =>

		val locations = for {
			line <- io.Source.fromFile(new File(Play.application.path, "etc/data/Gaz_zcta_national.txt")).getLines.drop(1)
			chunks = line.split("\t")
			zip = chunks(0)
			lat = chunks(7)
			lng = chunks(8)
			if zipSubset.isEmpty || zipSubset.get.contains(zip)
		} yield {
			Location(Some(zip), LatLng(lat.toFloat, lng.toFloat), 0L)
		}
		val plan = """PREPARE fooplan (text, geography, int) AS
						 	INSERT INTO locations (zipcode, latlng, id) VALUES($1, $2, $3);"""

		val query = plan + locations.map( loc => s"EXECUTE fooplan('${loc.zipcode.get}', ${loc.latlng.geoString}, ${loc.id});").mkString("\n")
		SQL(query).execute()

	}

	def seed() = inTransaction {
		User.register(User("michael@lv11.co", "q1w2e3"))
	}
}
