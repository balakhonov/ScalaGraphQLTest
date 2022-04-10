package com.balakhonov.graphql.models

import io.circe.JsonObject
import sangria.ast.Document

case class Request(ast: Document,
                   operationName: Option[String],
                   variables: JsonObject)
