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

  def playLocalAudio(filePath: String): Try[PlayerStatus]

  def playNetworkAudio(url: String): Try[PlayerStatus]

  def playPause(): Try[PlayerStatus]

  def playerStatus(): PlayerStatus

}


class AudioServiceImpl @Inject()(audioPlayer : BasicPlayer) extends AudioService with LazyLogging {

  override def playLocalAudio(filePath: String): Try[PlayerStatus] = Try {
    audioPlayer.open(new URL("file:///" + filePath))
    audioPlayer.play()
    playerStatus
  }

  override def playNetworkAudio(url: String): Try[PlayerStatus] = Try {
    audioPlayer.open(new URL(url))
    audioPlayer.play()
    playerStatus
  }

  override def playPause(): Try[PlayerStatus] = Try {
    import BasicPlayer._

    audioPlayer.getStatus match {
      case PLAYING => audioPlayer.pause()
      case PAUSED => audioPlayer.resume()
      case _ => throw NoAudioPlayingOrPaused("No audio is currently playing or paused")
    }

    playerStatus
  }

  override def playerStatus: PlayerStatus = {
    // TODO: return metadata information
    PlayerStatus(playerState, None)
  }

  private def playerState: PlayerState = {
    import BasicPlayer._

    audioPlayer.getStatus match {
      case PLAYING => PlayerStateEnum.PLAYING
      case PAUSED => PlayerStateEnum.PAUSED
      case UNKNOWN | STOPPED | OPENED | SEEKING => PlayerStateEnum.IDLE
      case other =>
        val message = s"Unrecognised player state enum value: $other"
        logger.error(message)
        throw new Exception(message)
    }
  }

}

case class NoAudioPlayingOrPaused(message: String) extends Exception(message)