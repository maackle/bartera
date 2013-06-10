package models

trait ItemBase extends IdPK {

	def what: String
	def description: String
	var nascent: Boolean = true
}
