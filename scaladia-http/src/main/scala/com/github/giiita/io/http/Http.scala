package com.github.giiita.io.http

import com.giitan.injector.Injector
import com.github.giiita.io.http.setting.HttpRequestSetting
import dispatch.{url, _}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Http extends Injector {
  private lazy final val URL_PARAM_FORMAT = "%s=%s"
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  implicit class UrlParameters(value: Map[String, Any]) {
    /**
      * Convert to get request string.
      *
      * @return "x=y&a=b&1=2"
      */
    def asUrl: String = {
      value.map { x =>
        URL_PARAM_FORMAT.format(x._1, x._2.toString)
      }.mkString("&")
    }
  }

  /**
    * Create a http request task.
    * {{{
    *   import com.github.giiita.io.http.Http._
    *
    *   val requets = Map(
    *     "id" -> 1,
    *     "name" -> "Jack"
    *   )
    *
    *   val result: FutureSearch.Response =
    *     http[GET](s"http://localhost:80/?${requets.asUrl}")
      *     .header("auth", "abcde")
      *     .deserializing[ResponseType]
      *     .map(_.value)
      *     .flatMap(FutureSearch.byValue)
      *     .run
    * }}}
    *
    * @param urlString Request url
    * @tparam T Request method type. See [[com.github.giiita.io.http.HttpMethod]]
    * @return
    */
  def http[T <: HttpMethod.Method : MethodType](urlString: String): HttpBuilderTask = {
    logger.info(s"Setup http request [ $urlString ]")

    val setting = inject[HttpRequestSetting].recover {
      case _ => new HttpRequestSetting()
    }

    new HttpBuilderTask(
      implicitly[MethodType[T]].method(
        setting.globalHeader
          .foldLeft(url(urlString))((x, y) => x.setHeader(y.name, y.value.toString))
          .setBodyEncoding(setting.bodyEncoding)
      ),
      setting
    )
  }
}

private object HttpBuilderTask extends Injector {

  private lazy final val RETRY = 2
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  /**
    * Http request executor with retry.
    *
    * @param retry Max retry count.
    * @param func  Execution.
    * @tparam R Return type.
    * @return
    */
  private def retryRequest[R](retry: Int = 1)(func: => Future[R]): Future[R] = {
    func.transform {
      case Success(x)                   =>
        Success(Future(x))
      case Failure(x) if retry >= RETRY =>
        logger.error(s"Request retry failed.", x)
        Failure(x)
      case Failure(x)                   =>
        logger.warn(s"Request retry failed. ${x.getMessage}")
        Try(retryRequest(retry + 1)(func))
    }.flatten
  }
}

sealed class HttpBuilderTask(request: Req, setting: HttpRequestSetting) extends JacksonParser {
  /**
    * Set a request body.
    *
    * @param value Request body. It will be serialized by Jackson.
    * @tparam T Request body type.
    * @return
    */
  def body[T](value: T): HttpBuilderTask = new HttpBuilderTask(
    request.setBody(serialize(value)),
    setting
  )

  /**
    * Set a requets header.
    *
    * @param key   header key
    * @param value header value
    * @return
    */
  def header(key: String, value: String): HttpBuilderTask = new HttpBuilderTask(
    request.setHeader(key, value),
    setting
  )

  /**
    * Regist a type of returning deserialized json texts.
    *
    * @tparam T Deserialized type.
    * @return
    */
  def deserializing[T]: HttpRunner[T] = new HttpRunner[T](
    () => HttpBuilderTask.retryRequest() {
      val cli = dispatch.Http(dispatch.Http.defaultClientBuilder.setRequestTimeout(setting.timeout))
      cli(request.OK(as.String)).map { x =>
        cli.client.close()
        x
      }.map(deserialize)
    }
  )
}

sealed class HttpRunner[T](request: HttpResultTask[T]) extends JacksonParser {
  /**
    * To synthesize.
    *
    * @param func Synthesis processing.
    * @tparam R Synthesize return type.
    * @return
    */
  def map[R](func: T => R): HttpRunner[R] = new HttpRunner(() => run.map(func))

  /**
    * To flatten synthesize.
    *
    * @param func Synthesis processing.
    * @tparam R Synthesize return type.
    * @return
    */
  def flatMap[R](func: T => Future[R]): HttpRunner[R] = new HttpRunner(() => run.flatMap(func))

  /**
    * Execute future functions.
    *
    * @return
    */
  def run: Future[T] = request.execute()
}

trait HttpResultTask[T] extends JacksonParser {
  def execute(): Future[T]
}

object HttpMethod {

  abstract class Method

  class GET extends Method

  class PUT extends Method

  class POST extends Method

  class DELETE extends Method

}