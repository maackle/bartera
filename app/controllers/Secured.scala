package controllers

import play.api.mvc._
import models.User

trait Secured extends Controller with Common {

	private def username(request:RequestHeader) = request.session.get(Security.username)

	private def onUnauthorized(request: RequestHeader) = {
		Redirect(routes.Auth.login).flashing(msgError("Please log in"))
	}

	def IsAuthenticated(f: User => Request[_] => Result) = {
		Security.Authenticated(username, onUnauthorized) { username =>
			val user = User.getByName(username)
			Action(request => f(user)(request))
		}
	}

	def IsAuthenticated[A](p:BodyParser[A])(f: User => Request[A] => Result) = {
		Security.Authenticated(username, onUnauthorized) { username =>
			val user = User.getByName(username)
			Action(p)(request => f(user)(request))
		}
	}

	def IsAdmin(f: User => Request[_] => Result) = IsAuthenticated { user => ctx =>
		if(user.isAdmin)
			f(user)(ctx)
		else
			Forbidden
	}
}
