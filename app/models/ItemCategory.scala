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

	lazy val tree = transaction {

		def makeNode(parent:ItemCategory):Node = inTransaction {
			val kids = table.where(_.parent_id === parent.id).toSet.map(makeNode)
			val d = new Node(
				parent.name,
				parent,
				kids,
				kids ++ kids.flatMap(_.descendants)
			)
			d
		}

		makeNode(Root)
	}

	class Node(val name:String, val model:ItemCategory, val children:Set[Node], val descendants:Set[Node]) {

		def toHtmlSelect = {

		}
	}


}
