package controllers

import play.api._
import play.api.mvc._
import models.{SQ, ItemImage}
import SQ._
import core.DBEngine

object Application extends Common {
  
	def index = Action { implicit request =>
		DBEngine.current(Play.current)
		Ok(views.html.index("Your new application is ready."))
	}

	def viewImage(image_id:Long, width:Int, height:Int) = Action {
		val image = transaction { ItemImage.table.get(image_id) }
		val (bytes, contentType) = image.serve(width, height)
		Ok(bytes).as(contentType)
	}

//	def viewImage(image_id:Long, size:Int):Action[_] = viewImage(image_id, size, size)
}
