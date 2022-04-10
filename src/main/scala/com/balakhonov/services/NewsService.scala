package com.balakhonov.services

import cats.MonadError
import cats.implicits._
import com.balakhonov.dao.NewsDao
import com.balakhonov.models.News
import io.circe.Json

trait NewsService[F[_]] {
  def list: F[List[News]]

  def graphqlQuery(body: Json): F[Either[Json, Json]]

  def grabContent: F[List[News]]
}

object NewsService {
  final case class NewsException(e: Throwable) extends RuntimeException

  def impl[F[_]](httpClient: HttpClient[F],
                 newsParserService: NewsParserService[F],
                 graphQLService: GraphQLService[F],
                 newsDao: NewsDao[F])
                (implicit me: MonadError[F, Throwable]): NewsService[F] = new NewsService[F] {
    override def list: F[List[News]] = execute {
      newsDao.list(None, None)
    }

    override def graphqlQuery(body: Json): F[Either[Json, Json]] = {
      graphQLService.query(body)
    }

    override def grabContent: F[List[News]] = {
      def crawl(links: List[String]): F[List[News]] = {
        val empty = me.unit.map[List[News]](_ => Nil)

        // run in series
        links.foldLeft(empty) { case (accF, link) =>
          for {
            acc <- accF
            content <- httpClient.get(link)
            titleOpt <- newsParserService.parseTitle(content)
          } yield {
            titleOpt.fold(acc) { title =>
              acc.+:(News(link, title))
            }
          }
        }
      }

      def save(news: Seq[News]): F[Unit] = {
        news.map(newsDao.insert)
          .sequence
          .map(_ => ())
      }

      for {
        res <- httpClient.get("https://nytimes.com")
        links <- newsParserService.parseLinks(res)
        news <- crawl(links)
        _ <- save(news)
      } yield news
    }

    private def execute[A](block: => F[A]): F[A] = {
      me.unit
        .flatMap(_ => block)
        .adaptError { case t =>
          NewsException(t)
        }
    }
  }
}