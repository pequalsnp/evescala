package evescala.model.requestresponse

import java.util.concurrent.TimeUnit

import com.twitter.util.Duration
import evescala.converter.TimeConverter
import org.jboss.netty.handler.codec.http.HttpResponse
import org.joda.time.{DateTime, Instant, Interval}

import scala.xml.Elem

trait BaseRequest {
  private val KeyIDParam = "keyID"
  private val vCodeParam = "vCode"
  protected def keyID: Int
  protected def vCode: String

  protected def authenticationParams: String = s"$KeyIDParam=$keyID&$vCodeParam=$vCode"

  def toURLPathWithParams: String
}

object BaseResponse {
  def parseCurrentTimeAndCachedUntil(root: Elem): (Instant, Instant) = {
    val currentTime: Instant = TimeConverter.fromCCPTime((root \\ "eveapi" \ "currentTime").text)
    val cachedUntil: Instant = TimeConverter.fromCCPTime((root \\ "eveapi" \ "cachedUntil").text)
    (currentTime, cachedUntil)
  }
}

trait BaseResponse {
  def currentTime: Instant
  def cachedUntil: Instant

  def durationCachedUntil: Duration = {
    Duration(new Interval(DateTime.now, cachedUntil).toDurationMillis, TimeUnit.MILLISECONDS)
  }
}
