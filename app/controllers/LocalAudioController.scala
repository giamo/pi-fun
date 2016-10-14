package controllers

import java.io.FileNotFoundException
import javax.inject.Inject
import scala.util.{Failure, Success}

import com.typesafe.scalalogging.LazyLogging
import play.api.mvc.{Action, AnyContent, Controller}

import controllers.results.Problems.{PathNotFound, ServerError}
import services.AudioService


class LocalAudioController @Inject()(audioService: AudioService) extends Controller with LazyLogging {

  def play(audioPath: String): Action[AnyContent] = Action { implicit request =>
    audioService.playLocalFile(audioPath) match {
      case Success(_) => Ok(s"Playing audio file: $audioPath")
      case Failure(ex: FileNotFoundException) =>
        PathNotFound(s"The provided local audio file does not exist: $audioPath")
      case Failure(ex) =>
        logger.error(s"Failure while playing audio from local file $audioPath", ex)
        ServerError(s"Unexpected error while playing local audio file: $audioPath")
    }
  }
}
