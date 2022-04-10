package com.balakhonov.schema

import com.balakhonov.models.News
import sangria.schema._

object NewsType {

  def apply: ObjectType[GraphQLContext, News] = {
    ObjectType(
      name = "News",
      fieldsFn = () => fields(
        Field(
          name = "link",
          fieldType = StringType,
          description = Some("News Link"),
          resolve = _.value.link
        ),
        Field(
          name = "title",
          fieldType = StringType,
          description = Some("News title"),
          resolve = _.value.title
        )
      )
    )
  }

}