package evescala.api

import com.twitter.bijection.twitter_util.UtilBijections._
import com.twitter.finagle.{Service, Http}
import evescala.model.requestresponse.{BaseRequest, ApiKeyInfoResponse, ApiKeyInfoRequest}
import org.jboss.netty.handler.codec.http._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CachingHTTPCharacterXMLAPI extends CharacterXMLAPI {
  private val eveApiService: Service[HttpRequest, HttpResponse] =
    Http.client.withTlsWithoutValidation().newService(dest = "api.eveonline.com:443")

  private def makeRequest(request: BaseRequest): Future[HttpResponse] = {
    val httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, request.toURLPathWithParams)
    HttpHeaders.setHost(httpRequest, "api.eveonline.com")
    HttpHeaders.setContentLength(httpRequest, 0)
    twitter2ScalaFuture[HttpResponse].apply(eveApiService(httpRequest))
  }

  override def apiKeyInfo(request: ApiKeyInfoRequest): Future[ApiKeyInfoResponse] = {
    makeRequest(request).flatMap(httpResponse => Future.fromTry(ApiKeyInfoResponse.fromHttpResponse(httpResponse)))
  }
}
