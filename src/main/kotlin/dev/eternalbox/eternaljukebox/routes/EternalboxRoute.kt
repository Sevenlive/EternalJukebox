package dev.eternalbox.eternaljukebox.routes

import dev.eternalbox.eternaljukebox.*
import dev.eternalbox.eternaljukebox.data.HttpResponseCodes
import dev.eternalbox.eternaljukebox.data.WebApiResponseCodes
import dev.eternalbox.eternaljukebox.data.WebApiResponseMessages
import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.Vertx
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.CoroutineContext

@ExperimentalContracts
@ExperimentalCoroutinesApi
abstract class EternalboxRoute(val jukebox: EternalJukebox) {
    abstract class Factory<T: EternalboxRoute>(override val name: String): RouteFactory<T>

    val vertx: Vertx
        get() = jukebox.vertx
    val baseRouter: Router
        get() = jukebox.baseRouter
    val apiRouter: Router
        get() = jukebox.apiRouter

    open val coroutineScope: CoroutineScope = GlobalScope
    open val context: CoroutineContext = vertx.dispatcher()

    suspend fun apiRouteNotFound(context: RoutingContext) {
        routeWith(context) {
            response().setStatusCode(HttpResponseCodes.NOT_FOUND).endJsonAwait {
                "error" .. WebApiResponseCodes.NOT_FOUND
                "message" .. errorMessage(WebApiResponseMessages.API_ROUTE_NOT_FOUND, request().path())
            }
        }
    }

    suspend fun apiMethodNotAllowedForRoute(context: RoutingContext) {
        routeWith(context) {
            response().setStatusCode(HttpResponseCodes.METHOD_NOT_ALLOWED).endJsonAwait {
                "error" .. WebApiResponseCodes.METHOD_NOT_ALLOWED
                "message" .. errorMessage(WebApiResponseMessages.API_METHOD_NOT_ALLOWED_FOR_ROUTE, request().rawMethod(), request().path())
            }
        }
    }

    suspend fun apiMethodNotAllowedRouteSupportsGet(context: RoutingContext) {
        routeWith(context) {
            response()
                    .setStatusCode(HttpResponseCodes.METHOD_NOT_ALLOWED)
                    .putHeader(HttpHeaderNames.ALLOW, "GET")
                    .endJsonAwait {
                        "error" .. WebApiResponseCodes.METHOD_NOT_ALLOWED
                        "message" .. errorMessage(WebApiResponseMessages.API_METHOD_NOT_ALLOWED_FOR_ROUTE, request().rawMethod(), request().path())
                    }
        }
    }

    suspend fun apiNotImplemented(context: RoutingContext) {
        routeWith(context) {
            response()
                    .setStatusCode(HttpResponseCodes.NOT_IMPLEMENTED)
                    .endJsonAwait {
                        "error" .. WebApiResponseCodes.NOT_IMPLEMENTED
                        "message" .. errorMessage(WebApiResponseMessages.API_NOT_IMPLEMENTED, request().rawMethod(), request().path())
                    }
        }
    }

    //    fun Route.suspendHandler(func: suspend (RoutingContext) -> Unit) = suspendHandler(coroutineScope, context, func)
    fun Router.suspendErrorHandler(errorCode: Int, func: suspend (RoutingContext) -> Unit) = suspendErrorHandler(errorCode, coroutineScope, context, func)

    fun Route.suspendHandler(func: suspend (RoutingContext) -> Unit) = suspendHandler(coroutineScope, context, func)
    fun Route.suspendFailureHandler(func: suspend (RoutingContext) -> Unit) = suspendFailureHandler(coroutineScope, context, func)
}