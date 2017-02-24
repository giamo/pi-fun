package models

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

import models.enums.{EnumError, NamedEnum, PlayerStateEnum}
import models.enums.PlayerStateEnum.PlayerState


object JsonOps {

  implicit val formatsPlayerStateEnum = createEnumFormat(PlayerStateEnum.apply)

  implicit val formatsProblem: Format[Problem] = (
    (__ \ "status").format[Int] and
    (__ \ "title").format[String] and
    (__ \ "details").format[String]
  )(Problem, unlift(Problem.unapply))

  implicit val formatsAudioMetadata: Format[AudioMetadata] = (
    (__ \ "id").format[Int] and
    (__ \ "title").format[String] and
    (__ \ "artist").formatNullable[String] and
    (__ \ "length").formatNullable[Long] and
    (__ \ "elapsed").formatNullable[Long] and
    (__ \ "icon_url").formatNullable[String]
  )(AudioMetadata, unlift(AudioMetadata.unapply))

  implicit val formatsPlaylists: Format[Playlist] = (
    (__ \ "current").formatNullable[Int] and
    (__ \ "songs").format[Seq[AudioMetadata]]
  )(Playlist, unlift(Playlist.unapply))

  implicit val formatsPlayerStatus: Format[PlayerStatus] = (
    (__ \ "state").format[PlayerState] and
    (__ \ "playlist").format[Playlist]
  )(PlayerStatus, unlift(PlayerStatus.unapply))

  private[models] def createEnumFormat[T <: NamedEnum](fn: String => Either[EnumError, T]): Format[T] = new Format[T] {
    override def reads(json: JsValue): JsResult[T] = {
      json.validate[String].flatMap { x =>
        fn(x).fold(
          error => JsError(Seq(JsPath() -> Seq(ValidationError(error.message)))),
          value => JsSuccess(value)
        )
      }
    }

    override def writes(o: T): JsValue = JsString(o.name)
  }
}
