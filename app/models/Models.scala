package models

import models.enums.PlayerStateEnum.PlayerState

case class Problem(status: Int, title: String, detail: String)

case class PlayerStatus(
    state: PlayerState, // current player state
    playlist: Playlist // current state of playlist
)

case class AudioMetadata(
    id: Int,
    title: String,
    artist: Option[String],
    length: Option[Long],
    elapsed: Option[Long],
    cover: Option[String]
)

case class Playlist(
    current: Option[Int], // index of currently playing audio
    songs: Seq[AudioMetadata]
)