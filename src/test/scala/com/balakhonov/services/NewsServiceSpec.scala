package com.balakhonov.services

import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import com.balakhonov.dao.NewsDao
import com.balakhonov.models.News
import io.circe.Json
import org.mockito.specs2.Mockito
import org.specs2.mutable.Specification

class NewsServiceSpec extends Specification with CatsEffect with Mockito {
  "NewsService" should {
    "#list" in {
      val httpClient = mock[HttpClient[IO]]
      val newsParserService = mock[NewsParserService[IO]]
      val graphQLService = mock[GraphQLService[IO]]

      val newsDao = mock[NewsDao[IO]]
      newsDao.list(any[Option[String]], any[Option[String]]) returns IO.pure(List(
        News("link_1", "link_1"),
        News("link_2", "link_2")
      ))

      val helloWorld = NewsService.impl[IO](
        httpClient = httpClient,
        newsParserService = newsParserService,
        graphQLService = graphQLService,
        newsDao = newsDao
      )

      helloWorld.list.flatMap { result =>
        IO.pure {
          result must have size 2
          result must contain(News("link_1", "link_1"))
          result must contain(News("link_2", "link_2"))
        }
      }
    }

    "#graphqlQuery" in {
      val requestBody = Json.obj(
        "query" -> Json.fromString("Some query")
      )
      val httpClient = mock[HttpClient[IO]]
      val newsParserService = mock[NewsParserService[IO]]
      val graphQLService = mock[GraphQLService[IO]]
      graphQLService.query(requestBody) returns IO.pure(Right(Json.fromString("success")))
      val newsDao = mock[NewsDao[IO]]

      val helloWorld = NewsService.impl[IO](
        httpClient = httpClient,
        newsParserService = newsParserService,
        graphQLService = graphQLService,
        newsDao = newsDao
      )

      helloWorld.graphqlQuery(requestBody).flatMap { eiter =>
        IO.pure {
          eiter must beRight(Json.fromString("success"))
          there was one(graphQLService).query(requestBody)
          there was noMoreCallsTo(graphQLService)

          there was noMoreCallsTo(httpClient)
          there was noMoreCallsTo(newsParserService)
          there was noMoreCallsTo(newsDao)
        }
      }
    }

    "#grabContent" in {
      val httpClient = mock[HttpClient[IO]]
      httpClient.get("https://nytimes.com") returns IO.pure("<some_content>")
      httpClient.get("http://example1.com") returns IO.pure("<news_content_1>")
      httpClient.get("http://example2.com") returns IO.pure("<news_content_2>")

      val newsParserService = mock[NewsParserService[IO]]
      newsParserService.parseLinks("<some_content>") returns IO.pure(List("http://example1.com", "http://example2.com"))
      newsParserService.parseTitle("<news_content_1>") returns IO.pure(Some("Title 1"))
      newsParserService.parseTitle("<news_content_2>") returns IO.pure(Some("Title 2"))

      val graphQLService = mock[GraphQLService[IO]]
      val newsDao = mock[NewsDao[IO]]
      newsDao.insert(any[News]) returns IO.unit

      val helloWorld = NewsService.impl[IO](
        httpClient = httpClient,
        newsParserService = newsParserService,
        graphQLService = graphQLService,
        newsDao = newsDao
      )

      helloWorld.grabContent.flatMap { result =>
        IO.pure {
          result must have size 2
          result must contain(News("http://example1.com", "Title 1"))
          result must contain(News("http://example2.com", "Title 2"))

          there was one(httpClient).get("https://nytimes.com")
          there was one(httpClient).get("http://example1.com")
          there was one(httpClient).get("http://example2.com")
          there was noMoreCallsTo(httpClient)

          there was one(newsParserService).parseLinks("<some_content>")
          there was one(newsParserService).parseTitle("<news_content_1>")
          there was one(newsParserService).parseTitle("<news_content_2>")
          there was noMoreCallsTo(newsParserService)

          there was one(newsDao).insert(News("http://example1.com", "Title 1"))
          there was one(newsDao).insert(News("http://example2.com", "Title 2"))
          there was noMoreCallsTo(newsDao)

          there was noMoreCallsTo(newsParserService)
          there was noMoreCallsTo(newsDao)
        }
      }
    }
  }
}