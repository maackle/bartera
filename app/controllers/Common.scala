package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.templates.Html
import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.mvc.SimpleResult
import play.api.libs.json.JsNumber

trait Common extends Controller with app.Common {

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

	implicit object AjaxResultFormat extends Format[AjaxResult] {
		def reads(json:JsValue) = {
			???
//			val code = (json \ "code").as[Int]
//			code match {
//				case AjaxResult.Code.SUCCESS =>
//					AjaxSuccess( (json \ "data").as[JsValue] )
//				case AjaxResult.Code.ERROR =>
//					Error( (json \ "message").as[String] )
//			}
		}

		def writes(r:AjaxResult):JsValue = JsObject{
			r match {
				case s:AjaxSuccess =>
					List(
						"code" -> JsNumber(s.code.id),
						"data" -> s.data
					)
				case e:AjaxError =>
					List(
						"code" -> JsNumber(e.code.id),
						"message" -> JsString(e.message)
					)
			}
		}
	}

	trait AjaxResult {
		val code:AjaxResult.Code.Value
		implicit def toJson = Json.toJson(this)
		implicit def toResult:Result
	}

	object AjaxResult {
		object Code extends Enumeration {
			val SUCCESS = Value(200)
			val ERROR = Value(400)
		}

	}


	case class AjaxSuccess(data: JsValue = JsNull) extends AjaxResult {
		val code = AjaxResult.Code.SUCCESS
		implicit def toResult = Ok(this.toJson)
	}
	case class AjaxError(message: String) extends AjaxResult {
		val code = AjaxResult.Code.ERROR
		implicit def toResult = BadRequest(this.toJson)
	}

	def jsonSuccess(obj: JsValue) = {
		Ok(Json.toJson(
			JsObject(Seq(
				"status" -> JsString("success"),
				"data" -> obj
			))
		))
	}
}
