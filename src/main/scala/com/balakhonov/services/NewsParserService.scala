package com.balakhonov.services

import cats.MonadError
import cats.syntax.all._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

trait NewsParserService[F[_]] {
  def parseLinks(content: String): F[List[String]]

  def parseTitle(content: String): F[Option[String]]
}

object NewsParserService {
  final case class NewsParserException(e: Throwable) extends RuntimeException

  def impl[F[_]](implicit me: MonadError[F, Throwable]): NewsParserService[F] = new NewsParserService[F] {
    private val browser = JsoupBrowser()

    override def parseLinks(content: String): F[List[String]] = {
      me.unit.map { _ =>
        val doc = browser.parseString(content)

        val items = doc >> elementList(".story-wrapper a")

        items.map(_.attr("href")).distinct
      }.adaptError { case t =>
        NewsParserException(t)
      }
    }

    override def parseTitle(content: String): F[Option[String]] = {
      me.unit.map[Option[String]] { _ =>
        val doc = browser.parseString(content)

        val titleOpt = doc >?> element("#site-content h1")

        titleOpt.map(_.text)
      }.redeemWith(
        recover = _ => none[String].pure[F], // exclude link
        bind = _.pure[F]
      )
    }

  }

}
