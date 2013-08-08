package models

import SQ._
import scala.xml.{NodeSeq, Elem}

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

		def makeNode(category:ItemCategory):Node = inTransaction {
			val kids = table.where(_.parent_id === category.id).toSet.map(makeNode)
			val d = new Node(
				category.name,
				category,
				kids,
				kids ++ kids.flatMap(_.descendants)
			)
			assert(!d.children.contains(d))
			d
		}

		makeNode(Root)
	}



	case class Node(val name:String, val model:ItemCategory, val children:Set[Node], val descendants:Set[Node]) {

		def toPairs = {

			def traverse(node:Node, prefix:String):Seq[(String, String)] = {
				val label = prefix + node.name
				Seq((node.model.id.toString, label)) ++ node.children.flatMap(traverse(_, label + " > "))
			}

			traverse(this, "").sortBy(_._2)
		}
//		def toHtml = {
//
//			def traverse(node:Node, prefix:String):Seq[NodeSeq] = {
//				val label = prefix + " > " + node.name
//				<option name={ node.model.id }>{ label }</option> ++ children.flatMap(traverse(_, label))
//			}
//
//			traverse(this, "")
//		}
	}


}
