package models

case class Item(
	user_id:Long
				 ) extends IdPK {

}

object Item extends MetaModel[Item] {
	val table = DB.items
}
