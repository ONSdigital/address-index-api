package uk.gov.ons.addressIndex.server.utils

import java.io.UnsupportedEncodingException
import java.net.URLDecoder

object DecodeHelper {

  def decodeUrl(url: String): String = {
    val decodedUrl: Either[String, String] = try {
      Left(URLDecoder.decode(url, "UTF-8"))
    } catch {
      case exception: UnsupportedEncodingException =>
        Right("Problems while decoding " + exception.getMessage)
    }

    decodedUrl match {
      case Left(dUrl) => if (url == dUrl) url else decodeUrl(dUrl)
      case Right(exceptionMsg) => exceptionMsg
    }
  }

}
