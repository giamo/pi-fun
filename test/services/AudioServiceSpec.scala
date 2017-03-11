package services

import java.io.FileNotFoundException
import java.net.URL
import javazoom.jlgui.basicplayer.BasicPlayer

import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalatestplus.play.PlaySpec

import models.enums.PlayerStateEnum
import models.Playlist


class AudioServiceSpec extends PlaySpec with MockitoSugar {

  val aInputFile = "/path/to/audio/file.mp3"
  val aInputUrl = "http://musicwebsite.com/anotherfile.mp3"

  "The audio service" should {

    import BasicPlayer._

    "add an audio to the playlist" in {
      val mockAudioPlayer = mock[BasicPlayer]
      val service = new AudioServiceImpl(mockAudioPlayer)
      service.playlist() mustBe Playlist(None, Seq())

      service.addToPlaylist(aInputFile)
      service._playlist mustBe Seq(new URL(s"file:///$aInputFile"))

      service.addToPlaylist(aInputUrl)
      service._playlist mustBe Seq(new URL(s"file:///$aInputFile"), new URL(aInputUrl))
    }

    "open and play the given playlist element" in {
      val mockAudioPlayer = mock[BasicPlayer]
      val service = new AudioServiceImpl(mockAudioPlayer)

      service.addToPlaylist(aInputFile)
      val ans = service.play(0)

      ans.isSuccess mustBe true
      ans.get.state mustBe PlayerStateEnum.PLAYING

      verify(mockAudioPlayer, times(1)).open(new URL(s"file:///$aInputFile"))
      verify(mockAudioPlayer, times(1)).play()
    }

    "pause the audio if the player is currently playing" in {
      val mockAudioPlayer = mock[BasicPlayer]
      val service = new AudioServiceImpl(mockAudioPlayer)

      when(mockAudioPlayer.getStatus) thenReturn PLAYING thenReturn PAUSED
      val ans = service.playPause()

      ans.isSuccess mustBe true
      ans.get.state mustBe PlayerStateEnum.PAUSED
      verify(mockAudioPlayer, times(1)).pause()
    }

    "resume the audio if the player is currently paused" in {
      val mockAudioPlayer = mock[BasicPlayer]
      val service = new AudioServiceImpl(mockAudioPlayer)

      when(mockAudioPlayer.getStatus) thenReturn PAUSED thenReturn PLAYING
      val ans = service.playPause()

      ans.isSuccess mustBe true
      ans.get.state mustBe PlayerStateEnum.PLAYING
      verify(mockAudioPlayer, times(1)).resume()
    }

    "throw a NoAudioPlayingOrPaused exception when playing/resuming if the player is in wrong state" in {
      val mockAudioPlayer = mock[BasicPlayer]
      val service = new AudioServiceImpl(mockAudioPlayer)

      when(mockAudioPlayer.getStatus) thenReturn STOPPED
      val ans = service.playPause()

      ans.isSuccess mustBe false
      ans.failed.get mustBe a[NoAudioPlayingOrPaused]
    }
  }
}
