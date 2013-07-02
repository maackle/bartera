package controllers

import play.api.data.Form
import play.api.data.Forms._
import models._
import play.api.libs.json.Json
import play.api.mvc.Action

import SQ._
import views.html.helper.form
import scala.Some

object Wants extends Items[Want] {

	val table = Want.table

	def userToItems(implicit user:User) = user.wants

	def index = IsAuthenticated { implicit user => implicit request =>
		Ok(views.html.user.wants.index(Forms.addWant, userItems(0)))
	}

	def add = IsAuthenticated { implicit user => implicit request =>

		Forms.addWant.bindFromRequest().fold (
			form => BadRequest(views.html.user.wants.index(form, userItems(0)))
//			form => BadRequest(form.errorsAsJson)
			,
			want => {
				transaction { table.insert(want) }
				Redirect(routes.Wants.index)
			}
		)
	}

	def detail(want_id:Long) = Action { implicit request =>
		transaction(table.lookup(want_id)).map { want =>
			Ok(views.html.wants.detail(want))
		}
		.getOrElse {
			Redirect(routes.Application.index).flashing(msgError("This item does not exist or has been removed."))
		}

	}

	def edit(want_id:Long) = IsAuthenticated { implicit user => implicit request =>
		transaction(table.lookup(want_id)).map { want =>
			Ok(views.html.user.wants.edit(Forms.editWant.fill(want), want))
		}
		.getOrElse {
			Redirect(routes.Wants.index)
		}

	}

	def update(want_id:Long) = IsAuthenticated { implicit user => implicit request =>
		Forms.editWant.bindFromRequest().fold (
			form => BadRequest(views.html.user.wants.index(form, userItems(0)))
			,
			want => transaction {
				want.id = want_id
				table.update(want)
				Redirect(routes.Wants.index)
			}
		)
	}

	object Forms {

		def addWant(implicit user:User) = Form(
			mapping(
				"what" -> nonEmptyText,
				"description" -> text
			)
			( Want(_, _, user.id) )
			( item => Some(item.what, item.description) )
		)

		def editWant(implicit user:User) = Form(
			mapping(
				"what" -> nonEmptyText,
				"description" -> text
			)
			( Want(_, _, user.id) )
			( item => Some(item.what, item.description) )
		)
	}

}
