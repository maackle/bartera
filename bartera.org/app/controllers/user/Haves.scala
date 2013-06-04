package controllers.user

import controllers.{Secured}
import play.api.data.Form
import play.api.data.Forms._
import models.Item
import views.html.helper.form

object Haves extends Secured {

	def index = IsAuthenticated { user => implicit request =>
		Ok(views.html.user.haves.index(Forms.addHave))
	}

	def add = IsAuthenticated { user => implicit request =>
		Forms.addHave.bindFromRequest().fold (
			form => BadRequest(views.html.user.haves.index(form))
			,
			item => Ok(item.toString)
		)
	}

	object Forms {
		val addHave = Form(
			mapping(
				"what" -> nonEmptyText,
				"description" -> text
			)
			( Item(_, _) )
			( item => Some(item.what, item.description) )
		)
	}
}
