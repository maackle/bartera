package models

import SQ._

case class Want(
						what: String,
						description: String = "",
						user_id:Long = 0L
						) extends ItemBase {
	def this() = this("", "", 0L)

	lazy val images = Schema.wantImages.left(this)

	def meta = Want

	lazy val detailURL = controllers.routes.Wants.detail(id)

	def withZipcode(zipcode:String) = {
		location_id = Location.fromZipcode(zipcode).id
		this
	}

}

object Want extends ItemBaseMeta[Want] {
	val table = Schema.wants

	val nameSingle = "want"
	val namePlural = "wants"
}

//
//case class WantImage(
//							  want_id: Long,
//							  image: Array[Byte],
//							  contentType: String
//							  ) extends DBImage {
//
//}
//
//object WantImage extends MetaModel[WantImage] {
//	val table = Schema.wantImages
//}
