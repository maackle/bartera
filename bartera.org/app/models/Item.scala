package models

case class Item(
	what: String,
	description: String = "",
	user_id:Long = 0L
				 ) extends IdPK {

}

object Item extends MetaModel[Item] {
	val table = DB.items
}
