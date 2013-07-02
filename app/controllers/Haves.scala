package controllers

import play.api.data.Form
import play.api.data.Forms._
import models.{SQ, ItemImage, User, Have}
import play.api.libs.json.Json
import play.api.mvc.Action

import SQ._
import views.html.helper.form

object Haves extends Items[Have] {

	val table = Have.table

	def userToItems(implicit user:User) = user.haves

	def index = IsAuthenticated { implicit user => implicit request =>
		Ok(views.html.user.haves.index(Forms.addHave, userItems(0)))
	}

	def add = IsAuthenticated { implicit user => implicit request =>

		Forms.addHave.bindFromRequest().fold (
			form => BadRequest(views.html.user.haves.index(form, userItems(0)))
//			form => BadRequest(form.errorsAsJson)
			,
			have => {
				transaction { Have.table.insert(have) }
				Redirect(routes.Haves.index)
			}
		)
	}

	def detail(have_id:Long) = Action { implicit request =>
		transaction(table.lookup(have_id)).map { have =>
			Ok(views.html.haves.detail(have))
		}
		.getOrElse {
			Redirect(routes.Application.index).flashing(msgError("This item does not exist or has been removed."))
		}

	}

	def edit(have_id:Long) = IsAuthenticated { implicit user => implicit request =>
		transaction(table.lookup(have_id)).map { have =>
			Ok(views.html.user.haves.edit(Forms.editHave.fill(have), have))
		}
		.getOrElse {
			Redirect(routes.Haves.index)
		}

	}

	def update(have_id:Long) = IsAuthenticated { implicit user => implicit request =>
		Forms.editHave.bindFromRequest().fold (
			form => BadRequest(views.html.user.haves.index(form, userItems(0)))
			,
			have => transaction {
				have.id = have_id
				table.update(have)
				Redirect(routes.Haves.index)
			}
		)
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

		def editHave(implicit user:User) = Form(
			mapping(
				"what" -> nonEmptyText,
				"description" -> text
			)
			( Have(_, _, user.id) )
			( item => Some(item.what, item.description) )
		)
	}

}
