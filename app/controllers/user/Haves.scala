package controllers.user

import controllers.{Secured}
import play.api.data.Form
import play.api.data.Forms._
import models.{User, Have}
import views.html.helper.form

object Haves extends Secured {

	val havesPerPage = 25

	def userHaves(page:Int = 0)(implicit user:User) = transaction {
		user.haves.drop(page*havesPerPage).take(havesPerPage).toArray
	}

	def index = IsAuthenticated { implicit user => implicit request =>
		val haves:Seq[Have] = Seq()
		Ok(views.html.user.haves.index(Forms.addHave, userHaves(0)))
	}

	def add = IsAuthenticated { implicit user => implicit request =>

		Forms.addHave.bindFromRequest().fold (
			form => BadRequest(views.html.user.haves.index(form, userHaves(0)))
//			form => BadRequest(form.errorsAsJson)
			,
			have => {
				transaction { Have.table.insert(have) }
				Redirect(routes.Haves.index)
			}
		)
	}

	def save = IsAuthenticated { implicit user => implicit request =>

		Forms.addHave.bindFromRequest().fold (
			form => BadRequest(form.errorsAsJson)
			,
			item => Ok(item.toString)
		)
	}

	def handleImages = IsAuthenticated(parse.multipartFormData) { implicit user => implicit request =>
		request.body.files map { file =>
			file.ref

		}

		Ok("got image?")
	}

	object Forms {
		def addHave(implicit user:User) = Form(
			mapping(
				"what" -> nonEmptyText,
				"description" -> text
			)
			( Have(_, _, user.id) )
			( item => Some(item.what, item.description) )
		)
	}
}
