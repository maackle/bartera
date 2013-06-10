package controllers.user

import controllers.{Secured}
import play.api.data.Form
import play.api.data.Forms._
import models.{ItemImage, User, Have}
import views.html.helper.form
import java.io.{ByteArrayInputStream, FileInputStream}
import play.api.mvc.Action
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

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

	def edit(have_id:Long) = IsAuthenticated { implicit user => implicit request =>
		val have = transaction { Have.table.get(have_id) }
		Ok(views.html.user.haves.edit(Forms.editHave, have))
	}

	def update(have_id:Long) = IsAuthenticated { implicit user => implicit request =>
		Forms.editHave.bindFromRequest().fold (
			form => BadRequest(views.html.user.haves.index(form, userHaves(0)))
			,
			have => transaction {
				have.id = have_id
				Have.table.update(have)
				Ok("updated")
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

	def handleImages(have_id:Long) = IsAuthenticated(parse.multipartFormData) { implicit user => implicit request =>
		transaction {
//			val have_id = request.body.dataParts.get("have_id").map(_.head).get.toLong

			val have = Have.table.get(have_id)

			request.body.files map { f =>
				val file = f.ref.file
				val im = ItemImage.create(f.ref.file, f.contentType.get)
				ItemImage.table.insert(im)
				have.images.associate(im)

			}
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
