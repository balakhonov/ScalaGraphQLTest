package com.balakhonov

import cats.effect._

object MainCatsIO
  extends IOApp
    with AppBuilder {

  def run(args: List[String]): IO[ExitCode] = {
    // build services
    val service = AppServiceBuilder[IO].init

    // we use clear Cats IO here
    start(service)
  }

}
