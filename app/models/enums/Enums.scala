package models.enums

trait EnumError {
  def message: String
}

case class BadValue(message: String) extends EnumError

trait NamedEnum {
  def name: String
  override def toString(): String = name
}