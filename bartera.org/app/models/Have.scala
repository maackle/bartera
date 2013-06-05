package models

case class Have(
						what: String,
						description: String = "",
						user_id:Long = 0L
						) extends ItemBase {
	def this() = this("", "", 0L)

}

object Have extends MetaModel[Have] {
	val table = DB.haves
}
