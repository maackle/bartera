package controllers

import models.{ItemImage, Have, User, ItemBase}
import org.squeryl.Table
import org.squeryl.dsl.OneToMany
import models.SQ._
import play.templates.BaseScalaTemplate
import play.api.mvc.SimpleResult
import play.api.libs.json.Json

trait Items[A <: ItemBase] extends Secured {

	val havesPerPage = 25
	val table:Table[A]
	def userToItems(implicit user:User):OneToMany[A]

	def userItems(page:Int = 0)(implicit user:User):Vector[A] = transaction {
		userToItems.drop(page*havesPerPage).take(havesPerPage).toVector
	}


	def addImage(item_id:Long) = IsAuthenticated(parse.multipartFormData) { implicit user => implicit request =>
		val images = transaction {
			//			val have_id = request.body.dataParts.get("have_id").map(_.head).get.toLong

			val item = table.get(item_id)

			request.body.files map { f =>
				val file = f.ref.file
				val im = ItemImage.create(f.ref.file, f.contentType.get)
				ItemImage.table.insert(im)
				item.images.associate(im)
				im
			}
		}

		require(images.length == 1, "there should only be one image uploaded at a time")

		AjaxSuccess(Json.toJson(Map("image" -> images.head.id))).toResult
	}


	def deleteImage(item_id:Long, image_id:Long) = IsAuthenticated { implicit user => implicit request =>
		val success = transaction {
			val have = table.get(item_id)
			val image = ItemImage.table.get(image_id)
			have.images.dissociate(image)

		}
		if(success) {
			AjaxSuccess().toResult
		}
		else {
			AjaxError("couldn't delete image").toResult
		}
	}
}
