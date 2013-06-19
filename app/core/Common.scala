package core

import play.api.data.Form
import play.api.mvc.{AnyContent, Request, SimpleResult}
import play.api.templates.Html
import grizzled.slf4j.Logging
import java.awt.Color


trait Common
	extends Logging  {

	def msgInfo(text:String*) = "msg-info" -> text.mkString("\n")
	def msgError(text:String*) = "msg-error" -> text.mkString("\n")
	def msgSuccess(text:String*) = "msg-success" -> text.mkString("\n")

	def flashConsole(text:String*) = "debug-flash-console" -> text.mkString("<li>", "</li><li>", "</li>")

	implicit def app = play.api.Play.current

	def noImageURL(size:Int) = {
		val num = 6
		val brights = for(i <- 0 until num) yield "%x".format(Color.getHSBColor(i.toFloat / num, 1f, 1f).getRGB)
		val bgColor = "cccccc"
		val fgColor = brights((math.random*num).toInt)
		s"http://placehold.it/${size}x${size}/$bgColor/$fgColor&text=N/A"
	}

	def zipString(zip:Int) = {
		"%05s".format(zip)
	}

	//  implicit def injectContext(request:Request[AnyContent]) = Context(request)
	//  implicit def deriveRequest(ctx:Context[AnyContent]) = ctx.request
}

object Common extends Common
