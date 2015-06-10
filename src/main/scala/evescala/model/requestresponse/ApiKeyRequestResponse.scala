package evescala.model.requestresponse

import com.twitter.finagle.http.Response
import evescala.converter.TimeConverter
import evescala.exception.HttpResponseNotOKException
import evescala.model.requestresponse.ApiKey.{CorporationApiKey, AccountApiKey, CharacterApiKey}
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, HttpResponse}
import org.joda.time.Instant
import scala.util.{Failure, Try}
import scala.xml.{Elem, XML}

case class ApiKeyInfoRequest(val keyID: Int, val vCode: String) extends BaseRequest {
  private val URLPath = "/account/APIKeyInfo.xml.aspx"
  override def toURLPathWithParams: String = {
    s"$URLPath?$authenticationParams"
  }
}

case class AccessMask(mask: Int)

case class Character(id: Int, name: String, corporationID: Int, corporationName: String, allianceID: Int, allianceName: String, factionID: Int, factionName: String)
case class Corporation(id: Int, name: String, allianceID: Int, allianceName: String, factionID: Int, factionName: String)

sealed trait ApiKey {
  def accessMask: AccessMask
  def expiresAt: Option[Instant]
}
object ApiKey {
  case class AccountApiKey(val accessMask: AccessMask, val expiresAt: Option[Instant], characters: Seq[Character]) extends ApiKey
  case class CharacterApiKey(val accessMask: AccessMask, val expiresAt: Option[Instant], character: Character) extends ApiKey
  case class CorporationApiKey(val accessMask: AccessMask, val expiresAt: Option[Instant], corporation: Corporation) extends ApiKey
}

object ApiKeyInfoResponse {
  private def parseCharacters(root: Elem): Seq[Character] = {
    for {
      node <- (root \\ "eveapi" \ "result" \ "key" \ "rowset" \ "row")
      characterID = (node \ "@characterID").text.toInt
      characterName = (node \ "@characterName").text
      corporationID = (node \ "@corporationID").text.toInt
      corporationName = (node \ "@corporationName").text
      allianceID = (node \ "@allianceID").text.toInt
      allianceName = (node \ "@allianceName").text
      factionID = (node \ "@factionID").text.toInt
      factionName = (node \ "@factionName").text
    } yield {
      Character(
        id = characterID,
        name = characterName,
        corporationID = corporationID,
        corporationName = corporationName,
        allianceID = allianceID,
        allianceName = allianceName,
        factionID = factionID,
        factionName = factionName
      )
    }
  }

  private def parseCorporations(root: Elem): Seq[Corporation] = {
    for {
      node <- (root \\ "eveapi" \ "result" \ "key" \ "rowset" \ "row")
      corporationID = (node \ "@corporationID").text.toInt
      corporationName = (node \ "@corporationName").text
      allianceID = (node \ "@allianceID").text.toInt
      allianceName = (node \ "@allianceName").text
      factionID = (node \ "@factionID").text.toInt
      factionName = (node \ "@factionName").text
    } yield {
      Corporation(
        id = corporationID,
        name = corporationName,
        allianceID = allianceID,
        allianceName = allianceName,
        factionID = factionID,
        factionName = factionName
      )
    }
  }

  def fromHttpResponse(httpResponse: HttpResponse): Try[ApiKeyInfoResponse] = {
    if (httpResponse.getStatus != HttpResponseStatus.OK) {
      Failure(HttpResponseNotOKException(httpResponse.getStatus))
    } else {
      val xmlString = Response(httpResponse).contentString
      Try {
        val root = XML.loadString(xmlString)
        val (currentTime, cachedUntil) = BaseResponse.parseCurrentTimeAndCachedUntil(root)
        val accessMask: AccessMask = {
          AccessMask(
            (root \\ "eveapi" \ "result" \ "key").map(_ \ "@accessMask").head.text.toInt
          )
        }
        val expiresAt: Option[Instant] = {
          val timeString = (root \\ "eveapi" \ "result" \ "key").map(_ \ "@expires").head.text
          if (timeString.isEmpty) None else Some(TimeConverter.fromCCPTime(timeString))
        }
        val typeString = (root \\ "eveapi" \ "result" \ "key").map(_ \ "@type").head.text

        val apiKey = typeString match {
          case "Account" => AccountApiKey(
            accessMask = accessMask,
            expiresAt = expiresAt,
            characters = parseCharacters(root)
          )
          case "Character" => CharacterApiKey(
            accessMask = accessMask,
            expiresAt = expiresAt,
            character = parseCharacters(root).head
          )
          case "Corporation" => CorporationApiKey(
            accessMask = accessMask,
            expiresAt = expiresAt,
            corporation = parseCorporations(root).head
          )
        }

        val r = ApiKeyInfoResponse(
          currentTime = currentTime,
          cachedUntil = cachedUntil,
          key = apiKey
        )
        println(s"response: $r")
        r
      }
    }
  }
}

case class ApiKeyInfoResponse(
  val currentTime: Instant,
  val cachedUntil: Instant,
  key: ApiKey
)
