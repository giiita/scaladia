package refuel.oauth.token.flow

import akka.http.scaladsl.server.{Directive1, Directives}
import refuel.domination.InjectionPriority.Finally
import refuel.inject.Inject
import refuel.oauth.action.OAuth2ActionHandler
import refuel.oauth.authorize.AuthProfile
import refuel.oauth.exception.InvalidGrantException
import refuel.oauth.grant.GrantHandler
import refuel.oauth.token.GrantFlow
import refuel.oauth.token.GrantRequest.PasswordGrantRequest

import scala.concurrent.{ExecutionContext, Future}

@Inject[Finally]
@deprecated("Since OAuth2.1, this specification seems to disappear.")
class PasswordCredentialsFlow(override val actionHandler: OAuth2ActionHandler)(implicit ec: ExecutionContext)
    extends GrantFlow[Future, PasswordGrantRequest]
    with Directives {
  override def sync[U](
      v1: PasswordGrantRequest,
      grantHandler: GrantHandler[Future, U]
  ): Directive1[AuthProfile[U]] =
    onComplete[AuthProfile[U]] {
      for {
        app <- grantHandler.findApp(v1.clientId)
        _   <- Future.fromTry(appVerification(v1, app))
        state <- grantHandler.verifyCredentials
          .lift(v1)
          .getOrElse(
            Future.failed(new InvalidGrantException("Unsupported grant type."))
          )
      } yield state
    }
}
