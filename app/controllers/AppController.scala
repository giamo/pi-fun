package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class AppController @Inject() extends Controller {

  def main(any: String) = Action {
    Ok(views.html.main())
  }

  def index() = Action {
    Ok(views.html.index())
  }

  def music() = Action {
    Ok(views.html.music())
  }

}
