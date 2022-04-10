lazy val ZioVersion = "1.0.13"
lazy val ZioCatsVersion = "3.2.9.1"
lazy val SttpVersion = "3.5.1"
lazy val ScrappeVersion = "2.2.1"
lazy val SvmVersion = "20.2.0"
lazy val CatsEffectVersion = "3.3.8"
lazy val CatsVersion = "2.7.0"
lazy val Http4sVersion = "0.23.11"
lazy val CirceVersion = "0.14.1"
lazy val MunitVersion = "0.7.29"
lazy val LogbackVersion = "1.2.11"
lazy val MunitCatsEffectVersion = "1.0.7"
lazy val SangriaVersion = "2.1.3"
lazy val SangriaCirceVersion = "1.3.2"
lazy val MysqlConnectorVersion = "8.0.28"

lazy val root = (project in file("."))
  .settings(
    organization := "com.balakhonov",
    name := "avantstaytestcode",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      // Util libs
      "dev.zio" %% "zio" % ZioVersion,
      "dev.zio" %% "zio-interop-cats" % ZioCatsVersion,
      "com.softwaremill.sttp.client3" %% "core" % SttpVersion,
      "net.ruippeixotog" %% "scala-scraper" % ScrappeVersion,

      // API Server
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-optics" % CirceVersion,
      "org.scalameta" %% "svm-subs" % SvmVersion,

      // GraphQL
      "org.sangria-graphql" %% "sangria" % SangriaVersion,
      "org.sangria-graphql" %% "sangria-circe" % SangriaCirceVersion,

      // Database
      "io.getquill" %% "quill-jdbc" % "3.16.3",
      "mysql" % "mysql-connector-java" % MysqlConnectorVersion,

      // Logback
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime,

      // Test libs
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "org.typelevel" %% "cats-effect-testkit" % "3.3.9" % Test,
      "org.typelevel" %% "cats-effect-testing-specs2" % "1.2.0" % Test,
      "org.mockito" %% "mockito-scala-specs2" % "1.17.5"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
