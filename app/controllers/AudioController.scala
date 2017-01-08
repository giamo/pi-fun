package controllers

import java.io.FileNotFoundException
import javax.inject.Inject

import scala.util.{Failure, Success}

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}

import controllers.results.Problems.{AudioConflict, PathNotFound, ServerError}
import models.JsonOps._
import services.{AudioService, NoAudioPlayingOrPaused}


class AudioController @Inject()(audioService: AudioService) extends Controller with LazyLogging {

  def play(filePath: String) = Action { implicit request =>
    audioService.playLocalAudio(filePath) match {
      case Success(_) => Ok(s"Playing audio file: $filePath")
      case Failure(_: FileNotFoundException) =>
        PathNotFound(s"The provided local audio file does not exist: $filePath")
      case Failure(ex) =>
        val message = s"Unexpected error while playing local audio file: $filePath"
        logger.error(message, ex)
        ServerError(message)
    }
  }

  def playURL(url: String) = Action { implicit request =>
    audioService.playNetworkAudio(url) match {
      case Success(_) => Ok(s"Playing audio URL: $url")
      case Failure(_: FileNotFoundException) =>
        PathNotFound(s"The provided URL audio does not exist: $url")
      case Failure(ex) =>
        val message = s"Unexpected error while playing URL audio: $url"
        logger.error(message, ex)
        ServerError(message)
    }
  }

  def pauseResume(): Action[AnyContent] = Action { implicit request =>
    audioService.pauseResume() match {
      case Success(_) => Ok(s"Successfully resumed/paused audio")
      case Failure(aex: NoAudioPlayingOrPaused) => AudioConflict(aex.message)
      case Failure(ex) =>
        val message = "Unexpected error while resuming/pausing audio"
        logger.error(message, ex)
        ServerError(message)
    }
  }

  def playerStatus(): Action[AnyContent] = Action { implicit request =>
    audioService.getPlayerStatus() match {
      case Success(status) => Ok(Json.toJson(status))
      case Failure(ex) =>
        val message = "Unexpected error while getting player status"
        logger.error(message, ex)
        ServerError(message)
    }
  }

}
