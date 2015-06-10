package evescala.api

import evescala.model.requestresponse.{ApiKeyInfoResponse, ApiKeyInfoRequest}

import scala.concurrent.Future

trait CharacterXMLAPI {
  def apiKeyInfo(request: ApiKeyInfoRequest): Future[ApiKeyInfoResponse]
}
