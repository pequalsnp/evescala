package evescala.converter

import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

object TimeConverter {
  val ccpTimeFormatter = DateTimeFormat.forPattern("Y-M-d H:m:s")
  def fromCCPTime(ccpTimeString: String): Instant = {
    Instant.parse(ccpTimeString, ccpTimeFormatter)
  }
}
