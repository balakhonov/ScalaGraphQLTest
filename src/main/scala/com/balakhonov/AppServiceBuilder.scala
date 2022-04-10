package com.balakhonov

import cats.MonadError
import cats.syntax.all._
import com.balakhonov.dao.NewsDao
import com.balakhonov.schema.GraphQLContext
import com.balakhonov.services._
import io.getquill.{LowerCase, MysqlJdbcContext}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext


trait AppServiceBuilder[F[_]] {
  def init: NewsService[F]
}

object AppServiceBuilder {

  def apply[F[_]](implicit me: MonadError[F, Throwable]): AppServiceBuilder[F] = new AppServiceBuilder[F] {

    override def init: NewsService[F] = {
      val blockingContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
      implicit val quillContext: MysqlJdbcContext[LowerCase] = createQuillContext

      val httpClient = HttpClient.impl[F]
      val newsParserService = NewsParserService.impl[F]
      val graphQLService = implGraphQLService(quillContext, blockingContext)
      val newsDao = NewsDao.init[F]

      NewsService.impl[F](httpClient, newsParserService, graphQLService, newsDao)
    }

    private def createQuillContext: MysqlJdbcContext[LowerCase] = {
      val ctx = new MysqlJdbcContext[LowerCase](LowerCase, "ctx")

      ctx
    }

    private def implGraphQLService(quillContext: MysqlJdbcContext[LowerCase],
                                   blockingContext: ExecutionContext): GraphQLService[F] = {
      GraphQLService.impl[F](
        userContext = GraphQLContext.withQuillContext(quillContext).pure[F],
        blockingExecutionContext = blockingContext
      )
    }
  }
}
