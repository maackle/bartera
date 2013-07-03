package controllers

import play.api._
import play.api.mvc._
import models.{SQ, ItemImage}
import SQ._
import core.DBEngine
import play.api.data._
import play.api.data.Forms._

object Application extends Common {
  
	def index = Action { implicit request =>
		DBEngine.current(Play.current)
		Ok(views.html.index())
	}

	def viewImage(image_id:Long, width:Int, height:Int) = Action {
		val image = transaction { ItemImage.table.get(image_id) }
		val (bytes, contentType) = image.serve(width, height)
		Ok(bytes).as(contentType)
	}

	val locationForm = Form(
		single(
			"zipcode" -> Fields.zipcode
		)
	)

	def editLocation = Action { implicit request =>
		val form = request.session.get("zipcode").map(locationForm.fill).getOrElse(locationForm)
		Ok(views.html.edit_location(form))

	}

	def updateLocation = Action { implicit request =>

		locationForm.bindFromRequest().fold(
			form => BadRequest(views.html.edit_location(form)),
			zipcode => {
				Redirect("/").withSession(request.session + ("zipcode" -> zipcode)).flashing(msgSuccess("location updated"))
			}
		)
	}

//	def viewImage(image_id:Long, size:Int):Action[_] = viewImage(image_id, size, size)
}
