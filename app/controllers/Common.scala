package controllers

import play.api.data.Form
import play.api.mvc.{AnyContent, Request, SimpleResult}
import play.api.templates.Html
import grizzled.slf4j.Logging


trait Common
	extends play.api.mvc.Controller
	with org.squeryl.PrimitiveTypeMode
	with Logging {

	def msgInfo(text:String*) = "msg-info" -> text.mkString("\n")
	def msgError(text:String*) = "msg-error" -> text.mkString("\n")

	def submission[A, B](form: Form[A])(
		redirect:SimpleResult[B],
		origin: Form[A] => Html
		)(onSuccess: A=>String)(implicit request:Request[AnyContent]) = {

		form.bindFromRequest.fold(
			bad => { BadRequest(origin(bad)) },
			tup => transaction {
				val successMessage = onSuccess(tup)
				redirect.flashing(msgInfo(successMessage))
			}
		)
	}

	//  implicit def injectContext(request:Request[AnyContent]) = Context(request)
	//  implicit def deriveRequest(ctx:Context[AnyContent]) = ctx.request
}

