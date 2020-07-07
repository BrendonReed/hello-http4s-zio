scalaVersion := "2.13.2" // Also supports 2.12.x

val http4sVersion = "0.21.5"
lazy val doobieVersion = "0.8.8"
val CirceVersion = "0.13.0"
val ZIOVersion = "1.0.0-RC12-1"

// Only necessary for SNAPSHOT releases
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.http4s"      %% "http4s-circe"        % http4sVersion,
    "io.circe"        %% "circe-generic"       % CirceVersion,
    "dev.zio"                  %% "zio"                 % ZIOVersion,
    "dev.zio"                  %% "zio-test"            % ZIOVersion % "test",
    "dev.zio"                  %% "zio-test-sbt"        % ZIOVersion % "test",
    "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC2",
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,
    "org.tpolecat" %% "doobie-specs2" % doobieVersion
)

// Uncomment if you're using Scala 2.12.x
// scalacOptions ++= Seq("-Ypartial-unification")
