package com.balakhonov.services

import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import com.balakhonov.dao.NewsDao
import com.balakhonov.models.News
import com.balakhonov.schema.GraphQLContext
import io.circe.Json
import org.mockito.specs2.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

//import cats.syntax.option.{none => _none}

class GraphQLServiceSpec extends Specification with CatsEffect with Mockito {
  "GraphQLService" should {

    val newsDao = mock[NewsDao[Future]]
    val userContext = IO.pure(GraphQLContext(newsDao))

    val graphQLService = GraphQLService.impl[IO](
      userContext,
      blockingExecutionContext = global
    )

    "#query case 1" in {
      newsDao.list(any[Option[String]], any[Option[String]]) returns Future.successful(List(
        News("link_1", "title_1"),
        News("link_2", "title_2")
      ))
      newsDao.list(Some("link_2"), None) returns Future.successful(List(
        News("link_2", "title_2")
      ))

      val requestBody = Json.obj(
        "query" -> Json.fromString(
          """
            |query TestQuery {
            |  headlines {
            |    link
            |    title
            |  }
            |}
            |""".stripMargin)
      )
      val expectedResponse = Json.obj(
        "data" -> Json.obj(
          "headlines" -> Json.arr(
            Json.obj("link" -> Json.fromString("link_1"), "title" -> Json.fromString("title_1")),
            Json.obj("link" -> Json.fromString("link_2"), "title" -> Json.fromString("title_2"))
          )
        )
      )
      graphQLService.query(requestBody).flatMap { either =>
        IO.pure {
          either must beRight(expectedResponse)
        }
      }
    }

    "#query case 2" in {
      newsDao.list(Some("link_2"), None) returns Future.successful(List(
        News("link_2", "title_2")
      ))

      val requestBody = Json.obj(
        "query" -> Json.fromString(
          """
            |query TestQuery {
            |  headlines(link: "link_2") {
            |    link
            |  }
            |}
            |""".stripMargin)
      )
      val expectedResponse = Json.obj(
        "data" -> Json.obj(
          "headlines" -> Json.arr(
            Json.obj("link" -> Json.fromString("link_2"))
          )
        )
      )
      graphQLService.query(requestBody).flatMap { either =>
        IO.pure {
          either must beRight(expectedResponse)
        }
      }
    }

    "#query case 3" in {
      newsDao.list(None, Some("le_")) returns Future.successful(List(
        News("link_1", "title_1"),
        News("link_2", "title_2")
      ))

      val requestBody = Json.obj(
        "query" -> Json.fromString(
          """
            |query TestQuery {
            |  headlines(title: "le_") {
            |    title
            |  }
            |}
            |""".stripMargin)
      )
      val expectedResponse = Json.obj(
        "data" -> Json.obj(
          "headlines" -> Json.arr(
            Json.obj("title" -> Json.fromString("title_1")),
            Json.obj("title" -> Json.fromString("title_2"))
          )
        )
      )
      graphQLService.query(requestBody).flatMap { either =>
        IO.pure {
          either must beRight(expectedResponse)
        }
      }
    }
  }
}
