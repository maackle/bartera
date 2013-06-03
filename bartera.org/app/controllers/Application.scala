package controllers

import app.DBEngine
import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
	  DBEngine.current(Play.current)
    Ok(views.html.index("Your new application is ready."))
  }
  
}
