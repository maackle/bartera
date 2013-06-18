package models

case class Want(
						what: String,
						description: String = "",
						user_id:Long = 0L
						) extends ItemBase {
	def this() = this("", "", 0L)

	lazy val images = Schema.wantImages.left(this)

}

object Want extends MetaModel[Have] {
	val table = Schema.haves
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
