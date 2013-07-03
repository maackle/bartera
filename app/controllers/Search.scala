package controllers

import play.api.mvc.{Request, Action}

import play.api.data.Form
import play.api.data.Forms._
import models._
import SQ._
import anorm._
import play.api.db.DB
import org.squeryl.Table
import controllers.Search.Forms.ItemQuery

object Search extends Common {

	val limitTemp = 20

	object Views {
		def items(name:String)(implicit request:Request[_]) = {
			if(name=="haves") views.html.search.haves(_, _)
			else if (name=="wants") views.html.search.wants(_, _)
			else ???
		}
		val results = views.html.search.results
	}

	object Forms {

		case class ItemQuery(q:String, cat_id:Option[Long], zipcode:String) {
			val location = Location.fromZipcode(zipcode)
		}

		object ItemQuery {
			def emptyWithZipcode(implicit request:Request[_]) = ItemQuery("", None, request.session.get("zipcode").getOrElse(""))
		}

		val items = Form( mapping(
			"q" -> text,
			"cat_id" -> optional(longNumber),
			"zipcode" -> Fields.zipcode
		)(ItemQuery.apply)(ItemQuery.unapply))

	}

	case class SearchResult[+A <: ItemBase](distance_km:Double, item:A)

	private def items[A <: ItemBase](lookup:(ItemQuery) => Seq[SearchResult[A]], itemType:String) = Action { implicit request =>
		val view = Views.items(itemType)
		if(request.queryString.isEmpty) {
			Ok(view(Forms.items, None))
		}
		else {
			val zipcode = request.session.get("zipcode").getOrElse("")
			val data = if(zipcode.isEmpty) {
				request.queryString
			} else {
				request.queryString + ("zipcode" -> Seq(zipcode))
			}
			Forms.items.bindFromRequest(data).fold(
				form => {
					BadRequest(view(form, None))
				},
				query => {
					val results = lookup(query)
					Ok(view(Forms.items.fill(query), Some(results)))
				}
			)
		}
	}

	def haves = items(lookupHavesWithDistance(_, limitTemp), "haves")
	def wants = items(lookupWantsWithDistance(_, limitTemp), "wants")

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
