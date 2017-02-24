package services

import java.io.File
import java.net.{HttpURLConnection, URL}
import java.util

import scala.util.{Failure, Success, Try}

import com.google.inject.ImplementedBy
import javax.inject.Inject
import javazoom.jlgui.basicplayer.{BasicController, BasicPlayer, BasicPlayerEvent, BasicPlayerListener}

import com.typesafe.scalalogging.LazyLogging

import models.{AudioMetadata, PlayerStatus, Playlist}
import models.enums.PlayerStateEnum
import models.enums.PlayerStateEnum.PlayerState


@ImplementedBy(classOf[AudioServiceImpl])
trait AudioService {

  def addToPlaylist(audioPath: String): Try[Unit]

  def play(playListIndex: Int): Try[PlayerStatus]

  def playPause(): Try[PlayerStatus]

  def stop(): Try[PlayerStatus]

  def playerStatus(): PlayerStatus

  def playlist(): Playlist

}


class AudioServiceImpl @Inject()(audioPlayer : BasicPlayer) extends AudioService with LazyLogging {

  var _playlist: Seq[URL] = Seq()
  var currentPlayListIndex: Int = -1
  var currentSongLengthSeconds: Long = -1L
  var currentSongElapsedSeconds: Long = -1L

  audioPlayer.addBasicPlayerListener(new BasicPlayerListener {
    override def progress(bytesread: Int, microseconds: Long, pcmdata: Array[Byte], properties: util.Map[_, _]): Unit = {
      currentSongElapsedSeconds = (microseconds / scala.math.pow(10, 6)).toLong
    }

    override def setController(controller: BasicController): Unit = {}

    override def stateUpdated(event: BasicPlayerEvent): Unit = {}

    override def opened(stream: scala.Any, properties: util.Map[_, _]): Unit = {
      import scala.collection.JavaConversions._

      val currentUrlPath = _playlist(currentPlayListIndex).toString

      val audioLength: Long = if (isLocalFileUrl(currentUrlPath)) {
        val audioFile: File = new File(_playlist(currentPlayListIndex).getFile)
        audioFile.length
      } else {
        logger.warn("Extraction of file length for URL streams currently not supported")
        -1 // TODO: implement file length for url streams
      }

      val frameSize: Int = Try(properties.get("mp3.framesize.bytes").asInstanceOf[Int]).getOrElse(0)
      val frameRate: Float = Try(properties.get("mp3.framerate.fps").asInstanceOf[Float]).getOrElse(0)

      val audioLengthSeconds =
        if (frameSize > 0 && frameRate > 0) audioLength / (frameSize * frameRate)
        else -1

      logger.info(s"Audio '$currentUrlPath' frame size: $frameSize, frame rate: $frameRate, length: $audioLengthSeconds")
      currentSongLengthSeconds = audioLengthSeconds.toLong
      currentSongElapsedSeconds = 0L
    }
  })

  override def addToPlaylist(audioPath: String): Try[Unit] = {
    val audioUrlTry = Try {
      if (isUrl(audioPath)) new URL(audioPath)
      else new URL(s"file:///$audioPath")
    }

    logger.info(s"Adding $audioPath to playlist")
    audioUrlTry.map(url => _playlist = _playlist :+ url)
  }

  override def play(playlistIndex: Int): Try[PlayerStatus] = Try {
    if (playlistIndex < 0 || playlistIndex > _playlist.length - 1)
      throw new IndexOutOfBoundsException(s"No playlist element found with index: $playlistIndex")

    logger.info(s"Playing playlist element $playlistIndex")
    currentPlayListIndex = playlistIndex
    audioPlayer.open(_playlist(playlistIndex))
    audioPlayer.play()
    playerStatus()
  }

  override def playPause(): Try[PlayerStatus] = Try {
    import BasicPlayer._

    audioPlayer.getStatus match {
      case PLAYING =>
        logger.info("Pausing playlist")
        audioPlayer.pause()
      case PAUSED =>
        logger.info("Resuming playlist")
        audioPlayer.resume()
      case _ => throw NoAudioPlayingOrPaused("No audio is currently playing or paused")
    }

    playerStatus()
  }

  override def stop(): Try[PlayerStatus] = Try {
    logger.info("Stopping player audio")
    audioPlayer.stop()
    currentPlayListIndex = -1
    currentSongElapsedSeconds = 0
    currentSongLengthSeconds = 0

    playerStatus()
  }

  override def playerStatus(): PlayerStatus = PlayerStatus(playerState, playlist())

  override def playlist(): Playlist = Playlist(
    current = if (currentPlayListIndex > -1) Some(currentPlayListIndex) else None,
    songs = _playlist.zipWithIndex.map { case (url: URL, idx: Int) =>
      AudioMetadata(
        idx,
        urlTitle(url),
        None,
        if (idx == currentPlayListIndex) Some(currentSongLengthSeconds) else None,
        if (idx == currentPlayListIndex) Some(currentSongElapsedSeconds) else None,
        None
      )
    }
  )

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

  private def isLocalFileUrl(str: String): Boolean = str.startsWith("file:///")

  private def urlTitle(url: URL) = url.getPath.split("/").last
}

case class NoAudioPlayingOrPaused(message: String) extends Exception(message)