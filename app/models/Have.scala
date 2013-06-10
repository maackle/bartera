package models

import java.io.{ByteArrayOutputStream, FileInputStream, File}
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import javax.imageio.stream.ImageOutputStream

case class Have(
						what: String,
						description: String = "",
						user_id:Long = 0L
						) extends ItemBase {
	def this() = this("", "", 0L)

	lazy val images = DB.haveImages.left(this)

}

object Have extends MetaModel[Have] {
	val table = DB.haves
}


trait DBImage extends IdPK {
	def image:Array[Byte]
	def thumb:Array[Byte]
	def contentType:String
}

case class ItemImage(val image:Array[Byte], val thumb:Array[Byte], val contentType:String) extends DBImage {

}

object ItemImage extends MetaModel[ItemImage] {
	val table = DB.itemImages

	def file2byteArray(file:File) = {
		val bytes = new Array[Byte](file.length().toInt)
		val fis = new FileInputStream(file)
		fis.read(bytes)
		bytes
	}

	def resize(im:BufferedImage, width:Int, height:Int) = {
		val resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
		val g = resizedImage.createGraphics()
		g.drawImage(im, 0, 0, width, height, null)
		g.dispose()
		resizedImage
	}

	def image2bytes(im:BufferedImage) = {
		val baos = new ByteArrayOutputStream()
		ImageIO.write(im, "jpg", baos)
		baos.flush()
		baos.toByteArray()
	}

	def create(file:File, contentType:String) = {

		val bytes = file2byteArray(file)
		val fis = new FileInputStream(file)
		val im = ImageIO.read(fis)
		val large = image2bytes( resize(im, 500, 500))
		val thumb = image2bytes( resize(im, 100, 100) )

		val ii = new ItemImage(large, thumb, contentType)
		ItemImage.table.insert(ii)
	}

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
