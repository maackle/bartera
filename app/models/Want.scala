package models

case class Want(
						what: String,
						description: String = "",
						user_id:Long = 0L
						) extends ItemBase {
	def this() = this("", "", 0L)
}

object Want extends MetaModel[Have] {
	val table = DB.haves
}
