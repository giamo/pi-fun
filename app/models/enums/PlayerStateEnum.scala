package models.enums


object PlayerStateEnum {
  sealed trait PlayerState extends NamedEnum

  case object PLAYING extends PlayerState { override val name = "playing" }
  case object PAUSED extends PlayerState { override val name = "paused" }
  case object IDLE extends PlayerState { override val name = "idle" }

  def apply(name: String): Either[EnumError, PlayerState] = name match {
    case PLAYING.name => Right(PLAYING)
    case PAUSED.name => Right(PAUSED)
    case IDLE.name => Right(IDLE)
    case _ => Left(BadValue(s"Unknown value '$name' for PlayerState enum"))
  }
}
