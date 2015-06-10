package evescala.exception

import org.jboss.netty.handler.codec.http.HttpResponseStatus

case class HttpResponseNotOKException(status: HttpResponseStatus) extends Exception(s"http respose failed: $status")
