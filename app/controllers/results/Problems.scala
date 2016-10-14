package controllers.results

import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.mvc.Results.Status

import models.Problem
import models.JsonOps._


object Problems {

  case class ProblemJson(status: Int, title: String, detail: String) {

    val toProblem: Problem = Problem(status, title, detail)
    val toJson: JsValue = Json.toJson(toProblem)
    val toResult: Result = new Status(status)(toJson).as("application/json")
  }

  object PathNotFound {
    def apply(message: String): Result = ProblemJson(NOT_FOUND, "Not found", message).toResult
  }

  object ServerError {
    def apply(message: String): Result = ProblemJson(INTERNAL_SERVER_ERROR, "Internal server error", message).toResult
  }

}
