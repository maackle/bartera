package models
import org.squeryl.KeyedEntity

case class Profile(
							user_id:Long,
							zipcode:String,
							name:Option[String]
							) extends KeyedEntity[Long] {

	//	@transient
	val id = user_id
}

object Profile extends MetaModel[Profile] {
	val table = Schema.userProfiles

	def blank(user:User) = {
		Profile(user.id, "00000", None)
	}
}
