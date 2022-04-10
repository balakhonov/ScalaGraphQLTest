package com.balakhonov.controllers

import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import com.balakhonov.dao.NewsDao
import com.balakhonov.models.News
import com.balakhonov.services.{GraphQLService, HttpClient, NewsParserService, NewsService}
import org.http4s._
import org.http4s.implicits._
import org.mockito.specs2.Mockito
import org.specs2.mutable.Specification

class NewsControllerSpec extends Specification with CatsEffect with Mockito {
  "NewsController" should {
    "#list" in {
      val getHW = Request[IO](Method.GET, uri"/news")

      val httpClient = mock[HttpClient[IO]]
      val newsParserService = mock[NewsParserService[IO]]
      val graphQLService = mock[GraphQLService[IO]]
      val newsDao = mock[NewsDao[IO]]
      newsDao.list(any[Option[String]], any[Option[String]]) returns IO.pure(List(
        News("link_1", "title_1"),
        News("link_2", "title_2")
      ))

      val helloWorld = NewsService.impl[IO](
        httpClient = httpClient,
        newsParserService = newsParserService,
        graphQLService = graphQLService,
        newsDao = newsDao
      )

      val resource = NewsController.routes(helloWorld).orNotFound(getHW)

      resource.flatMap { result =>
        IO.pure {
          result.status must_== Status.Ok
          result.as[String].map { body =>
            body must_== """[{"link":"link_1","title":"title_1"},{"link":"link_2","title":"title_2"}]"""
          }
        }.flatten
      }
    }
  }
}