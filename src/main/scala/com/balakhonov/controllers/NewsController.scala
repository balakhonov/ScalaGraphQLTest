package com.balakhonov.controllers

import cats.effect.Async
import cats.implicits._
import com.balakhonov.controllers.decoders.NewsDecoder._
import com.balakhonov.services.NewsService
import io.circe.Json
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl


object NewsController {

  def routes[F[_] : Async](service: NewsService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "news" =>
        for {
          news <- service.list
          resp <- Ok(news)
        } yield resp

      case req@POST -> Root / "graphql" =>
        req.as[Json]
          .flatMap(service.graphqlQuery)
          .flatMap {
            case Right(json) => Ok(json)
            case Left(json) => BadRequest(json)
          }

      case POST -> Root / "grabContent" =>
        for {
          news <- service.grabContent
          resp <- Ok(news)
        } yield resp
    }
  }

}
