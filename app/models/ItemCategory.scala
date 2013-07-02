package models

case class ItemCategory(name:String, parent_id:Option[Long]) extends Model[AnyRef] with IdPK {

	def this() = this("", None)

	def meta = ItemCategory
}

object ItemCategory extends MetaModel[ItemCategory] {
	val table = Schema.itemCategories
}
