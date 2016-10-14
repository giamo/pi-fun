package services

import java.io.{FileInputStream, InputStream}

import scala.util.Try

import com.google.inject.{ImplementedBy, Singleton}
import sun.audio.{AudioPlayer, AudioStream}

@ImplementedBy(classOf[AudioServiceImpl])
trait AudioService {

  def playLocalFile(filePath: String): Try[Unit]

}


class AudioServiceImpl() extends AudioService {

  val player: AudioPlayer = AudioPlayer.player

  override def playLocalFile(filePath: String): Try[Unit] = {
    Try {
      val is: InputStream = new FileInputStream(filePath)
      val audioStream: AudioStream = new AudioStream(is)

      player.start(audioStream)
    }
  }

}
