package controllers.dev

import anorm._
import app.Common
import play.api.mvc.{Action, Controller}
import models.{User, Schema => S}
import play.api.db.DB
import play.api.Play.current

object Schema extends Controller with Common {

	def rebuild = Action {
		transaction {
			S.drop
			S.create
			seed()
		}
		DB.withConnection { implicit c =>
			val q = s"SELECT AddGeometryColumn('${S.zipcodes.name}', 'geom', 4326, 'POINT', 2 );"
			val result: Boolean = SQL(q).execute()
		}
		Ok("schema rebuilt")
	}

	def seed() = inTransaction {
		User.register(User("michael@lv11.co", "q1w2e3"))
	}
}
