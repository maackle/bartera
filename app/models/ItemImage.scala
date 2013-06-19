package models

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, FileInputStream, File}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.{RenderingHints, Rectangle}

import play.api._
import play.api.mvc._
import play.api.Play._

trait DBImage extends IdPK {
	def master:Array[Byte]
	def contentType:String
}

trait ImageOps {

	def im:BufferedImage
	def width = im.getWidth
	def height = im.getHeight

	def crop(rect: Rectangle) = {
		im.getSubimage(rect.x, rect.y, rect.width, rect.height)
	}

	def cropAspect(aspect: Double) = {
		val ah = (im.getWidth / aspect).round.toInt
		val aw = (im.getHeight * aspect).round.toInt

		if (width > aw) {
			crop(new Rectangle((width-aw)/2, 0, aw, height))
		}
		else if (height > ah) {
			crop(new Rectangle(0, (height-ah)/2, width, ah))
		}
		else {
			im
		}
	}

	def resize(width:Int, height:Int) = {
		val resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
		val g = resizedImage.createGraphics()
		if (this.width < width || this.height < height) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
		}
		g.drawImage(im, 0, 0, width, height, null)
		g.dispose()
		resizedImage
	}

	def resizeProportionally(maxWidth:Int, maxHeight:Int) = {

		val (iw, ih) = (im.getWidth, im.getHeight)

		var width, height : Int = 0

		if (iw > ih && iw > maxWidth) {
			width = maxWidth
			height = width * ih / iw
		}
		else if(ih > iw && ih > maxHeight) {
			height = maxHeight
			width = height * iw / ih
		}
		else {
			val size = math.min(maxWidth, maxHeight)
			width = size
			height = size
		}

		resize(width, height)
	}

	def toBytes = {
		val baos = new ByteArrayOutputStream()
		ImageIO.write(im, "jpg", baos)
		baos.flush()
		baos.toByteArray()
	}
}

import ImageOps._

object ImageOps {
	implicit def bim2ops(bim:BufferedImage):ImageOps = {
		require(bim != null, throw new Exception("null image"))
		new ImageOps { val im = bim }
	}
}

case class ItemImage(val master:Array[Byte], val clipRect:Rectangle, val contentType:String) extends DBImage {

	lazy val image = {
		require(master != null, throw new Exception("null master image"))
		val bis = new ByteArrayInputStream(master)
		val bim = ImageIO.read(bis)
		require(bim != null, {println(bis); throw new Exception("null bim image")})
		bim
	}
	import ItemImage.thumbSize

	lazy val thumbURL = (controllers.routes.Application.viewImage(id, thumbSize, thumbSize))

	def url(width:Int, height:Int) = (controllers.routes.Application.viewImage(id, width, height))

	def serve(width:Int, height:Int) = {
		val aspect = width.toFloat / height.toFloat
		val resized = image.cropAspect(aspect).resizeProportionally(width, height)
		(resized.toBytes, contentType)
	}
//
//	def serve(width:Int, height:Int, crop:Option[Rectangle]) = {
//		val im = crop.map(image.crop(_)).getOrElse(image)
//		val resized = im.resizeProportionally(width, height)
//		(resized.toBytes, contentType)
//	}

}

object ItemImage extends MetaModel[ItemImage] {
	val table = Schema.itemImages

	lazy val thumbSize = current.configuration.getInt("haves.thumbnail_size").getOrElse(throw new Exception("must set haves.thumbnail_size"))

	def file2byteArray(file:File) = {
		val bytes = new Array[Byte](file.length().toInt)
		val fis = new FileInputStream(file)
		fis.read(bytes)
		bytes
	}
//
//	def create(bytes:Array[Byte], contentType:String) = {
//
//	}

	def create(file:File, contentType:String) = {

		val bytes = file2byteArray(file)
		val fis = new FileInputStream(file)
		val im = ImageIO.read(fis)
		val master = im.resizeProportionally(1000, 1000).toBytes

		val ii = new ItemImage(master, null, contentType)
		ItemImage.table.insert(ii)
	}

}
