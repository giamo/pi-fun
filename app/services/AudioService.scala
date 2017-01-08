package services

import java.net.URL

import scala.util.{Failure, Success, Try}

import com.google.inject.ImplementedBy
import javax.inject.Inject
import javazoom.jlgui.basicplayer.BasicPlayer

import com.typesafe.scalalogging.LazyLogging

import models.PlayerStatus
import models.enums.PlayerStateEnum
import models.enums.PlayerStateEnum.PlayerState


@ImplementedBy(classOf[AudioServiceImpl])
trait AudioService {

  def playLocalAudio(filePath: String): Try[Unit]

  def playNetworkAudio(url: String): Try[Unit]

  def pauseResume(): Try[Unit]

  def getPlayerStatus(): Try[PlayerStatus]

}


class AudioServiceImpl @Inject()(audioPlayer : BasicPlayer) extends AudioService with LazyLogging {

  override def playLocalAudio(filePath: String): Try[Unit] = Try {
    audioPlayer.open(new URL("file:///" + filePath))
    audioPlayer.play()
  }

  override def playNetworkAudio(url: String): Try[Unit] = Try {
    audioPlayer.open(new URL(url))
    audioPlayer.play()
  }

  override def pauseResume(): Try[Unit] = Try {
    import BasicPlayer._

    audioPlayer.getStatus match {
      case PLAYING => audioPlayer.pause()
      case PAUSED => audioPlayer.resume()
      case _ => throw NoAudioPlayingOrPaused("No audio is currently playing or paused")
    }
  }

  override def getPlayerStatus(): Try[PlayerStatus] = {
    // TODO: return metadata information
    playerState.map(ps => PlayerStatus(ps, None))
  }

  private def playerState: Try[PlayerState] = {
    import BasicPlayer._

    audioPlayer.getStatus match {
      case PLAYING => Success(PlayerStateEnum.PLAYING)
      case PAUSED => Success(PlayerStateEnum.PAUSED)
      case UNKNOWN | STOPPED | OPENED | SEEKING => Success(PlayerStateEnum.IDLE)
      case other =>
        val message = s"Unrecognised player state enum value: $other"
        logger.error(message)
        Failure(new Exception(message))
    }
  }

}

case class NoAudioPlayingOrPaused(message: String) extends Exception(message)