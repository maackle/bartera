package controllers.dev

import controllers.Common
import play.api.mvc.{Action, Controller}
import models.DB

object Schema extends Controller with Common {

	def rebuild = Action {
		transaction {
			DB.drop
			DB.create
		}
		Ok("schema rebuilt")
	}
}
