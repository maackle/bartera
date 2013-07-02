package models

import play.api.Play.current

import SQ._
case class Have(
						what: String,
						description: String,
						user_id:Long
						) extends ItemBase {
	def this() = this("", "", 0L)

	lazy val images = Schema.haveImages.left(this)

	def meta = Have

	lazy val detailURL = controllers.routes.Haves.detail(id)
}

object Have extends ItemBaseMeta[Have] {
	val table = Schema.haves

	val nameSingle = "have"
	val namePlural = "haves"
}

