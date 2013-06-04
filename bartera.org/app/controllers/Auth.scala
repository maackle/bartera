package controllers

import play.api.mvc.{Action, Security, Flash, Controller}
import play.api.data.Form
import play.api.data.Forms._
import models.User

object Auth extends Common {

	def formError(msg:String) = Flash(
		Map("form-error" -> msg)
	)

	val loginForm = Form(
		tuple(
			"email" -> email,
			"password" -> text
		) verifying ("Invalid email or password", result => result match {
			case (email, password) => User.authenticate(email, password).isDefined
		})
	)

	val signupForm = Form(
		tuple(
			"email" -> email,
			"password" -> nonEmptyText(6, 32),
			"password_confirm" -> nonEmptyText(6, 32)
		) verifying ("Passwords must match", result => result match {
			case (_, password, password_confirm) => password == password_confirm
		}) verifying ("Email is taken", result => result match {
			case (email, _, _) => User.lookupByName(email).isEmpty
		})
	)

	def login = Action { implicit request =>
		Ok(views.html.auth.login(loginForm))
	}

	def signup = Action { implicit request =>
		Ok(views.html.auth.signup(signupForm))
	}

	def authenticate = Action { implicit request =>
		loginForm.bindFromRequest.fold (
			bad => { BadRequest(views.html.auth.login(bad)) },
			user => {
				val (username, _) = user
				Redirect(routes.Application.index)
					.withSession(Security.username -> username)
					.flashing(msgInfo("You're logged in!"))
			}
		)
	}

	def deauthenticate = Action { implicit request =>
		Redirect(routes.Application.index).withNewSession.flashing(msgInfo("You are now logged out"))
	}

	def register = Action { implicit request =>
		signupForm.bindFromRequest.fold (
			bad => { BadRequest(views.html.auth.signup(bad)) },
			user => {
				User.register(User(user._1, user._2))
				Redirect(routes.Auth.login)
					.flashing(msgInfo("You're registered!  Now log in."))
			}
		)
	}
}
