package models

import models.enums.PlayerStateEnum.PlayerState

case class Problem(status: Int, title: String, detail: String)

case class PlayerStatus(
    state: PlayerState, // current player state
    metadata: Option[AudioMetadata] // optional information about current audio played
)

case class AudioMetadata(
    title: String,
    artist: Option[String],
    length: Option[Int],
    cover: Option[String]
)