package models

import play.api.mvc.{Request, Security}

import SQ._

case class User(
						email: String,
						passhash: String
						) extends IdPK {

//	def this() = this("", "")

	val username = email

	def profile = transaction { Profile.table.where(_.user_id === id).head }

	lazy val haves = Schema.userHaves.left(this)
	lazy val wants = Schema.userWants.left(this)

	def isAdmin = false

	def meta = User
}

object User extends MetaModel[User] {
	val table = Schema.users

	def getByName(username:String) = inTransaction {
		table.where(_.username === username).head
	}

	def lookupByName(username:String) = inTransaction {
		table.where(_.username === username).headOption
	}

	def currentUser(implicit request:Request[_]) = request.session.get(Security.username).flatMap(User.lookupByName(_))

	def authenticate(username: String, password: String):Option[User] = inTransaction {
		// TODO: use SHA1
		table.where( u => u.username === username and u.passhash === password ).headOption
	}

	def register(newUser:User) = transaction {
		val user = table.insert(newUser)
		Profile.table.insert(Profile.blank(user))
		user
	}
}
