package services

import java.net.URL
import scala.util.Try

import com.google.inject.ImplementedBy
import javax.inject.Inject
import javazoom.jlgui.basicplayer.BasicPlayer


@ImplementedBy(classOf[AudioServiceImpl])
trait AudioService {

  def playLocalAudio(filePath: String): Try[Unit]

  def pauseResume(): Try[Unit]

}


class AudioServiceImpl @Inject()(audioPlayer : BasicPlayer) extends AudioService {

  override def playLocalAudio(filePath: String): Try[Unit] = Try {
    audioPlayer.open(new URL("file:///" + filePath))
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

}

case class NoAudioPlayingOrPaused(message: String) extends Exception(message)