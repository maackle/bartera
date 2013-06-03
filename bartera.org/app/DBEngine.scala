package app

trait DBEngine {
	val driverName:String
}

object DBEngine {

	def current(implicit app:play.api.Application) = {
		val m = enabled.map(d => d.driverName -> d).toMap
		m(app.configuration.getString("db.default.driver").get)
	}

	val enabled = Set(H2, Postgres)

	case object H2 extends DBEngine {
		val driverName = "org.h2.Driver"
	}

	case object Postgres extends DBEngine {
		val driverName = "org.postgresql.Driver"
	}
}
