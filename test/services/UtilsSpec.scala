package services

import org.scalatestplus.play.PlaySpec

class UtilsSpec extends PlaySpec {

  "isUrl" should {
    "return true if the input has a URL form" in {
      Utils.isUrl("http://website.com") mustBe true
      Utils.isUrl("https://website.com") mustBe true
      Utils.isUrl("file:///local/file") mustBe true
      Utils.isUrl("hdfs://remote/path") mustBe true
    }

    "return false if the input has a URL form" in {
      Utils.isUrl("/local/user/path") mustBe false
      Utils.isUrl("/http/website.com") mustBe false
    }
  }
}