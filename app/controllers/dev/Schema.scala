package controllers.dev

import app.Common
import play.api.mvc.{Action, Controller}
import models.{User, DB}

object Schema extends Controller with Common {

	def rebuild = Action {
		transaction {
			DB.drop
			DB.create
			seed()
		}
		Ok("schema rebuilt")
	}

	def seed() = inTransaction {
		User.register(User("michael@lv11.co", "q1w2e3"))
	}
}
