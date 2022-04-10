package com.balakhonov.schema

import com.balakhonov.dao.NewsDao
import io.getquill.{LowerCase, MysqlJdbcContext}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

final case class GraphQLContext(dao: NewsDao[Future])

object GraphQLContext {

  def withQuillContext(implicit quillContext: MysqlJdbcContext[LowerCase]): GraphQLContext = {
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global

    GraphQLContext(
      NewsDao[Future]
    )
  }

}