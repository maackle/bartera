package models

import SQ._

case class ItemCategory(name:String, parent_id:Option[Long]) extends Model[AnyRef] with IdPK {

	def this() = this("", None)

	def meta = ItemCategory
}

object ItemCategory extends MetaModel[ItemCategory] {
	val table = Schema.itemCategories

	lazy val Root = transaction { table.where(_.parent_id.isNull).single }
	lazy val Goods = transaction { table.where(c => c.name === "goods" and c.parent_id === Root.id).single }
	lazy val Services = transaction { table.where(c => c.name === "services" and c.parent_id === Root.id).single }

	def detail = transaction {

		def getDetail(parent:ItemCategory):Detail = inTransaction {
			val kids = table.where(_.parent_id === parent.id).toSet.map(getDetail)
			val d = new Detail(
				parent.name,
				parent,
				kids,
				kids ++ kids.flatMap(_.descendants)
			)
			d
		}

		getDetail(Root)
	}

	class Detail(val name:String, val model:ItemCategory, val children:Set[Detail], val descendants:Set[Detail])


}
