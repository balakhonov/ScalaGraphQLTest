package com.balakhonov.schema

import com.balakhonov.models.News
import sangria.schema._

object QueryType {

  val LinkPattern: Argument[Option[String]] = Argument(
    name = "link",
    argumentType = OptionInputType(StringType),
    description = "Unique link."
  )

  val TitlePattern: Argument[Option[String]] = Argument(
    name = "title",
    argumentType = OptionInputType(StringType),
    description = "SQL-style pattern for News title, operation LIKE"
  )

  def apply: ObjectType[GraphQLContext, Unit] = {
    implicit val vot: ValidOutType[Any, Seq[News]] = new ValidOutType[Any, Seq[News]] {}

    ObjectType[GraphQLContext, Unit](
      name = "Query",
      fields = fields(
        Field(
          name = "headlines",
          fieldType = ListType(NewsType.apply),
          description = Some("Returns all news with the given title pattern, if any."),
          arguments = List(LinkPattern, TitlePattern),
          resolve = { c: Context[GraphQLContext, Unit] =>
            c.ctx.dao.list(c.arg(LinkPattern), c.arg(TitlePattern))
          }
        )
      )
    )
  }

  def schema: Schema[GraphQLContext, Unit] = Schema(QueryType.apply)

}