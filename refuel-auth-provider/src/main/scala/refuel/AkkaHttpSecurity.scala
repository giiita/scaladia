package refuel

import java.security.Security
import java.util

import akka.actor.ActorSystem
import akka.http.scaladsl.common.StrictForm
import akka.http.scaladsl.model.headers.`Set-Cookie`
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.{authorize => akkaHttpAuthorize}
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server.{Directive, Directive0, Directive1, Directives, Route, RouteResult}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import akka.util.ByteString
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.config.Config
import org.pac4j.core.engine.{
  CallbackLogic,
  DefaultCallbackLogic,
  DefaultLogoutLogic,
  DefaultSecurityLogic,
  LogoutLogic,
  SecurityLogic
}
import org.pac4j.core.http.adapter.HttpActionAdapter
import org.pac4j.core.profile.UserProfile
import org.pac4j.core.util.Pac4jConstants
import org.pac4j.saml.client.SAML2Client
import org.pac4j.saml.config.SAML2Configuration
import refuel.http.AkkaHttpActionAdapter
import refuel.injector.AutoInject
import refuel.json.codecs.Read
import refuel.json.{CodecDef, EncodedJsonTranform}
import AkkaHttpWebContext.ResponseChanges
import refuel.saml.SAMLAuthConfig
import refuel.session.SessionIDGenerator
import refuel.store.SessionStorage

import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

object AkkaHttpSecurity extends EncodedJsonTranform with CodecDef with Directives {
  type AkkaHttpSecurityLogic = SecurityLogic[Future[RouteResult], AkkaHttpWebContext]
  type AkkaHttpCallbackLogic = CallbackLogic[Future[RouteResult], AkkaHttpWebContext]
  type AkkaHttpLogoutLogic   = LogoutLogic[Future[RouteResult], AkkaHttpWebContext]

  def auth(authorizer: Authorizer[UserProfile])(request: AuthenticatedRequest): Directive0 =
    akkaHttpAuthorize(authorizer.isAuthorized(request._webContext, request.profiles.asJava))

  private[refuel] final def applyHeadersAndCookiesToResponse(
      changes: ResponseChanges
  )(httpResponse: HttpResponse): HttpResponse = {
    val regularHeaders: List[HttpHeader]             = changes.headers
    val cookieHeaders: List[HttpHeader]              = changes.cookies.map(v => `Set-Cookie`(v))
    val additionalHeaders: immutable.Seq[HttpHeader] = regularHeaders ++ cookieHeaders

    httpResponse.mapHeaders(h => (additionalHeaders ++ h).distinct)
  }

  private[refuel] final def getFormFields(
      request: HttpRequest
  )(
      implicit materializer: Materializer,
      executionContext: ExecutionContext,
      as: ActorSystem
  ): Future[Map[String, String]] = {
    Unmarshal(request.entity).to[StrictForm].flatMap { x =>
      Future
        .sequence(
          x.fields.map {
            case (name, field) if name.nonEmpty =>
              Unmarshal(field).to[String].map(fieldString => (name, fieldString))
          }
        )
        .map(_.toMap)
    }
  }

  protected def unmarshaller[T](implicit read: Read[T], system: ActorSystem): Unmarshaller[HttpRequest, T] =
    Unmarshaller.apply { implicit ec => (hr: HttpRequest) =>
      hr.entity.dataBytes
        .runFold(ByteString.empty)(_ ++ _)
        .map(_.utf8String)
        .flatMap { r =>
          __toEntryMaterialization(r)
            .as[T]
            .fold(e => Future.failed(e), Future(_))
        }
    }
}

/** {{{
  * class Controller(securityBuilder: AuthnSecrityBuilder) {
  *   securityBuilder.build(samlConfig, )
  * }
  * }}}
  *
  * @param conf injected
  * @param gen injected
  * @param storage injected
  */
class AuthnSAMLBuilder(conf: SAMLAuthConfig, gen: SessionIDGenerator, storage: SessionStorage)(implicit as: ActorSystem)
    extends AutoInject {
  Security.addProvider(new BouncyCastleProvider())

  lazy final val DefaultSAMLConf: SAML2Configuration = {
    val config = new SAML2Configuration(
      conf.keystorePath,
      conf.keystorePassword,
      conf.privateKeyPassword,
      conf.idpMetadataPath
    )
    conf.spMetadataPath.foreach(config.setServiceProviderMetadataPath)
    config.setAuthnRequestBindingType(conf.authnRequestBindingType)
    config
  }

  lazy final val DefaultSAMLClient: SAML2Client = {
    val client = new SAML2Client(DefaultSAMLConf)
    client.setCallbackUrl(conf.callbackurl)
    client
  }

  lazy final val Default: AkkaHttpSecurity = {
    build(
      new Config(DefaultSAMLClient)
    )
  }

  def build(config: Config)(implicit as: ActorSystem) =
    new AkkaHttpSecurity(config, storage, conf, gen)
}

