package services

import java.io.File
import java.net.{HttpURLConnection, URL}
import java.util

import scala.util.{Failure, Success, Try}

import com.google.inject.ImplementedBy
import javax.inject.Inject
import javazoom.jlgui.basicplayer.{BasicController, BasicPlayer, BasicPlayerEvent, BasicPlayerListener}

import com.typesafe.scalalogging.LazyLogging

import models.{AudioMetadata, PlayerStatus}
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

  var currentSongLengthSeconds: Long = -1L
  var currentSongElapsedSeconds: Long = -1L
  var currentSongPath = ""

  audioPlayer.addBasicPlayerListener(new BasicPlayerListener {
    override def progress(bytesread: Int, microseconds: Long, pcmdata: Array[Byte], properties: util.Map[_, _]): Unit = {
      currentSongElapsedSeconds = (microseconds / scala.math.pow(10, 6)).toLong
    }

    override def setController(controller: BasicController): Unit = {}

    override def stateUpdated(event: BasicPlayerEvent): Unit = {}

    override def opened(stream: scala.Any, properties: util.Map[_, _]): Unit = {
      import scala.collection.JavaConversions._

      val audioLength: Long = if (!isUrl(currentSongPath)) {
        val audioFile: File = new File(currentSongPath)
        audioFile.length
      } else {
        -1 // TODO: implement file length for url streams
      }

      val frameSize: Int = Try(properties.get("mp3.framesize.bytes").asInstanceOf[Int]).getOrElse(0)
      val frameRate: Float = Try(properties.get("mp3.framerate.fps").asInstanceOf[Float]).getOrElse(0)

      val audioLengthSeconds =
        if (frameSize > 0 && frameRate > 0) audioLength / (frameSize * frameRate)
        else -1

      logger.info(s"Audio '$currentSongPath' frame size: $frameSize, frame rate: $frameRate, length: $audioLengthSeconds")
      currentSongLengthSeconds = audioLengthSeconds.toLong
      currentSongElapsedSeconds = 0L
    }
  })

  override def playLocalAudio(filePath: String): Try[PlayerStatus] = Try {
    currentSongPath = filePath
    audioPlayer.open(new URL("file:///" + filePath))
    audioPlayer.play()
    playerStatus()
  }

  override def playNetworkAudio(url: String): Try[PlayerStatus] = Try {
    currentSongPath = url
    audioPlayer.open(new URL(url))
    audioPlayer.play()
    playerStatus()
  }

  override def playPause(): Try[PlayerStatus] = Try {
    import BasicPlayer._

    audioPlayer.getStatus match {
      case PLAYING => audioPlayer.pause()
      case PAUSED => audioPlayer.resume()
      case _ => throw NoAudioPlayingOrPaused("No audio is currently playing or paused")
    }

    playerStatus()
  }

  override def playerStatus(): PlayerStatus = {
    // TODO: return metadata information
    PlayerStatus(
      playerState,
      Some(AudioMetadata(
        "TODO: title",
        None,
        if (currentSongLengthSeconds > 0) Some(currentSongLengthSeconds) else None,
        if (currentSongElapsedSeconds > 0) Some(currentSongElapsedSeconds) else None,
        None
      ))
    )
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

  private def isUrl(str: String): Boolean = {
    val rHttp = """http[s]?://.*""".r
    val rFile = """file:///.*""".r
    val rAny = """.*://.*""".r

    str match {
      case rHttp(_) | rFile(_) | rAny(_) => true
      case _ => false
    }
  }
}

case class NoAudioPlayingOrPaused(message: String) extends Exception(message)