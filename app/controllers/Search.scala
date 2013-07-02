package controllers

import play.api.mvc.Action

import play.api.data.Form
import play.api.data.Forms._
import models._
import SQ._
import anorm._
import play.api.db.DB
import org.squeryl.Table

object Search extends Common {

	val limitTemp = 20

	object Views {
		val haves = views.html.search.haves
		val wants = views.html.search.haves
		val results = views.html.search.results
	}

	object Forms {

		case class ItemQuery(q:String, cat_id:Option[Long], zipcode:String) {
			val location = Location.fromZipcode(zipcode)
		}

		val items = Form( mapping(
			"q" -> text,
			"cat_id" -> optional(longNumber),
			"zipcode" -> Fields.zipcode
		)(ItemQuery.apply)(ItemQuery.unapply))
	}

	case class SearchResult[+A <: ItemBase](distance_km:Double, item:A)

	def haves = Action { implicit request =>

		if(request.queryString.isEmpty) {
			Ok(Views.haves(Forms.items))
		}
		else {
			Forms.items.bindFromRequest.fold(
				form => {
					BadRequest(Views.haves(form))
				},
				query => {
					val results = lookupHavesWithDistance(query, limitTemp)
					Ok(Views.results(Forms.items.fill(query), results))
				}
			)
		}
	}

	def wants = Action { implicit request =>

		if(request.queryString.isEmpty) {
			Ok(Views.wants(Forms.items))
		}
		else {
			Forms.items.bindFromRequest.fold(
				form => {
					BadRequest(Views.wants(form))
				},
				query => {
					val results = lookupWantsWithDistance(query, limitTemp)
					Ok(Views.results(Forms.items.fill(query), results))
				}
			)
		}
	}

	private def lookupItemsWithDistance[A <: ItemBase](searchQuery:Forms.ItemQuery, limit:Int)(table:Table[A], fn:(String, String, Long)=>A):Seq[SearchResult[A]] = {
		val point = searchQuery.location.latlng
		val q = s"%${searchQuery.q.toLowerCase}%"
		DB.withConnection { implicit c =>
			val query = SQL(
				s"""
				  SELECT h.id, h.what, h.description, h.user_id, ST_Distance(l.latlng, ${point.geoString})/1000 as distance_km
				  FROM "${table.name}" h
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
					val item = fn(what, description, user_id)
					item.id = id
					SearchResult[A](distance_km, item)
				case row:Row =>
					println("got unexpected row: " + row)
					???
			}
			.toList
		}
	}

	private def lookupHavesWithDistance(searchQuery:Forms.ItemQuery, limit:Int):Seq[SearchResult[Have]] = {
		lookupItemsWithDistance(searchQuery, limit)(Have.table, Have.apply)
		//		val point = searchQuery.location.latlng
		//		val q = s"%${searchQuery.q.toLowerCase}%"
		//		DB.withConnection { implicit c =>
		//			val query = SQL(
		//				s"""
		//				  SELECT h.id, h.what, h.description, h.user_id, ST_Distance(l.latlng, ${point.geoString})/1000 as distance_km
		//				  FROM "${Have.table.name}" h
		//				  JOIN locations l ON l.id = h.location_id
		//				  WHERE LOWER(h.description) LIKE {q} OR LOWER(h.what) LIKE {q}
		//				  ORDER BY distance_km
		//				  LIMIT $limit
		//				""")
		//				.on(
		//					"q" -> q
		//				)
		//			query().map {
		//				case Row(id:Long, what:String, description:String, user_id:Long, distance_km:Double) =>
		//					val have = Have(what, description, user_id)
		//					have.id = id
		//					SearchResult[Have](distance_km, have)
		//				case row:Row =>
		//					println("got unexpected row: " + row)
		//					???
		//			}
		//			.toList
		//		}
	}

	private def lookupWantsWithDistance(searchQuery:Forms.ItemQuery, limit:Int):Seq[SearchResult[Want]] = {
		lookupItemsWithDistance(searchQuery, limit)(Want.table, Want.apply)
	}
}
