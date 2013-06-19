package controllers

import play.api.mvc.Action

import play.api.data.Form
import play.api.data.Forms._
import models.{Location, Schema, SQ, Have}
import SQ._
import anorm._
import play.api.db.DB

object Search extends Common {

	object Views {
		val index = views.html.search.index
		val results = views.html.search.results
	}

	object Forms {

		case class HaveQuery(q:String, cat_id:Option[Long], zipcode:String) {
			val location = Location.fromZipcode(zipcode)
		}

		val haves = Form( mapping(
			"q" -> text,
			"cat_id" -> optional(longNumber),
			"zipcode" -> Fields.zipcode
		)(HaveQuery.apply)(HaveQuery.unapply))
	}

	case class SearchResultHave(distance_km:Double, have:Have)

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
					val results = lookupHavesWithDistance(query, limit)
					Ok(Views.results(Forms.haves.fill(query), results))
				}
			)
		}
	}

	private def lookupHavesWithDistance(searchQuery:Forms.HaveQuery, limit:Int):Seq[SearchResultHave] = {
		val point = searchQuery.location.latlng
		val q = s"%${searchQuery.q.toLowerCase}%"
		DB.withConnection { implicit c =>
			val query = SQL(
				s"""
				  SELECT h.id, h.what, h.description, h.user_id, ST_Distance(l.latlng, ${point.geoString})/1000 as distance_km
				  FROM "${Have.table.name}" h
				  JOIN locations l ON l.id = h.location_id
				  WHERE LOWER(h.description) LIKE {q} OR LOWER(h.what) LIKE {q}
				  ORDER BY distance_km
				  LIMIT $limit
				""")
				.on(
					"q" -> q
				)
			query().map {
				case Row(id:Long, what:String, description:String, user_id:Long, distance_km:Double) =>
					val have = Have(what, description, user_id)
					have.id = id
					SearchResultHave(distance_km, have)
				case row:Row =>
					println("got unexpected row: " + row)
					???
			}
			.toList
		}
	}
}
