package models

import org.squeryl

case class User(
	email: String,
	passhash: String,
	userprofile_id: Long = 0L
				 ) extends IdPK {

	this() = this("", "", 0L)

	val username = email

	def isAdmin = false
}

object User extends MetaModel[User] {
	val table = DB.users

	def getByName(username:String) = inTransaction {
		table.where(_.username === username).head
	}

	def lookupByName(username:String) = inTransaction {
		table.where(_.username === username).headOption
	}

	def authenticate(email: String, password: String):Option[User] = inTransaction {
		???
	}

	def register(newUser:User) = inTransaction {
		table.insert(newUser)
	}
}
