package models

import play.api.libs.functional.syntax._
import play.api.libs.json._


object JsonOps {

  implicit val formatsProblem: Format[Problem] = (
    (__ \ "status").format[Int] and
    (__ \ "title").format[String] and
    (__ \ "details").format[String]
  )(Problem, unlift(Problem.unapply))

}
