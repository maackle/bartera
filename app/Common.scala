package app

import play.api.data.Form
import play.api.mvc.{AnyContent, Request, SimpleResult}
import play.api.templates.Html
import grizzled.slf4j.Logging


trait Common
	extends Logging
	with org.squeryl.PrimitiveTypeMode {

	def msgInfo(text:String*) = "msg-info" -> text.mkString("\n")
	def msgError(text:String*) = "msg-error" -> text.mkString("\n")

	def app = play.api.Play.current

	//  implicit def injectContext(request:Request[AnyContent]) = Context(request)
	//  implicit def deriveRequest(ctx:Context[AnyContent]) = ctx.request
}

