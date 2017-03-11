package services

import java.net.URL

object Utils {

  def isUrl(str: String): Boolean = {
    val rHttp = """(http[s]?://.*)""".r
    val rFile = """(file:///.*)""".r
    val rAny = """(.*://.*)""".r

    str match {
      case rHttp(_) | rFile(_) | rAny(_) => true
      case _ => false
    }
  }

  def isLocalFileUrl(str: String): Boolean = str.startsWith("file:///")

  def urlTitle(url: URL) = url.getPath.split("/").last
}