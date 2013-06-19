package models

import core.Common

trait Owned {
	def user_id:Long
//	def owner:User = transaction { User.table.get(user_id) }
}

trait ItemBase extends IdPK with Owned with Model[AnyRef] {

	def what: String
	def description: String
	var nascent: Boolean = true
}
