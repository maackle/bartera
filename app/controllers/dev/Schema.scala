package controllers.dev

import anorm._
import play.api.mvc.{Action, Controller}
import models.{SQ, User}
import play.api.db.DB
import play.api.Play.current
import controllers.Common
import SQ._

object Schema extends Common {

	def rebuild = Action {
		info("schema rebuild begin")
		transaction {
			info("schema rebuild - in transaction")
			models.Schema.drop
			info("schema dropped")
			models.Schema.create
			info("schema created")
		}
		DB.withConnection { implicit c =>
			val q = s"SELECT AddGeometryColumn('${models.Schema.zipcodes.name}', 'geom', 4326, 'POINT', 2 );"
			val result: Boolean = SQL(q).execute()
		}
		info("raw sql executed")
		transaction {
			seed()
		}
		info("seeded")
		Ok("schema rebuilt")
	}

	def seed() = inTransaction {
		User.register(User("michael@lv11.co", "q1w2e3"))
	}
}
