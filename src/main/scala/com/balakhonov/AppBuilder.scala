package com.balakhonov

import cats.effect.{Async, ExitCode, Resource}
import cats.syntax.all._
import com.balakhonov.controllers.NewsController
import com.balakhonov.services.NewsService
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

trait AppBuilder {

  protected def start[F[_] : Async](service: NewsService[F]): F[ExitCode] = {
    // build server
    val appResource = {
      // prepare routes
      val httpRoutes = NewsController.routes(service).orNotFound

      // prepare app
      val httpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpRoutes)

      EmberServerBuilder.default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(httpApp)
        .build.>>(Resource.eval(Async[F].never))
    }

    Stream.resource(appResource).compile.drain.as(ExitCode.Success)
  }

}
