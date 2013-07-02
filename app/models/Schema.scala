package models

import org.squeryl._
import org.squeryl.dsl._

import org.squeryl.dsl.CompositeKey2

object SQ extends PrimitiveTypeMode

import SQ._

object Schema extends org.squeryl.Schema {

	val users = table[models.User]
	val userProfiles = table[models.Profile]
	val haves = table[models.Have]
	val wants = table[models.Want]

	val zipcodes = table[models.Zipcode]

	val itemImages = table[models.ItemImage]
	val itemCategories = table[models.ItemCategory]

	val userHaves = oneToManyRelation(users, haves).via((u, i) => u.id === i.user_id)
	val userWants = oneToManyRelation(users, wants).via((u, i) => u.id === i.user_id)

	trait ItemImageRelation extends KeyedEntity[CompositeKey2[Long,Long]]
	class HaveImage(val have_id:Long, val image_id:Long) extends ItemImageRelation { def id = compositeKey(have_id, image_id) }
	class WantImage(val want_id:Long, val image_id:Long) extends ItemImageRelation { def id = compositeKey(want_id, image_id) }

//	val haveHaveImages = oneToManyRelation(haves, haveImages).via((h, i) => h.id === i.have_id)
//	val wantWantImages = oneToManyRelation(wants, wantImages).via((w, i) => w.id === i.want_id)
	val haveImages = manyToManyRelation(haves, itemImages).via[HaveImage]((h, i, hi) => (h.id === hi.have_id, hi.image_id === i.id))
	val wantImages = manyToManyRelation(wants, itemImages).via[WantImage]((w, i, wi) => (w.id === wi.want_id, wi.image_id === i.id))

	on(haves)(t => declare(
		(t.description) is (dbType("text"))
	))

//	def q(query: String, args: Any*) = new RawTupleQuery(query, args)
}

trait Model[+A] extends core.Common {
	def meta:MetaModel[_ <: A]
}

trait IdPK extends KeyedEntity[Long] {
	var id:Long = 0

}


trait MetaModel[T] {
	val table:Table[T]

//	def getOrRedirect(id:Long, call:Call)(fn: T => SimpleResult) = {
//		table.lookup(id).map { obj =>
//			fn(obj)
//		}
//		.getOrElse{ Redirect(call) }
//	}

}
