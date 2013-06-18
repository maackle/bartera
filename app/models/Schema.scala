package models

import org.squeryl._
import org.squeryl.dsl._

import org.squeryl.dsl.CompositeKey2
import play.api.mvc.{SimpleResult, Call}
import scala.collection.mutable.ArrayBuffer

object Schema extends org.squeryl.Schema with PrimitiveTypeMode {

	val users = table[models.User]
	val haves = table[models.Have]
	val wants = table[models.Want]

	val zipcodes = table[models.Zipcode]

	val itemImages = table[models.ItemImage]

	val userHaves = oneToManyRelation(users, haves).via((u, i) => u.id === i.user_id)
	val userWants = oneToManyRelation(users, wants).via((u, i) => u.id === i.user_id)

	class HaveImage(val have_id:Long, val image_id:Long) extends KeyedEntity[CompositeKey2[Long,Long]] { def id = compositeKey(have_id, image_id) }
	class WantImage(val want_id:Long, val image_id:Long) extends KeyedEntity[CompositeKey2[Long,Long]] { def id = compositeKey(want_id, image_id) }

//	val haveHaveImages = oneToManyRelation(haves, haveImages).via((h, i) => h.id === i.have_id)
//	val wantWantImages = oneToManyRelation(wants, wantImages).via((w, i) => w.id === i.want_id)
	val haveImages = manyToManyRelation(haves, itemImages).via[HaveImage]((h, i, hi) => (h.id === hi.have_id, hi.image_id === i.id))
	val wantImages = manyToManyRelation(wants, itemImages).via[WantImage]((w, i, wi) => (w.id === wi.want_id, wi.image_id === i.id))

	on(haves)(t => declare(
		(t.description) is (dbType("text"))
	))

//	def q(query: String, args: Any*) = new RawTupleQuery(query, args)
}


trait IdPK extends KeyedEntity[Long] {
	var id:Long = 0

}


trait MetaModel[T] extends PrimitiveTypeMode {
	val table:Table[T]

//	def getOrRedirect(id:Long, call:Call)(fn: T => SimpleResult) = {
//		table.lookup(id).map { obj =>
//			fn(obj)
//		}
//		.getOrElse{ Redirect(call) }
//	}

}