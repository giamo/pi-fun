package controllers

import java.io.FileNotFoundException
import javax.inject.Inject

import scala.util.{Failure, Success, Try}

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}

import controllers.results.Problems.{AudioConflict, InvalidPlaylistIndex, PathNotFound, ServerError}
import models.JsonOps._
import services.{AudioService, NoAudioPlayingOrPaused}


class AudioController @Inject()(audioService: AudioService) extends Controller with LazyLogging {

  def play(playlistIndex: Int) = Action { implicit request =>
    audioService.play(playlistIndex) match {
      case Success(playerStatus) => Ok(Json.toJson(playerStatus))
      case Failure(ex: IndexOutOfBoundsException) => InvalidPlaylistIndex(ex.getMessage)
      case Failure(ex) =>
        val message = s"Eror while playing playlist element with index $playlistIndex"
        logger.error(message, ex)
        ServerError(message)
    }
  }
  def enqueue(audioPath: String) = Action { implicit request =>
    audioService.addToPlaylist(audioPath) match {
      case Success(_) => Ok(s"Added audio with path $audioPath to the playlist")
      case Failure(ex) =>
        val message = s"Eror while adding audio file to playlist: $audioPath"
        logger.error(message, ex)
        ServerError(message)
    }
  }

  def playPause(): Action[AnyContent] = Action { implicit request =>
    audioService.playPause() match {
      case Success(playerStatus) => Ok(Json.toJson(playerStatus))
      case Failure(aex: NoAudioPlayingOrPaused) => AudioConflict(aex.message)
      case Failure(ex) =>
        val message = "Unexpected error while resuming/pausing audio"
        logger.error(message, ex)
        ServerError(message)
    }
  }

  def stop(): Action[AnyContent] = Action { implicit request =>
    audioService.stop() match {
      case Success(playerStatus) => Ok(Json.toJson(playerStatus))
      case Failure(ex) =>
        val message = "Unexpected error while stopping audio"
        logger.error(message, ex)
        ServerError(message)
    }
  }

  def playerStatus(): Action[AnyContent] = Action { implicit request =>
    Try(audioService.playerStatus()) match {
      case Success(playerStatus) => Ok(Json.toJson(playerStatus))
      case Failure(ex) =>
        val message = "Unexpected error while getting player status"
        logger.error(message, ex)
        ServerError(message)
    }
  }

  def playlist(): Action[AnyContent] = Action { implicit request =>
    Ok(Json.toJson(audioService.playlist()))
  }

}
