package models

import play.api.mvc.{AnyContent, Request, Security}

case class User(
	email: String,
	passhash: String,
	userprofile_id: Long = 0L
				 ) extends IdPK {

//	this() = this("", "", 0L)

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

	def isAuthenticated(implicit request:Request[AnyContent]) = request.session.get(Security.username).flatMap(User.lookupByName(_))

	def authenticate(username: String, password: String):Option[User] = inTransaction {
		// TODO: use SHA1
		table.where( u => u.username === username and u.passhash === password ).headOption
	}

	def register(newUser:User) = inTransaction {
		table.insert(newUser)
	}
}
