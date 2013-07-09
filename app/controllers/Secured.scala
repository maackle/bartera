package controllers

import play.api.mvc._
import models.{Location, Owned, User}

trait Secured extends Common {

	private def username(request:RequestHeader) = request.session.get(Security.username)
	private def zipcode(request:Request[_]) = Location.currentZipcode(request)

	private def onUnauthorized(request: RequestHeader) = {
		Redirect(routes.Auth.login).flashing(msgError("Please log in"))
	}

	private def onNoZipcode(request: RequestHeader) = {
		Redirect(routes.Application.editLocation).flashing(msgError("Please set your location before searching"))
	}

	private def onNotOwner(implicit request: RequestHeader) = {
		Redirect(routes.Auth.login).flashing(msgError("You aren't allowed to edit that"))
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

	def HasZipcode(f: String => Request[_] => Result) = {
		Action { implicit request =>
			zipcode(request).map { zipcode =>
				f(zipcode)(request)
			}
			.getOrElse {
				onNoZipcode(request)
			}
		}
	}

	def IsOwner(owned:Owned)(f: User => Request[_] => Result) = {
		Security.Authenticated(username, onUnauthorized) { username =>
			val user = User.getByName(username)
			Action { implicit request =>
				if (user.id != owned.user_id) onNotOwner
				else f(user)(request)
			}
		}
	}

	def IsOwner[A](p:BodyParser[A])(owned:Owned)(f: User => Request[_] => Result) = {
		Security.Authenticated(username, onUnauthorized) { username =>
			val user = User.getByName(username)
			Action(p) { implicit request =>
				if (user.id != owned.user_id) onNotOwner
				else f(user)(request)
			}
		}
	}

	def IsAdmin(f: User => Request[_] => Result) = IsAuthenticated { user => ctx =>
		if(user.isAdmin)
			f(user)(ctx)
		else
			Forbidden
	}
}
