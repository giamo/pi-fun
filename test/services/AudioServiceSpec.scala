package services

import java.io.FileNotFoundException
import java.net.URL
import javazoom.jlgui.basicplayer.BasicPlayer

import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalatestplus.play.PlaySpec


class AudioServiceSpec extends PlaySpec with MockitoSugar {

  val mockAudioPlayer = mock[BasicPlayer]
  val service = new AudioServiceImpl(mockAudioPlayer)

  "The audio service" should {

    import BasicPlayer._

    "open and play the input audio file" in {
      val inputFile = "/path/to/audio/file.mp3"
      val ans = service.playLocalAudio(inputFile)

      ans.isSuccess mustBe true
      verify(mockAudioPlayer, times(1)).open(new URL(s"file:///$inputFile"))
      verify(mockAudioPlayer, times(1)).play()
    }

    "pause the audio if the player is currently playing" in {
      when(mockAudioPlayer.getStatus) thenReturn PLAYING
      val ans = service.pauseResume()

      ans.isSuccess mustBe true
      verify(mockAudioPlayer, times(1)).pause()
    }

    "resume the audio if the player is currently paused" in {
      when(mockAudioPlayer.getStatus) thenReturn PAUSED
      val ans = service.pauseResume()

      ans.isSuccess mustBe true
      verify(mockAudioPlayer, times(1)).resume()
    }

    "throw a NoAudioPlayingOrPaused exception when playing/resuming if the player is in wrong state" in {
      when(mockAudioPlayer.getStatus) thenReturn STOPPED
      val ans = service.pauseResume()

      ans.isSuccess mustBe false
      ans.failed.get mustBe a[NoAudioPlayingOrPaused]
    }
  }
}
