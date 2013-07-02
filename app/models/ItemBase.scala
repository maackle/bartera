package models

import core.Common
import models.SQ._
import play.api.mvc.Call
import org.squeryl.Query
import org.squeryl.dsl.ManyToMany

trait Owned {
	def user_id:Long
//	def owner:User = transaction { User.table.get(user_id) }
}

trait ItemBase extends IdPK with Owned with HasLocation with Model[AnyRef] {

	def what: String
	def description: String
	var nascent: Boolean = true

	def images:Query[ItemImage] with ManyToMany[ItemImage, _ <: Schema.ItemImageRelation]
	def imageObjects = transaction { images.toArray }
	lazy val thumbURLs = transaction { images.map(_.id).map(controllers.routes.Application.viewImage(_, ItemImage.thumbSize, ItemImage.thumbSize)) }

	def detailURL:Call
}

trait ItemBaseMeta[A <: ItemBase] extends MetaModel[A] {

	val nameSingle:String
	val namePlural:String
}
