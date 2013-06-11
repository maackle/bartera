package models

import java.io.{ByteArrayOutputStream, FileInputStream, File}
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import javax.imageio.stream.ImageOutputStream
import org.squeryl.PrimitiveTypeMode
import PrimitiveTypeMode._
import play.api.Play.current

case class Have(
						what: String,
						description: String = "",
						user_id:Long = 0L
						) extends ItemBase {
	def this() = this("", "", 0L)

	lazy val images = DB.haveImages.left(this)

	lazy val thumbURLs = transaction { images.map(_.id).map(controllers.routes.Application.viewImage(_, current.configuration.getInt("haves.thumbnail_size").getOrElse(throw new Exception("must set haves.thumbnail_size")))) }

}

object Have extends MetaModel[Have] {
	val table = DB.haves
}


//case class HaveImage(
//							  have_id: Long,
//							  image: Array[Byte],
//							  contentType: String
//							  ) extends DBImage {
//
//}
//
//object HaveImage extends MetaModel[HaveImage] {
//	val table = DB.haveImages
//}
