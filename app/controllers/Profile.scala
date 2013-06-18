package controllers

import play.api.data._
import play.api.data.Forms._
import models.{SQ, Have, User}
import SQ._

object Profile extends Secured {

	def edit = IsAuthenticated { implicit user => implicit request =>
		Ok(Views.edit(Forms.edit.fill(user.profile)))
	}

	def save = IsAuthenticated { implicit user => implicit request =>
		Forms.edit.bindFromRequest.fold(
			form => {
				BadRequest(Views.edit(form))
			},
			profile => {
				transaction { models.Profile.table.update(profile) }
				Redirect(routes.Profile.edit).flashing(msgSuccess("Profile updated"))
			}
		)
	}

	private object Views {
		lazy val edit = views.html.user.profile.edit
	}

	private object Forms {
		def edit(implicit user:User) = {
			Form( mapping(
				//				"name" -> text,
				"zipcode" -> Fields.zipcode
			)
				(models.Profile(user.id, _:String, None))
				((up:models.Profile) => Some(up.zipcode))
			)
		}
	}
}
