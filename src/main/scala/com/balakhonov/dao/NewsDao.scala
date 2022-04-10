package com.balakhonov.dao

import cats.MonadError
import cats.syntax.functor._
import com.balakhonov.models.News
import io.getquill.{LowerCase, MysqlJdbcContext}

import scala.concurrent.Future

trait NewsDao[F[_]] {
  def list(link: Option[String],
           title: Option[String]): F[List[News]]

  def insert(news: News): F[Unit]

}

object NewsDao {

  def apply[F[_] : NewsDao]: NewsDao[F] = implicitly[NewsDao[F]]

  def init[F[_]](implicit quillContext: MysqlJdbcContext[LowerCase],
                 me: MonadError[F, Throwable]): NewsDao[F] = {
    new NewsDao[F] {

      import quillContext._

      private val headlines = quote {
        querySchema[News]("headlines")
      }

      override def list(link: Option[String],
                        title: Option[String]): F[List[News]] = {
        me.unit.map { _ =>
          val qr = quote {
            headlines
              .filter(r => lift(link).forall(r.link == _))
              .filter(r => lift(title).forall(v => r.title like s"%$v%"))
          }
          quillContext.run(qr)
        }
      }

      override def insert(news: News): F[Unit] = {
        me.unit.map { _ =>
          val qr = quote {
            headlines.insertValue(lift(news))
              .onConflictIgnore(_.link) // not throw exception if record already exists with this primary key
          }
          quillContext.run(qr)
          ()
        }
      }
    }
  }

  implicit def newsDaoFuture(implicit quillContext: MysqlJdbcContext[LowerCase],
                             me: MonadError[Future, Throwable]): NewsDao[Future] = {
    NewsDao.init[Future]
  }

}