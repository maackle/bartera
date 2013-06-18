package controllers

import play.api.mvc.Action

import play.api.data.Form
import play.api.data.Forms._
import models.{SQ, Have}
import SQ._

object Search extends Common {

	object Views {
		val index = views.html.search.index
		val results = views.html.search.results
	}

	object Forms {

		case class HaveQuery(q:String, cat_id:Option[Long], zipcode:String)

		val haves = Form( mapping(
			"q" -> text,
			"cat_id" -> optional(longNumber),
			"zipcode" -> Fields.zipcode
		)(HaveQuery.apply)(HaveQuery.unapply))
	}

	def haves = Action { implicit request =>
		val limit = 20

		if(request.queryString.isEmpty) {
			Ok(Views.index(Forms.haves))
		}
		else {
			Forms.haves.bindFromRequest.fold(
				form => {
					BadRequest(Views.index(form))
				},
				query => {
					val results = transaction {
						val q = s"%${query.q.toLowerCase}%"
						Have.table.where(have => (lower(have.what) like q) or (lower(have.description) like q)).take(20).toList
					}
					Ok(Views.results(Forms.haves.fill(query), results))
				}
			)
		}

	}
}
