package com.balakhonov.services

import cats.MonadError
import cats.syntax.all._
import sttp.client3._

trait HttpClient[F[_]] {
  def get(url: String): F[String]
}

object HttpClient {
  final case class HttpClientException(e: Throwable) extends RuntimeException

  def impl[F[_]](implicit me: MonadError[F, Throwable]): HttpClient[F] = new HttpClient[F] {

    override def get(url: String): F[String] = {
      me.unit.map { _ =>
        val request = basicRequest
          .get(uri"$url")

        val backend = HttpURLConnectionBackend()

        val res = request.send(backend)
        res.body match {
          case Left(value) => value
          case Right(value) => value
        }
      }.adaptError { case t =>
        HttpClientException(t)
      }
    }

  }
}
