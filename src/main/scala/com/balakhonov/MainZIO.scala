package com.balakhonov

import com.balakhonov.services.NewsService
import zio.interop.catz._

object MainZIO
  extends CatsApp
    with AppBuilder {

  override def run(args: List[String]): zio.URIO[zio.ZEnv, zio.ExitCode] = {
    val service: NewsService[zio.Task] = AppServiceBuilder[zio.Task].init

    // we use interop to convert ZIO Task to Cats Async as it not possible to use any other Monades for Http4s
    start[zio.Task](service).exitCode
  }

}
