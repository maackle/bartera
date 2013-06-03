package models

import org.squeryl

class User extends IdPK {

}

object User extends MetaModel[User] {
	val table = DB.users
}