class AkkaHttpSecurity private[refuel] (
    config: Config,
    sessionStorage: SessionStorage,
    conf: SAMLAuthConfig,
    gen: SessionIDGenerator
)(implicit val as: ActorSystem) {

  import AkkaHttpSecurity._
  private[this] implicit lazy final val ec: ExecutionContext = as.dispatcher

  private[refuel] lazy final val SecurityLogic: AkkaHttpSecurityLogic =
    Option(config.getSecurityLogic).fold[AkkaHttpSecurityLogic](
      new DefaultSecurityLogic[Future[RouteResult], AkkaHttpWebContext]
    )(_.asInstanceOf[AkkaHttpSecurityLogic])

  private[refuel] lazy final val ActionAdapter: HttpActionAdapter[Future[RouteResult], AkkaHttpWebContext] =
    Option(config.getHttpActionAdapter).fold[HttpActionAdapter[Future[RouteResult], AkkaHttpWebContext]](
      AkkaHttpActionAdapter
    )(_.asInstanceOf[HttpActionAdapter[Future[RouteResult], AkkaHttpWebContext]])

  private[refuel] lazy final val CallbackLogic: CallbackLogic[Future[RouteResult], AkkaHttpWebContext] =
    Option(config.getCallbackLogic).fold[CallbackLogic[Future[RouteResult], AkkaHttpWebContext]](
      new DefaultCallbackLogic[Future[RouteResult], AkkaHttpWebContext]
    )(_.asInstanceOf[AkkaHttpCallbackLogic])

  private[refuel] lazy final val LogoutLogic: LogoutLogic[Future[RouteResult], AkkaHttpWebContext] =
    Option(config.getLogoutLogic).fold[LogoutLogic[Future[RouteResult], AkkaHttpWebContext]](
      new DefaultLogoutLogic[Future[RouteResult], AkkaHttpWebContext]
    )(_.asInstanceOf[AkkaHttpLogoutLogic])

  /**
    * Callback to finish the login process for indirect clients.
    */
  def callback(
      defaultUrl: String = Pac4jConstants.DEFAULT_URL_VALUE,
      saveInSession: Boolean = true,
      multiProfile: Boolean = true,
      defaultClient: Option[String] = None,
      existingContext: Option[AkkaHttpWebContext] = None,
      setCsrfCookie: Boolean = true
  )(implicit as: ActorSystem): Route =
    withFormParameters.apply { formParams =>
      withContext(existingContext, formParams) { akkaWebContext => _ =>
        CallbackLogic
          .perform(
            akkaWebContext,
            config,
            ActionAdapter,
            defaultUrl,
            saveInSession,
            multiProfile,
            true,
            defaultClient.orNull
          )
          .map { result =>
            if (setCsrfCookie) akkaWebContext.addResponseCsrfCookie()
            result
          }
      }
    }

  def withFormParameters(implicit as: ActorSystem): Directive1[Map[String, String]] =
    Directive[Tuple1[Map[String, String]]] { inner => ctx =>
      getFormFields(ctx.request).flatMap { params => inner(Tuple1(params))(ctx) }
    }

  def logout(
      defaultUrl: String = Pac4jConstants.DEFAULT_URL_VALUE,
      logoutPatternUrl: String = Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE,
      localLogout: Boolean = true,
      destroySession: Boolean = true,
      centralLogout: Boolean = true
  ): Route = {
    withContext() { akkaWebContext => ctx =>
      LogoutLogic.perform(
        akkaWebContext,
        config,
        ActionAdapter,
        defaultUrl,
        logoutPatternUrl,
        localLogout,
        destroySession,
        centralLogout
      )
    }
  }

  /** This directive constructs a pac4j context for a route. This means the request is interpreted into
    * an AkkaHttpWebContext and any changes to this context are applied when the route returns (e.g. headers/cookies).
    *
    * @param existingContext
    * @param formParams
    * @return
    */
  def withContext(
      existingContext: Option[AkkaHttpWebContext] = None,
      formParams: Map[String, String] = Map.empty
  ): Directive1[AkkaHttpWebContext] =
    Directive[Tuple1[AkkaHttpWebContext]] { inner => ctx =>
      val akkaWebContext = existingContext.getOrElse(
        AkkaHttpWebContext(ctx.request, formParams, sessionStorage)(conf, gen)
      )
      inner(Tuple1(akkaWebContext))(ctx).map[RouteResult] {
        case Complete(response) => Complete(applyHeadersAndCookiesToResponse(akkaWebContext.getChanges)(response))
        case rejection          => rejection
      }
    }

  def clientsAuthentication(multiProfile: Boolean = true): Directive1[AuthenticatedRequest] = {
    withAuthentication(
      config.getClients.findAllClients().asScala.map(_.getName).mkString(","),
      multiProfile,
      config.getAuthorizers.keySet().asScala.mkString(",")
    )
  }

  /** Authenticate using the provided pac4j configuration. Delivers an AuthenticationRequest which can be used for further authorization
    * this does not apply any authorization ofr filtering.
    *
    * @param clients
    * @param multiProfile
    * @param authorizers
    * @return
    */
  def withAuthentication(
      clients: String = null /* Default null, meaning all defined clients */,
      multiProfile: Boolean = true,
      authorizers: String = ""
  ): Directive1[AuthenticatedRequest] =
    withContext().flatMap { akkaWebContext =>
      Directive[Tuple1[AuthenticatedRequest]] { inner => ctx =>
        Future.unit.flatMap { _ =>
          SecurityLogic.perform(
            akkaWebContext,
            config,
            (context: AkkaHttpWebContext, profiles: util.Collection[UserProfile], parameters: Any) => {
              val authenticatedRequest = new AuthenticatedRequest(context, profiles.asScala.toList)
              inner(Tuple1(authenticatedRequest))(ctx)
            },
            ActionAdapter,
            clients,
            authorizers,
            "",
            multiProfile
          )
        }
      }
    }
}
