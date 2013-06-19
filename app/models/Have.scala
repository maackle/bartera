package models

import play.api.Play.current

import SQ._
case class Have(
						what: String,
						description: String,
						user_id:Long
						) extends ItemBase with HasLocation {
	def this() = this("", "", 0L)

	lazy val images = Schema.haveImages.left(this)

	def imageObjects = transaction { images.toArray }

	lazy val thumbURLs = transaction { images.map(_.id).map(controllers.routes.Application.viewImage(_, current.configuration.getInt("haves.thumbnail_size").getOrElse(throw new Exception("must set haves.thumbnail_size")))) }

	def meta = Have
}

object Have extends MetaModel[Have] {
	val table = Schema.haves
}

