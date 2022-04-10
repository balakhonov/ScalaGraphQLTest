package com.balakhonov.services

import _root_.sangria.execution._
import _root_.sangria.execution.deferred._
import _root_.sangria.marshalling.circe._
import _root_.sangria.parser.{QueryParser, SyntaxError}
import _root_.sangria.schema._
import _root_.sangria.validation._
import cats.MonadError
import cats.implicits._
import com.balakhonov.graphql.models.Request
import com.balakhonov.schema.{GraphQLContext, QueryType}
import io.circe.optics.JsonPath.root
import io.circe.{Json, JsonObject}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}

trait GraphQLService[F[_]] {

  def query(request: Json): F[Either[Json, Json]]

}

object GraphQLService {

  // Some circe lenses
  private val queryStringLens = root.query.string
  private val operationNameLens = root.operationName.string
  private val variablesLens = root.variables.obj

  final case class GraphQLException(e: Throwable) extends RuntimeException

  def impl[F[_]](userContext: F[GraphQLContext],
                 blockingExecutionContext: ExecutionContext)
                (implicit me: MonadError[F, Throwable]): GraphQLService[F] = {
    new GraphQLService[F] {
      private val schema = Schema[GraphQLContext, Unit](query = QueryType.apply)

      override def query(body: Json): F[Either[Json, Json]] = {
        userContext.map { ctx =>
          val request = parseQuery(body)
          val result = request.map(_.flatMap(exec(ctx, _)))

          result match {
            case Some(Right(json)) => json.asRight
            case Some(Left(ex: SyntaxError)) => fail(formatSyntaxError(ex))
            case Some(Left(ex: WithViolations)) => fail(formatWithViolations(ex))
            case Some(Left(ex)) => fail(formatThrowable(ex))
            case None => fail(EmptyQueryJsonError)
          }
        }.adaptError { case t =>
          GraphQLException(t)
        }

      }

      // Parse GraphQL request
      private def parseQuery(request: Json): Option[Either[Throwable, Request]] = {
        val queryStringOpt = queryStringLens.getOption(request)
        val operationName = operationNameLens.getOption(request)
        val variables = variablesLens.getOption(request).getOrElse(JsonObject())

        queryStringOpt.map(QueryParser.parse(_)).map(_.map { ast =>
          Request(ast, operationName, variables)
        }.toEither)
      }

      // Execute a GraphQL query
      private def exec(ctx: GraphQLContext,
                       request: Request): Either[Throwable, Json] = {
        implicit val ec: ExecutionContext = blockingExecutionContext

        val result = Executor.execute(
          schema = schema,
          deferredResolver = DeferredResolver.empty,
          queryAst = request.ast,
          userContext = ctx,
          variables = Json.fromJsonObject(request.variables),
          operationName = request.operationName,
          exceptionHandler = ExceptionHandler {
            case (_, e) => HandledException(e.getMessage)
          }
        )
        // TODO in scope of this test task we should create an abstraction to support different IO libs, unfortunatelly it affects us
        // in using Async Operation to convert Future into F[_], so I've leave it as blocked operation
        // normally there should be some block like Async[F].async{ ... } what will handle a future result
        Await.result(result.attempt, 10.seconds)
      }

      private def fail(j: Json): Either[Json, Json] = j.asLeft
    }
  }

  // Format a String as a GraphQL `errors`
  private val EmptyQueryJsonError: Json = Json.obj(
    "errors" -> Json.arr(Json.obj(
      "message" -> Json.fromString("No 'query' property was present in the request."))
    )
  )

  // Format a SyntaxError as a GraphQL `errors`
  private def formatSyntaxError(e: SyntaxError): Json = Json.obj(
    "errors" -> Json.arr(Json.obj(
      "message" -> Json.fromString(e.getMessage),
      "locations" -> Json.arr(Json.obj(
        "line" -> Json.fromInt(e.originalError.position.line),
        "column" -> Json.fromInt(e.originalError.position.column)
      ))
    ))
  )

  // Format a WithViolations as a GraphQL `errors`
  private def formatWithViolations(e: WithViolations): Json = Json.obj(
    "errors" -> Json.fromValues(e.violations.map {
      case v: AstNodeViolation =>
        Json.obj(
          "message" -> Json.fromString(v.errorMessage),
          "locations" -> Json.fromValues(v.locations.map(loc => Json.obj(
            "line" -> Json.fromInt(loc.line),
            "column" -> Json.fromInt(loc.column)
          )))
        )

      case v =>
        Json.obj(
          "message" -> Json.fromString(v.errorMessage)
        )
    }))

  // Format a Throwable as a GraphQL `errors`
  private def formatThrowable(e: Throwable): Json = Json.obj(
    "errors" -> Json.arr(Json.obj(
      "class" -> Json.fromString(e.getClass.getName),
      "message" -> Json.fromString(e.getMessage)
    ))
  )

}